//#include <Arduino.h>
#include <ArduinoJson.h>
//#include <ESP8266WiFi.h>
#include "FS.h"
#include "cfg.h"

#define MAX_CFG_LINE_SZ 80

const int NCFGS=4; 
const char *CFG_NAMES[NCFGS]={"DBG", "SYSL", "SPP_1", "SPP_2"};
enum CFG_ID {CFG_DBG=0, CFG_SYSL=1, CFG_PIDS_1=2, CFG_PIDS_2=3};

CfgDrv CfgDrv::Cfg; // singleton

CfgDrv::CfgDrv() : srv_port(0), id(0), fs_ok(false)
 {
   *srv_addr=0;
   *SSID=0;
   *PWD=0;
  }

int16_t CfgDrv::init() {
  fs_ok=SPIFFS.begin();
  if (!fs_ok) {
    Serial.println(F("Failed to mount FS"));
    return 0;
  }
  Serial.println(F("FS mounted"));
  return 1;
}

int16_t CfgDrv::load() {
  if (!fs_ok) return 0;
  
  char buf[MAX_CFG_LINE_SZ];
  uint32_t ms1=millis();
  File f = SPIFFS.open(szFileName, "r");
  if (!f) {
    Serial.println(F("Failed to open config file"));
    return 0;
  }
  size_t size = f.size();
  int c=0;

  while(c!=-1) { // while !EOF
    char *p = buf;
    while(1) { // new line
      c=f.read();
      //Serial.println((char)c);
      if(c==-1 || c=='\n'  || c=='\r') break;
      if(p-buf<MAX_CFG_LINE_SZ-1) *p++=c;       
    }
    if(p>buf) { //non-empty
      *p=0; 
      Serial.println(buf);  
      StaticJsonBuffer<200> jsonBuffer;
      JsonObject& json = jsonBuffer.parseObject(buf);  
      if (json.success()) {
        id = json["ID"];
        srv_port = json["PORT"];
        strncpy(srv_addr, json["ADDR"], MAX_ADDR_SZ);
        srv_addr[MAX_ADDR_SZ-1]=0;
        strncpy(SSID, json["SSID"], MAX_SSID_SZ);
        SSID[MAX_SSID_SZ-1]=0;
        strncpy(PWD, json["PWD"], MAX_PWD_SZ);
        PWD[MAX_PWD_SZ-1]=0;        
      }
    } // new line
  } // while !EOF

  uint16_t t=millis()-ms1;
  Serial.print(F("Cfg sz ")); Serial.print(size); Serial.print(F(", read in ")); Serial.println(t);
  Serial.print(F("ID=")); Serial.println(id); 
  Serial.print(F("SSID=")); Serial.println(SSID);
  Serial.print(F("PWD=")); Serial.println(PWD);
  Serial.print(F("ADDR=")); Serial.println(srv_addr); 
  Serial.print(F("PORT=")); Serial.println(srv_port); 

  return 1;
}

int16_t CfgDrv::store() {
  if (!fs_ok) return 0;
  uint32_t ms1=millis();
 
  File f = SPIFFS.open(szFileName, "w");
  if (!f) {
    Serial.println(F("Failed to open config file (w)"));
    return 0;
  }
 
  StaticJsonBuffer<400> jsonBuffer;
  JsonObject& json = jsonBuffer.createObject();
  //String addr = srv_addr.toString();
    
  json["SSID"]=SSID;
  json["PWD"]=PWD;
  json["ADDR"]=srv_addr;
  json["PORT"]=srv_port;
  json["ID"]=id;

  json.printTo(f);
  f.write('\n');
  f.close();
  uint16_t t=millis()-ms1;
  Serial.print(F("Cfg written in ")); Serial.println(t);
  return 1;
}

