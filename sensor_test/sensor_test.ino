#include <ESP8266WiFi.h>

#define RTC_MAGIC 0xDE7B

struct {
  uint16_t magic;
  byte data[2];
} rtcData;

void setup() {

  const char *SSID="NETGEAR1";
  uint16_t CONN_COUNT=60;
  byte mac[6]; 
  uint8_t i = 0;
  uint8_t  seq=0;

  Serial.begin(115200);
  Serial.println("Start");
  
  WiFi.macAddress(mac);
  Serial.print(F("MAC: "));
  for(i=0; i<6; i++) {
    Serial.print(mac[i],HEX);
    if(i<5) Serial.print(F(":"));
  }

  Serial.println();
  
  //192.168.1.137
  //Subnet: 255.255.255.0
  //GW: 192.168.1.1

  if (ESP.rtcUserMemoryRead(0, (uint32_t*) &rtcData, sizeof(rtcData))) {
    Serial.print("Read RTC magic ");
    Serial.println(rtcData.magic);
    if(RTC_MAGIC==rtcData.magic && rtcData.data[0]>0) {
        seq = rtcData.data[1]; 
        Serial.print("Assuming ID digit ");
        Serial.println(rtcData.data[0]);
        Serial.print("Seq ");
        Serial.println(seq);
        IPAddress ip(192,168,1,rtcData.data[0]); 
        IPAddress subnet(255,255,255,0);
        IPAddress gw(192,168,1,1);  
        WiFi.config(ip, subnet, gw);

    }
  }
 
  WiFi.begin(SSID, "boarboar");
  Serial.print(F("\nConnecting to ")); Serial.print(SSID);
  i = 0;
  while (WiFi.status() != WL_CONNECTED && i++ < CONN_COUNT) {delay(500); Serial.print(".");}
  Serial.println();
  if(i == CONN_COUNT){
    Serial.print(F("Could not connect to ")); Serial.println(SSID);

    rtcData.magic = RTC_MAGIC;
    rtcData.data[0] = 0;

    if (ESP.rtcUserMemoryWrite(0, (uint32_t*) &rtcData, sizeof(rtcData))) {
      Serial.println("Write rtc");
    }
    return;
  }

  Serial.print("Connected, IP: ");
  Serial.println(WiFi.localIP());
  Serial.print("Subnet: ");
  Serial.println(WiFi.subnetMask());
  Serial.print("GW: ");
  Serial.println(WiFi.gatewayIP());

  Serial.print("IP last: ");
  Serial.println(WiFi.localIP()[3]);

  rtcData.magic = RTC_MAGIC;
  rtcData.data[0] = WiFi.localIP()[3];
  rtcData.data[1] = seq+1;
  if (ESP.rtcUserMemoryWrite(0, (uint32_t*) &rtcData, sizeof(rtcData))) {
    Serial.println("Write rtc");
  }
  Serial.println("Going deep sleep");
  ESP.deepSleep(30000000L);  // 30 sec
    
}

void loop() {
  // put your main code here, to run repeatedly:

}
