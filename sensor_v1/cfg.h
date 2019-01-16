#ifndef _UMP_CFG_H_
#define _UMP_CFG_H_

#define MAX_SSID_SZ 16
#define MAX_PWD_SZ 16
#define MAX_ADDR_SZ 16

#define MAX_SENS  2

class CfgDrv {
public:
  enum SensorTypes { SENSOR_NONE=0, SENSOR_DS18=1, SENSOR_DHT11=2, SENSOR_HUMD=3, SENSOR_INV=4};

  static CfgDrv Cfg; // singleton
  int16_t init();  
  int16_t load();
  int16_t store();
  int16_t setup(); 
  bool validate();
  void print();
 public: 
  char SSID[MAX_SSID_SZ];
  char PWD[MAX_PWD_SZ];
  char srv_addr[MAX_ADDR_SZ];
  //IPAddress srv_addr;
  uint16_t srv_port;
  uint8_t id;
  uint8_t sleep_min;
  SensorTypes sensors[MAX_SENS];
protected:  
  const char *szFileName = "/config.json"; 
  const char *pszSensKeys[MAX_SENS] = {"S1", "S2"};
  CfgDrv();
  //int16_t readLine(const char *prompt, const char *initv, char *buf, int16_t sz);
  void readLine1(const char *prompt, const char *initv, char *buf, char *dst, int16_t dstsz);
  int16_t readInt(const char *prompt, int init, bool nonzero=false);
  bool fs_ok;
};

#endif //_UMP_CFG_H_

