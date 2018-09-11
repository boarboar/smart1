// Include the libraries we need

#include <ESP8266WiFi.h>

#include <OneWire.h>
#include <DallasTemperature.h>

#define SETUP_PIN 0

// do not work with parasite

// 5k res

// Data wire is plugged into port 2 on the Arduino
#define ONE_WIRE_BUS 2

// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature sensors(&oneWire);


const char* ssid = "NETGEAR";
const char* password = "boarboar";
const char* host = "192.168.1.122";  
const int   port = 9999;            

/*
 * The setup function. We only start the sensors here
 */
void setup(void)
{
  // start serial port
  delay(2000);  
  Serial.begin(115200);
  
  Serial.println("Dallas Temperature IC Control Library Demo");

  pinMode(SETUP_PIN, INPUT);
  if(digitalRead(SETUP_PIN)==LOW) {
    delay(100);
    if(digitalRead(SETUP_PIN)==LOW) {
      Serial.println("SETUP MODE!");
    }
  }
  
  doConnect();

  // Start up the library
  sensors.begin();
  delay(2000);
  doDiag();
  doMeasure();
}

/*
 * Main function, get and show the temperature
 */
void loop(void)
{ 
  delay(5000);
  doSend(doMeasure());
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
  while (WiFi.status() != WL_CONNECTED && i++ < 40) {delay(500); Serial.print(".");}
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

bool doMeasure()
{
  //Serial.print("Requesting temperatures...");
  //sensors.requestTemperatures(); // Send the command to get temperatures
  // After we got the temperatures, we can print them here.
  // We use the function ByIndex, and as an example get the temperature from the first sensor only.
  float t=sensors.getTempCByIndex(0);
  Serial.print("Temperature for the device 1 (index 0) is: ");
  Serial.println(t);    
  //DEVICE_DISCONNECTED_C;
  //DEVICE_DISCONNECTED_RAW
  //#define DEVICE_DISCONNECTED_C -127
  //#define DEVICE_DISCONNECTED_RAW -7040
  
  return t;
}

bool doDiag()
{
  static DeviceAddress tempDeviceAddress;
  // call sensors.requestTemperatures() to issue a global temperature 
  // request to all devices on the bus

  // diagnostics
  
  if(!sensors.getAddress(tempDeviceAddress, 0)) {
    Serial.println("Failed to get device"); 
    return false;
  }

  Serial.print("Parasite: "); 
  if (sensors.isParasitePowerMode()) Serial.println("ON");
  else Serial.print("OFF");
  Serial.print("  Res: ");
  Serial.print(sensors.getResolution(tempDeviceAddress), DEC); 
  Serial.print("  Mod: ");
  Serial.print(tempDeviceAddress[0], HEX); 
 
  return true;
}

bool doSend(float t) {
    WiFiClient client;
    if (!client.connect(host, port)) {
      Serial.println("connection failed");
      return false;
    }

    Serial.print("connected to ");
    Serial.print(host);
    Serial.print(":");
    Serial.println(port);
    
    client.print("Hello");
    unsigned long timeout = millis();
    while (client.available() == 0) {
      if (millis() - timeout > 5000) {
        Serial.println(">>> Client Timeout !");
        client.stop();
        return false;
      }
      delay(10);
    }
   Serial.print("Sent in "); 
   Serial.print(millis() - timeout);
   Serial.println("ms"); 
   client.stop();
   return true;         
}


