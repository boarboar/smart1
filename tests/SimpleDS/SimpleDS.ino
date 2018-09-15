// Include the libraries we need

#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
//#include <FS.h>
#include <OneWire.h>
#include <DallasTemperature.h>

#define SYS_ID  1
#define TEMPERATURE_PRECISION 9 // Lower resolution
#define BUF_SZ 255
#define SETUP_PIN 0

// do not work with parasite

// 5k res

// Data wire is plugged into port 2 on the Arduino
#define ONE_WIRE_BUS 2

// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature sensors(&oneWire);
DeviceAddress tempDeviceAddress;  

// move to cfg
const char* ssid = "NETGEAR";
const char* password = "boarboar";
const char* host = "192.168.1.100";  
const int   port = 9999;            

static bool isSetup = false;

struct TempData {
  uint8_t id;
  uint8_t make;
  uint8_t res;
  uint8_t isParasite;
  int16_t t10;
} tData = {SYS_ID, 0};

ADC_MODE(ADC_VCC);

/*
 * The setup function. We only start the sensors here
 */
void setup(void)
{
  // start serial port
  delay(100);  
  Serial.begin(115200);
  Serial.println();
  Serial.println(F("Press button1 now to enter setup"));
  delay(5000);  

    
  // read from flash - todo

  pinMode(SETUP_PIN, INPUT);  
  isSetup = false;  
  if(digitalRead(SETUP_PIN)==LOW) {
    delay(100);
    if(digitalRead(SETUP_PIN)==LOW) {
      isSetup = true;
    }
  }

  sensors.begin();

  if(isSetup) {
    doSetup();
  }
  else {
    // Start up the library
    delay(2000);
    doDiag(&tData);
  
    doConnect();
    doMeasure();
  }
}

/*
 * Main function, get and show the temperature
 */
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
 
  WiFi.begin(ssid, password);
  Serial.print(F("\nConnecting to ")); Serial.print(ssid);
  i = 0;
  while (WiFi.status() != WL_CONNECTED && i++ < 60) {delay(500); Serial.print(".");}
  Serial.println();
  if(i == 21){
    Serial.print(F("Could not connect to ")); Serial.println(ssid);
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
  if (!client.connect(host, port)) {
      Serial.println("connection failed");
      return false;
    }

  Serial.print("connected to ");
  Serial.print(host);
  Serial.print(":");
  Serial.println(port);


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

bool doSetup() {
  char buf[BUF_SZ];
  int16_t cnt=0;
  Serial.setTimeout(-1);
  Serial.println(F("=============SETUP MODE!"));
 
  cnt=readLine("Server IP", "0.0.0.0", buf, BUF_SZ);
  Serial.print(cnt);
  Serial.print(" ");
  Serial.println(buf);
// port
// id
// SSID
// pwd
  delay(2000);
  doSensorSetup();
  delay(5000);
  // request parametres...
  ESP.reset(); // to be replaced with deep sleep
  return true;
}



void doSensorSetup(void)
{

  Serial.println("Dallas Temperature IC Setup");

  // Grab a count of devices on the wire
  int numberOfDevices = sensors.getDeviceCount();
  
  // locate devices on the bus
  Serial.print("Locating devices...");
  
  Serial.print("Found ");
  Serial.print(numberOfDevices, DEC);
  Serial.println(" devices.");

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
    Serial.print("Found device ");
    Serial.print(i, DEC);
    Serial.print(" with address: ");
    printAddress(tempDeviceAddress);
    Serial.println();
    
    Serial.print("Setting resolution to ");
    Serial.println(TEMPERATURE_PRECISION, DEC);
    
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

int16_t readLine(const char *prompt, const char *initv, char *buf, int16_t sz) {
  boolean res=false;
  int16_t bytes=0;   
  buf[bytes]=0; 

  while (Serial.available() > 0)  Serial.read();
  if(prompt!=NULL) {
    Serial.print(prompt);
    if(initv!=NULL) {
      Serial.print("[");
      Serial.print(prompt);
      Serial.print("]");
    }
    Serial.print(":");
  }
  while (!res && bytes<sz) // 
  {
    while(!res && Serial.available()) 
    {
      buf[bytes] = Serial.read();
      //Serial.print(buf[bytes]);
      if (buf[bytes] == 10 || buf[bytes] == 13)
      {
        //if (bytes > 0) { 
          buf[bytes]=0;        
        //} 
        res=true; 
     }
      else
        bytes++;
    }    
    if(!res) yield();
  }

  
  
  if(bytes>=sz) { 
    //Serial.println("OVERFLOW");
    bytes=0; //overflow, probably caused hang up at start...    
    buf[bytes]=0; 
    //return -2;     
  }
  return bytes;
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
