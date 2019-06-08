// Include the libraries we need

#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include "cfg.h"

#include "sensor_ds.h"

#define BUF_SZ 255
#define SETUP_PIN 0
#define LED_PIN 12

#define DATA_BUS_1 13
#define DATA_BUS_2 14

#define TRIES_TO_SEND 1

#define POWER_PIN 12 // vcc supply for the sensors_cfg


static int16_t ports[CfgDrv::MAX_SENS] = {DATA_BUS_1, DATA_BUS_2};

struct TempData {
  uint8_t id;
  uint16_t vcc;
  uint8_t magic;
} tData = {0};


ADC_MODE(ADC_VCC);

void setup(void)
{
  bool isSetup = false;
  bool isCfgValid = false;

  pinMode(LED_PIN, OUTPUT);

  pinMode(POWER_PIN, OUTPUT);

  blink(20, 1);

  Serial.begin(115200);
  Serial.println();

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
    delay(2000);  
    pinMode(SETUP_PIN, INPUT);  
    isSetup = false;  
    if(digitalRead(SETUP_PIN)==LOW) {
      delay(100);
      if(digitalRead(SETUP_PIN)==LOW) {
        isSetup = true;
      }
    }
   }

  if(isSetup || !isCfgValid) {
    digitalWrite(LED_PIN, HIGH); 
    do {
      CfgDrv::Cfg.setup();
      isCfgValid = CfgDrv::Cfg.validate();  
      Serial.print(F("VALID:"));    
      Serial.println(isCfgValid);
    } while (!isCfgValid);
    CfgDrv::Cfg.store();
    delay(1000);
    Serial.println(F("Cfg OK"));
    delay(1000);
    digitalWrite(LED_PIN, LOW); 
    digitalWrite(POWER_PIN, HIGH);
    delay(100); 
    CfgDrv::Cfg.sensors_cfg(ports);
    CfgDrv::Cfg.sensors_init();
    CfgDrv::Cfg.sensors_setup();
    digitalWrite(POWER_PIN, LOW); 
    Serial.println(F("Restarting..."));
    ESP.deepSleep(1000000L);
  } 
  
  digitalWrite(POWER_PIN, HIGH); 
  delay(50);    
  CfgDrv::Cfg.sensors_cfg(ports);
  CfgDrv::Cfg.sensors_init();
    
  
  Serial.println(F("Measuring..."));


  if(CfgDrv::Cfg.sensors_measure()) {
    Serial.println(F("Bad meas!"));
    blink(100, 4);
  }

  digitalWrite(POWER_PIN, LOW); 
    
  Serial.print(F("Getting vcc..."));
  tData.id = CfgDrv::Cfg.id;
  tData.vcc = ESP.getVcc();
  tData.magic = 37;
  Serial.println(tData.vcc);

  if(doConnect()) {
    for(int i=0; i<TRIES_TO_SEND; i++) {
      if(!doSend(&tData)) {
        Serial.println(F("Failed to send"));
        blink(100, 2);
        if(i<TRIES_TO_SEND-1) delay(1000+i*1000);
      } else break;
    }
  } else {
    blink(100, 3);
  }

  Serial.print(F("deep sleep for "));
  Serial.print(CfgDrv::Cfg.sleep_min);
  Serial.print(F(" min"));

  blink(20, 1);

  ESP.deepSleep(60000000L*CfgDrv::Cfg.sleep_min);

}

void loop(void)
{ 
  delay(5000);
  /*
  tData.t10 = doMeasure(); 
  doSend(&tData);
  */
}

bool doConnect()
{
  const int CONN_COUNT = 60;
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
  while (WiFi.status() != WL_CONNECTED && i++ < CONN_COUNT) {delay(500); Serial.print(".");}
  Serial.println();
  if(i == CONN_COUNT){
    Serial.print(F("Could not connect to ")); Serial.println(CfgDrv::Cfg.SSID);
    return false;
  }

  Serial.print("Connected, IP: ");
  Serial.println(WiFi.localIP());
  
  return true;
} 

bool doSend(TempData *pData) {
  WiFiClient client;
  client.setTimeout(10000); //10s
  if (!client.connect(CfgDrv::Cfg.srv_addr, CfgDrv::Cfg.srv_port)) {
      Serial.println("connection failed");
      return false;
    }

  Serial.print("connected to ");
  Serial.print(CfgDrv::Cfg.srv_addr);
  Serial.print(":");
  Serial.println(CfgDrv::Cfg.srv_port);


  char bufout[BUF_SZ];
  StaticJsonBuffer<448> jsonBufferOut;
  JsonObject& rootOut = jsonBufferOut.createObject();
  rootOut["I"] = pData->id;
  rootOut["V"] = pData->vcc;
  rootOut["Y"] = pData->magic;
  
  CfgDrv::Cfg.sensors_tojson(rootOut);
  
  rootOut.printTo(bufout, BUF_SZ-1);
  client.print(bufout);
  
  unsigned long timeout = millis();
  Serial.print("Sent in "); 
  Serial.print(millis() - timeout);
  Serial.println("ms"); 
  delay(50);
  client.stop(); 
  return true;         
}

void blink(uint16_t dly, uint16_t n)
{
  for(uint16_t i=0; i<n; i++) {
    if(i) delay(dly);
    digitalWrite(LED_PIN, HIGH); 
    delay(dly); 
    digitalWrite(LED_PIN, LOW);
  }
}

/*
void doSensorInit(void)
{
  for(int i=0; i<CfgDrv::Cfg.MAX_SENS; i++)
    if(CfgDrv::Cfg.sensors_inst[i]) CfgDrv::Cfg.sensors_inst[i]->init();
}

void doSensorSetup(void)
{
  for(int i=0; i<CfgDrv::Cfg.MAX_SENS; i++)
    if(CfgDrv::Cfg.sensors_inst[i]) CfgDrv::Cfg.sensors_inst[i]->cfg();
}

int16_t doSensorMeasure()
{
  int16_t rc=0, rci;
  for(int i=0; i<CfgDrv::Cfg.MAX_SENS; i++)
    if(CfgDrv::Cfg.sensors_inst[i]) {
      rci=CfgDrv::Cfg.sensors_inst[i]->measure();
      if(rci) rc=rci;
    }
  return rc;  
}

void doSensorJson(JsonObject &json)
{
  for(int i=0; i<CfgDrv::Cfg.MAX_SENS; i++)
    if(CfgDrv::Cfg.sensors_inst[i]) CfgDrv::Cfg.sensors_inst[i]->toJson(json);
}
*/
