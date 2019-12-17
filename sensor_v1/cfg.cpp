//#include <Arduino.h>
#include <ArduinoJson.h>
//#include <ESP8266WiFi.h>
#include "FS.h"
#include "sensor_ds.h"
#include "sensor_humd.h"
#include "sensor_dht.h"

#include "cfg.h"

#define MAX_CFG_LINE_SZ 120

static const char *sthelp = "0-NONE, 1-DS18, 2-DHT11, 3 - HUMD";
  
/*
{"SSID":"","PWD":"","ADDR":"192.168.1.149","PORT":9999,"ID":1}
Cfg sz 78, read in 5
ID=1
SSID=
PWD=
ADDR=192.168.1.149
PORT=9999
Cfg loaded
Invalid config, force setup!
*/

//const int NCFGS=4; 

static SensDS sensor_ds;
static SensHUMD sensor_humd;
static SensDHT11 sensor_dht11;

CfgDrv CfgDrv::Cfg; // singleton

CfgDrv::CfgDrv() : srv_port(0), id(0), sleep_min(1), conn_type(CONN_TCP), fs_ok(false)
 {   
   *srv_addr=0;
   *SSID=0;
   *PWD=0;
   for(int i=0; i<MAX_SENS; i++) sensors[i]=SENSOR_NONE;
  }

void CfgDrv::print() {
  Serial.print(F("ID=")); Serial.println(id); 
  Serial.print(F("SSID=")); Serial.println(SSID);
  Serial.print(F("PWD=")); Serial.println(PWD);
  Serial.print(F("ADDR=")); Serial.println(srv_addr); 
  Serial.print(F("PORT=")); Serial.println(srv_port); 
  Serial.print(F("SLP=")); Serial.println(sleep_min); 
  Serial.print(F("CONN=")); Serial.println(conn_type == CONN_TCP ? "tcp" : "udp"); 
  for(int i=0; i<MAX_SENS; i++) {
    Serial.print(pszSensKeys[i]); Serial.print(F("="));
    Serial.println(sensors[i]);
  }
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
        conn_type = json["CONN"];
        ps=json["ADDR"];
        if(ps!=NULL) {
          strncpy(srv_addr, ps, MAX_ADDR_SZ);
          srv_addr[MAX_ADDR_SZ-1]=0;
        }
        ps=json["SSID"];
        if(ps!=NULL) {
          strncpy(SSID, ps, MAX_SSID_SZ);
          SSID[MAX_SSID_SZ-1]=0;
        }
        ps=json["PWD"];
        if(ps!=NULL) {
         strncpy(PWD, ps, MAX_PWD_SZ);
         PWD[MAX_PWD_SZ-1]=0;
        }     
       for(int i=0; i<MAX_SENS; i++) {          
          sensors[i] = (SensorTypes)((int)(json[pszSensKeys[i]]));
        }
      }
    } // new line
  } // while !EOF

  uint16_t t=millis()-ms1;
  Serial.print(F("Cfg sz ")); Serial.print(size); Serial.print(F(", read in ")); Serial.println(t);
  print();
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
  json["CONN"]=conn_type;
  for(int i=0; i<MAX_SENS; i++) {          
    json[pszSensKeys[i]] = (int)sensors[i];
  }        

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
  Serial.setTimeout(-1);
  Serial.println(F("=============SETUP MODE!"));
  readLine1("SSID", SSID, buf, SSID, MAX_SSID_SZ);
  readLine1("PWD", PWD, buf, PWD, MAX_PWD_SZ);
  readLine1("Server IP", srv_addr, buf, srv_addr, MAX_ADDR_SZ);
  srv_port = readInt("Server port", srv_port, true); 
  id = readInt("Sensor ID", id, true);
  sleep_min = readInt("Sleep (min)", sleep_min, true);
  conn_type = readInt("Conn type (0-tcp, 1-udp)", sleep_min, false);
  Serial.print(F("Sensors setup, "));
  Serial.println(sthelp);
  for(int i=0; i<MAX_SENS; i++) {     
    bool succ = false;
    while(!succ) {
      int16_t sens = readInt(pszSensKeys[i], sensors[i], false);
      if(sens>=SENSOR_NONE && sens<SENSOR_INV) {
        sensors[i] = (SensorTypes)sens;
        succ = true;
      }
    }
  }
  print();
  return 1;
}

bool CfgDrv::validate() {
  if(!*SSID || !*PWD || !*srv_addr || !srv_port || !id || !sleep_min) return false;
  return true;
}

/*
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
*/ 

void CfgDrv::readLine1(const char *prompt, const char *initv, char *buf, char *dst, int16_t dstsz) {
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

  while (!res && bytes<dstsz-1) // 
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
  buf[bytes]=0; 

  if(!bytes) return;
    
  strncpy(dst, buf, dstsz);
  dst[dstsz-1]=0;
  
}  

int16_t CfgDrv::readInt(const char *prompt, int initv, bool nonzero) {
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
  int16_t res = 0;
  do {
    res = Serial.parseInt();
    if(res==0 && nonzero) res=initv;
    //Serial.println(":");
  } while(res==0 && nonzero);
  Serial.println(res);
  return res;
}

int16_t CfgDrv::sensors_cfg(int16_t ports[MAX_SENS]) {
  for(int i=0; i<MAX_SENS; i++) {
    switch(sensors[i]) {
      case SENSOR_DS18 : sensors_inst[i] = &sensor_ds; break;
      case SENSOR_DHT11 : sensors_inst[i] = &sensor_dht11;  break;
      case SENSOR_HUMD : sensors_inst[i] = &sensor_humd; break;
      default: sensors_inst[i] = NULL;
    }
    Serial.print(F("PORT(")); Serial.print(ports[i]); Serial.print(F(")->"));
    if(sensors_inst[i] != NULL)  {
      sensors_inst[i]->setPin(ports[i]);
      Serial.println(sensors_inst[i]->describe());      
    } else Serial.println();

    //Serial.print(pszSensKeys[i]); Serial.print(F("="));
    //Serial.println(sensors[i]);
  } 
  return 0; 
}

int16_t CfgDrv::sensors_init() {
  for(int i=0; i<MAX_SENS; i++) 
    if(sensors_inst[i] != NULL) sensors_inst[i]->init();
  return 0;  
}

int16_t CfgDrv::sensors_setup() {
  for(int i=0; i<MAX_SENS; i++) 
    if(sensors_inst[i] != NULL) sensors_inst[i]->cfg();
  return 0;  
}

int16_t CfgDrv::sensors_measure() {
  int16_t rc=0, rci;
  for(int i=0; i<MAX_SENS; i++)
    if(sensors_inst[i] != NULL) {
      rci=sensors_inst[i]->measure();
      if(rci) rc=rci;
    }
  return rc;  
}

int16_t CfgDrv::sensors_tojson(JsonObject &json) {
  for(int i=0; i<MAX_SENS; i++) 
    if(sensors_inst[i] != NULL) sensors_inst[i]->toJson(json);

  return 0;  
}
