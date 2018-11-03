// Include the libraries we need

#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include "cfg.h"

#define TEMPERATURE_PRECISION 9 // Lower resolution
#define BUF_SZ 255
#define SETUP_PIN 0
#define LED_PIN 12

// do not work with parasite

// 5k res

//#define ONE_WIRE_BUS 2
#define ONE_WIRE_BUS 13
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
DeviceAddress tempDeviceAddress;  

struct TempData {
  uint8_t id;
  uint8_t make;
  uint8_t res;
  uint8_t isParasite;
  int16_t t10;
} tData = {0};

ADC_MODE(ADC_VCC);

/*
 * The setup function. We only start the sensors here
 */
void setup(void)
{
  bool isSetup = false;
  bool isCfgValid = false;

  pinMode(LED_PIN, OUTPUT);

  digitalWrite(LED_PIN, HIGH); 
  
  
  // start serial port
  delay(100);  
  Serial.begin(115200);
  Serial.println();

  digitalWrite(LED_PIN, LOW); 
  

  if(CfgDrv::Cfg.init() && CfgDrv::Cfg.load()) {
    Serial.println(F("Cfg loaded"));
    isCfgValid = CfgDrv::Cfg.validate();
    if(!isCfgValid) {
      Serial.println(F("Invalid config, force setup!"));
    }
  } else {
    Serial.println(F("Failed to load cfg!"));
  }

  if(isCfgValid) {
    Serial.println(F("Press button1 now to enter setup"));
    delay(5000);  
    pinMode(SETUP_PIN, INPUT);  
    isSetup = false;  
    if(digitalRead(SETUP_PIN)==LOW) {
      delay(100);
      if(digitalRead(SETUP_PIN)==LOW) {
        isSetup = true;
      }
    }
   }

  sensors.begin();

  if(isSetup || !isCfgValid) {
    digitalWrite(LED_PIN, HIGH); 

    do {
      CfgDrv::Cfg.setup();
      isCfgValid = CfgDrv::Cfg.validate();      
    } while (!isCfgValid);
    CfgDrv::Cfg.store();
    delay(1000);
    Serial.println(F("Cfg OK"));
    delay(1000);
    doSensorSetup();

    digitalWrite(LED_PIN, LOW); 

  }
  
    // Start up the library
  digitalWrite(LED_PIN, HIGH); 
  delay(1000);
  digitalWrite(LED_PIN, LOW);

  
  tData.id = CfgDrv::Cfg.id;
  doDiag(&tData);  
  tData.t10 = doMeasure(); 

// do -127 chexck here

  if(doConnect()) {
    doSend(&tData);
  }

  Serial.println(F("deep sleep"));
   digitalWrite(LED_PIN, HIGH); 

  delay(500); //
    digitalWrite(LED_PIN, LOW);


  ESP.deepSleep(10000000);

}

void loop(void)
{ 
  delay(5000);
  tData.t10 = doMeasure(); 
  doSend(&tData);
}

bool doConnect()
{
  byte mac[6]; 
  uint8_t i = 0;
  WiFi.macAddress(mac);
  Serial.print(F("MAC: "));
  for(i=0; i<6; i++) {
    Serial.print(mac[i],HEX);
    if(i<5) Serial.print(F(":"));
  }
 
  WiFi.begin(CfgDrv::Cfg.SSID, CfgDrv::Cfg.PWD);
  Serial.print(F("\nConnecting to ")); Serial.print(CfgDrv::Cfg.SSID);
  i = 0;
  while (WiFi.status() != WL_CONNECTED && i++ < 60) {delay(500); Serial.print(".");}
  Serial.println();
  if(i == 21){
    Serial.print(F("Could not connect to ")); Serial.println(CfgDrv::Cfg.SSID);
    //delay(10000);
    //ESP.reset();
    return false;
  }

  Serial.print("Connected, IP: ");
  Serial.println(WiFi.localIP());
  
  return true;
} 

