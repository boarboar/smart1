//#include <Arduino.h>
#include <ArduinoJson.h>
//#include <ESP8266WiFi.h>
#include "FS.h"
#include "cfg.h"

#define MAX_CFG_LINE_SZ 120

/*
{"SSID":"NETGEAR","PWD":"boarboar","ADDR":"192.168.1.149","PORT":9999,"ID":1}
Cfg sz 78, read in 5
ID=1
SSID=
PWD=
ADDR=192.168.1.149
PORT=9999
Cfg loaded
Invalid config, force setup!
*/

const int NCFGS=4; 

CfgDrv CfgDrv::Cfg; // singleton

CfgDrv::CfgDrv() : srv_port(0), id(0), sleep_min(1), fs_ok(false)
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
      const char *ps;
      StaticJsonBuffer<400> jsonBuffer;
      JsonObject& json = jsonBuffer.parseObject(buf);  
      if (json.success()) {
        id = json["ID"];
        srv_port = json["PORT"];
        sleep_min = json["SLP"];
        ps=json["ADDR"];
        if(ps!=NULL) {
          strncpy(srv_addr, ps, MAX_ADDR_SZ);
          srv_addr[MAX_ADDR_SZ-1]=0;
        }
       // Serial.println("check SSID");        
        ps=json["SSID"];
        if(ps!=NULL) {
          //Serial.print("SSID not NULL: ");
          //Serial.println(ps);
          strncpy(SSID, ps, MAX_SSID_SZ);
          SSID[MAX_SSID_SZ-1]=0;
        }
         //Serial.println("check PWD"); 
        ps=json["PWD"];
        if(ps!=NULL) {
          //Serial.print("PWD not NULL: ");
          //Serial.println(ps);
         strncpy(PWD, ps, MAX_PWD_SZ);
         PWD[MAX_PWD_SZ-1]=0;
        }        
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
  Serial.print(F("SLP=")); Serial.println(sleep_min); 
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
  json["SLP"]=sleep_min;

  json.printTo(f);
  f.write('\n');
  f.close();
  uint16_t t=millis()-ms1;
  Serial.print(F("Cfg written in ")); Serial.println(t);
  return 1;
}

int16_t CfgDrv::setup() 
{
  char buf[MAX_CFG_LINE_SZ];
  int16_t cnt=0;
  Serial.setTimeout(-1);
  Serial.println(F("=============SETUP MODE!"));
 
  cnt=readLine("SSID", SSID, buf, MAX_CFG_LINE_SZ);
  if(cnt>0) {
    // TODO: add validation
    strncpy(SSID, buf, MAX_SSID_SZ);
    SSID[MAX_SSID_SZ-1]=0;
  }
  Serial.println(SSID);
  cnt=readLine("PWD", PWD, buf, MAX_CFG_LINE_SZ);
  if(cnt>0) {
    // TODO: add validation
    strncpy(PWD, buf, MAX_PWD_SZ);
    PWD[MAX_PWD_SZ-1]=0;
  }
  Serial.println(PWD);
  cnt=readLine("Server IP", srv_addr, buf, MAX_CFG_LINE_SZ);
  if(cnt>0) {
    // TODO: add validation
    strncpy(srv_addr, buf, MAX_ADDR_SZ);
    srv_addr[MAX_ADDR_SZ-1]=0;
  }
  Serial.println(srv_addr);
  cnt=readInt("Server port", srv_port);
  if(cnt>0) {
    // TODO: add validation
    srv_port=cnt;
  }
  Serial.println(srv_port);
  cnt=readInt("Sensor ID", id);
  if(cnt>0) {
    // TODO: add validation
    id=cnt;
  }  
  Serial.println(id);
  cnt=readInt("Sleep (min)", sleep_min);
  if(cnt>0) {
    // TODO: add validation
    sleep_min=cnt;
  }  
  Serial.println(sleep_min);
  return 1;
}

bool CfgDrv::validate() {
  if(!*SSID || !*PWD || !*srv_addr || !srv_port || !id || !sleep_min) return false;
  return true;
}


int16_t CfgDrv::readLine(const char *prompt, const char *initv, char *buf, int16_t sz) {
  boolean res=false;
  boolean leadingspace=true;
  int16_t bytes=0;   
  buf[bytes]=0; 

  while (Serial.available() > 0)  Serial.read();
  if(prompt!=NULL) {
    Serial.print(prompt);
    if(initv!=NULL) {
      Serial.print("[");
      Serial.print(initv);
      Serial.print("]");
    }
    Serial.print(":");
  }
  while (!res && bytes<sz) // 
  {
    while(!res && Serial.available()) 
    {
      buf[bytes] = Serial.read();
      if (buf[bytes] == 10 || buf[bytes] == 13)
      {
        buf[bytes]=0;        
        res=true; 
     }
      else 
        if(leadingspace) {
          if(!isspace(buf[bytes])) leadingspace=false;
        }
        if(!leadingspace) bytes++;
    }    
    if(!res) yield();
  }
 
  if(bytes>=sz) { 
    //Serial.println("OVERFLOW");
    bytes=0; //overflow, probably caused hang up at start...    
    buf[bytes]=0; 
  }
  return bytes;
}  

int16_t CfgDrv::readInt(const char *prompt, int initv) {
  while (Serial.available() > 0)  Serial.read();
  if(prompt!=NULL) {
    Serial.print(prompt);
    if(initv!=0) {
      Serial.print("[");
      Serial.print(initv);
      Serial.print("]");
    }
    Serial.print(":");
  }
  return Serial.parseInt();
}

