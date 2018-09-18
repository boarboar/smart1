#ifndef _UMP_CFG_H_
#define _UMP_CFG_H_

#define MAX_SSID_SZ 16
#define MAX_PWD_SZ 16
#define MAX_ADDR_SZ 16


class CfgDrv {
public:
  static CfgDrv Cfg; // singleton
  int16_t init();  
  int16_t load();
  int16_t store();
  int16_t setup(); 
  bool validate();
 public: 
  char SSID[MAX_SSID_SZ];
  char PWD[MAX_PWD_SZ];
  char srv_addr[MAX_ADDR_SZ];
  //IPAddress srv_addr;
  uint16_t srv_port;
  int8_t id;
protected:  
  const char *szFileName = "/config.json";  
  CfgDrv();
  int16_t readLine(const char *prompt, const char *initv, char *buf, int16_t sz);
  int16_t readInt(const char *prompt, int initv);
  bool fs_ok;
};

#endif //_UMP_CFG_H_