int16_t doMeasure()
{
  //Serial.print("Requesting temperatures...");
  sensors.requestTemperatures(); // Send the command to get temperatures
  // After we got the temperatures, we can print them here.
  // We use the function ByIndex, and as an example get the temperature from the first sensor only.
  float t=sensors.getTempCByIndex(0);
  Serial.print("Temperature is: ");
  Serial.println(t);    
  //DEVICE_DISCONNECTED_C;
  //DEVICE_DISCONNECTED_RAW
  //#define DEVICE_DISCONNECTED_C -127
  //#define DEVICE_DISCONNECTED_RAW -7040
  
  return (int16_t)(t*10);
}

bool doDiag(TempData *pData)
{
  // call sensors.requestTemperatures() to issue a global temperature 
  // request to all devices on the bus

  // diagnostics
  
  if(!sensors.getAddress(tempDeviceAddress, 0)) {
    Serial.println("Failed to get device"); 
    return false;
  }

  //pData->id=0;
  pData->isParasite = sensors.isParasitePowerMode();
  pData->res = sensors.getResolution(tempDeviceAddress);
  pData->make = tempDeviceAddress[0];
  
  Serial.print("Parasite: "); Serial.print(pData->isParasite); 
  Serial.print("  Res: "); Serial.print(pData->res); 
  Serial.print("  Make: "); Serial.println(pData->make, HEX); 
 
  return true;
}

bool doSend(TempData *pData) {
  WiFiClient client;
  if (!client.connect(CfgDrv::Cfg.srv_addr, CfgDrv::Cfg.srv_port)) {
      Serial.println("connection failed");
      return false;
    }

  Serial.print("connected to ");
  Serial.print(CfgDrv::Cfg.srv_addr);
  Serial.print(":");
  Serial.println(CfgDrv::Cfg.srv_port);


  char bufout[BUF_SZ];
  StaticJsonBuffer<400> jsonBufferOut;
  JsonObject& rootOut = jsonBufferOut.createObject();
  rootOut["I"] = pData->id;
  rootOut["M"] = pData->make;
  rootOut["P"] = pData->isParasite;
  rootOut["R"] = pData->res;
  rootOut["T"] = pData->t10;
  rootOut["V"]=ESP.getVcc();
  rootOut.printTo(bufout, BUF_SZ-1);
  client.print(bufout);
  
  unsigned long timeout = millis();
  Serial.print("Sent in "); 
  Serial.print(millis() - timeout);
  Serial.println("ms"); 
 
  client.stop(); 
  return true;         
}


void doSensorSetup(void)
{

  Serial.println("Dallas Temperature IC Setup");
  // Grab a count of devices on the wire
  int numberOfDevices = sensors.getDeviceCount();
  // locate devices on the bus
  Serial.print("Locating devices...");
  Serial.print("Found "); Serial.print(numberOfDevices, DEC); Serial.println(" devices.");

  // report parasite power requirements
  Serial.print("Parasite power is: "); 
  if (sensors.isParasitePowerMode()) Serial.println("ON");
  else Serial.println("OFF");
  
  // Loop through each device, print out address
  for(int i=0;i<numberOfDevices; i++)
  {
    // Search the wire for address
    if(sensors.getAddress(tempDeviceAddress, i))
  {
    Serial.print("Found device "); Serial.print(i, DEC);
    Serial.print(" with address: "); printAddress(tempDeviceAddress);
    Serial.println();
    
    Serial.print("Setting resolution to "); Serial.println(TEMPERATURE_PRECISION, DEC);
    // set the resolution to TEMPERATURE_PRECISION bit (Each Dallas/Maxim device is capable of several different resolutions)
    sensors.setResolution(tempDeviceAddress, TEMPERATURE_PRECISION);   
    Serial.print("Resolution actually set to: ");
    Serial.print(sensors.getResolution(tempDeviceAddress), DEC); 
    Serial.println();
  }else{
    Serial.print("Found ghost device at ");
    Serial.print(i, DEC);
    Serial.print(" but could not detect address. Check power and cabling");
  }
  }
}


// function to print a device address
void printAddress(DeviceAddress deviceAddress)
{
  for (uint8_t i = 0; i < 8; i++)
  {
    if (deviceAddress[i] < 16) Serial.print("0");
    Serial.print(deviceAddress[i], HEX);
  }
}
