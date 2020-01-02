// Include the libraries we need

#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h>
#include "cfg.h"

#include "sensor_ds.h"

//        val TCP_PORT = 9999
//        val UDP_PORT = 9998
        
#define BUF_SZ 255
#define SETUP_PIN 0
#define LED_PIN 12

#define DATA_BUS_1 13
#define DATA_BUS_2 14

#define TRIES_TO_SEND 1

#define POWER_PIN 12 // vcc supply for the sensors_cfg

#define RTC_MAGIC 0xDE7B13B7

struct {
  uint32_t magic; // should be 4byte aligned
  byte data[4];
} rtcData = {0};

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
  bool isCfgFromRTC = false;
  //bool isConnectOK = false;
  byte ip_digit = 0; 

  pinMode(LED_PIN, OUTPUT);

  pinMode(POWER_PIN, OUTPUT);

  blink(20, 1);

  Serial.begin(115200);
  Serial.println();

  if (ESP.rtcUserMemoryRead(0, (uint32_t*) &rtcData, sizeof(rtcData))) {   
    Serial.println("Read RTC "); 
    if(RTC_MAGIC==rtcData.magic && rtcData.data[0]>0) {
        //seq = rtcData.data[1]; 
        Serial.print("Assuming address from RTC: ");
        ip_digit = rtcData.data[0];
        Serial.println(ip_digit);
        //Serial.print("Seq ");
        //Serial.println(seq);
        IPAddress ip(192,168,1,ip_digit); 
        IPAddress subnet(255,255,255,0);
        IPAddress gw(192,168,1,1);  
        WiFi.config(ip, subnet, gw);
        //isCfgFromRTC = true;
    } else {
      rtcData.data[0]=0;
    }
  } else {
    Serial.println("Failed to read RTC ");
    rtcData.data[0]=0;
  }
  

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
    delay(1000);  
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
  
  doBeginConnect();
  
  digitalWrite(POWER_PIN, HIGH); 
  delay(50);    
  CfgDrv::Cfg.sensors_cfg(ports);
  CfgDrv::Cfg.sensors_init();
    
  Serial.println(F("Measuring..."));

  if(CfgDrv::Cfg.sensors_measure()) {
    Serial.println(F("Bad meas!"));
    blink(50, 4);
  }

  digitalWrite(POWER_PIN, LOW); 
    
  Serial.print(F("Getting vcc..."));
  tData.id = CfgDrv::Cfg.id;
  tData.vcc = ESP.getVcc();
  tData.magic = 37;
  Serial.println(tData.vcc);

  if(doWaitForConnect()) {
    ip_digit = WiFi.localIP()[3];
    delay(50);
    yield();
    for(int i=0; i<TRIES_TO_SEND; i++) {
      bool sendRes = CfgDrv::Cfg.conn_type == CfgDrv::CONN_TCP ? doSend(&tData) : doSendUdp(&tData);
      if(sendRes) rtcData.data[1]=0;
      else rtcData.data[1]++;
      delay(50);
      yield();
      //delay(50);
      if(!sendRes) {
        Serial.println(F("Failed to send"));
        blink(100, 2);
        if(i<TRIES_TO_SEND-1) delay(1000+i*1000);
      } else break;
    }
  } else {
    blink(50, 3);   
    ip_digit=0;
  }

  if(ip_digit != rtcData.data[0]) {    
    rtcData.magic = RTC_MAGIC;
    rtcData.data[0] = ip_digit;
    //rtcData.data[1] = seq+1;
    Serial.print("Writing to rtc: "); Serial.println(rtcData.data[0]);
    if (0==ESP.rtcUserMemoryWrite(0, (uint32_t*) &rtcData, sizeof(rtcData))) {
      Serial.println("failed to write rtc");
    }
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

void doBeginConnect()
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
  Serial.println();

  WiFi.begin(CfgDrv::Cfg.SSID, CfgDrv::Cfg.PWD);
}

bool doWaitForConnect()
{
  const int CONN_COUNT = 60;
  uint8_t i;
  Serial.println(F("\nConnecting to ")); Serial.print(CfgDrv::Cfg.SSID);
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
  client.setTimeout(5000); //5s
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
  rootOut["F"] = rtcData.data[1];
  
  CfgDrv::Cfg.sensors_tojson(rootOut);
  
  rootOut.printTo(bufout, BUF_SZ-1);
 
  unsigned long timeout = millis();
 
  client.print(bufout);
  
  Serial.print("Sent in "); 
  Serial.print(millis() - timeout);
  Serial.println("ms"); 
  delay(50);
  yield();
  client.stop(); 
  return true;         
}

bool doSendUdp(TempData *pData) {
  
  WiFiUDP udp_snd;
  IPAddress addr;
  int res = 0;

  char bufout[BUF_SZ];
  StaticJsonBuffer<448> jsonBufferOut;
  JsonObject& rootOut = jsonBufferOut.createObject();
  rootOut["I"] = pData->id;
  rootOut["V"] = pData->vcc;
  rootOut["Y"] = pData->magic;
  
  CfgDrv::Cfg.sensors_tojson(rootOut);
  
  rootOut.printTo(bufout, BUF_SZ-1);

  unsigned long timeout = millis();
  if(!addr.fromString(CfgDrv::Cfg.srv_addr)) {
    Serial.println("bad addr");
    return false;
  }
 
  if(!udp_snd.beginPacket(addr, CfgDrv::Cfg.srv_port)) {
    Serial.println("UDP begin packet failed");
    return false;
  }
  if(strlen(bufout) != udp_snd.write(bufout, strlen(bufout))) {
    Serial.println("UDP write packet failed");
    return false;
  }
  res = udp_snd.endPacket();

  if(res) {
    Serial.print("UDP Sent in "); 
    Serial.print(millis() - timeout);
    Serial.print("ms  to ");
  } else {
    Serial.print("UDP failed to send to ");
  }
  Serial.print(addr);
  Serial.print(":");
  Serial.println(CfgDrv::Cfg.srv_port);

  yield();
  delay(200);
  yield();
  //delay(200);
  //yield();

  return res;         
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

static void udp_sent_callback(void *arg) 
{
 Serial.println("UDP sent CB");
}
