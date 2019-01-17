#ifndef _UMP_DS_H_
#define _UMP_DS_H_

#include <OneWire.h>
#include <DallasTemperature.h>

#include "sensor_abst.h"

#define TEMPERATURE_PRECISION 9 // Lower resolution

class SensDS : public SensAbstract {
protected:
    OneWire oneWire;
    DallasTemperature sensors;
    DeviceAddress tempDeviceAddress;    
    struct SensData {
        uint8_t make;
        uint8_t res;
        uint8_t isParasite;
        int16_t t10;
        } _tData;  
    static void printAddress(DeviceAddress deviceAddress);    
public:
    SensDS(int16_t pin=DATA_BUS_DEF);
    inline const char *describe() { return "DS18"; }
    int16_t init();
    int16_t cfg();
    int16_t measure();
    int16_t toJson(JsonObject &json);
};

#endif //_UMP_DS_H_

