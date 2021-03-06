#ifndef _UMP_CFG_H_
#define _UMP_CFG_H_

#define MAX_SSID_SZ 16
#define MAX_PWD_SZ 16
#define MAX_ADDR_SZ 16

//#define MAX_SENS  2

class SensAbstract;

class CfgDrv {
public:
  static const int MAX_SENS=2;
  enum SensorTypes { SENSOR_NONE=0, SENSOR_DS18=1, SENSOR_DHT11=2, SENSOR_HUMD=3, SENSOR_INV=4};
  enum ConnTypes { CONN_TCP=0, CONN_UDP=1};

  static CfgDrv Cfg; // singleton
  int16_t init();  
  int16_t load();
  int16_t store();
  int16_t setup(); 
  int16_t sensors_cfg(int16_t ports[MAX_SENS]);
  int16_t sensors_init();
  int16_t sensors_setup();
  int16_t sensors_measure();
  int16_t sensors_tojson(JsonObject &json);

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
  uint8_t conn_type;
  
protected:  

  const char *szFileName = "/config.json"; 
  const char *pszSensKeys[MAX_SENS] = {"S1", "S2"};
  CfgDrv();
  //int16_t readLine(const char *prompt, const char *initv, char *buf, int16_t sz);
  void readLine1(const char *prompt, const char *initv, char *buf, char *dst, int16_t dstsz);
  int16_t readInt(const char *prompt, int init, bool nonzero=false);
  bool fs_ok;

  SensorTypes sensors[MAX_SENS];
  SensAbstract* sensors_inst[MAX_SENS];
};

#endif //_UMP_CFG_H_
