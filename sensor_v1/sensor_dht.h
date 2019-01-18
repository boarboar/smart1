#ifndef _UMP_DHT_H_
#define _UMP_DHT_H_

#include <SimpleDHT.h>

#include "sensor_abst.h"

class SimpleDHT11_Ex : public SimpleDHT11 {
    // just to expose setPin
    public :
    inline SimpleDHT11_Ex() : SimpleDHT11() {;}
    inline void setPin(int16_t pin) { SimpleDHT11::setPin((int)pin); }
};


class SensDHT11 : public SensAbstract {
protected:
    SimpleDHT11_Ex _sens;
    struct SensData {
        int16_t t10;
        int16_t h10;
        } _tData;  
public:
    inline SensDHT11(int16_t pin=DATA_BUS_DEF) : _sens() {;}
    inline const char *describe() { return "DHT11"; }
    inline int16_t init() {_sens.setPin(_pin); return 0;}
    inline int16_t cfg() { return 0; }
    inline int16_t measure() {
        byte temperature = 0;
        byte humidity = 0;
        int err = SimpleDHTErrSuccess;
        if ((err = _sens.read(&temperature, &humidity, NULL)) != SimpleDHTErrSuccess) {
            Serial.print("Read DHT11 failed, err="); Serial.println(err);
            _tData.t10 = -1270;
            _tData.h10 = -1270;
            return -1;
        }

        _tData.t10 = (int16_t)temperature;
        _tData.h10 = (int16_t)humidity;

        Serial.print("Sample OK: ");
        Serial.print(_tData.t10); Serial.print(" *C, "); 
        Serial.print(_tData.h10); Serial.println(" H");
        return 0;
    }

    inline int16_t toJson(JsonObject &json) {
        json["T"] = _tData.t10;    
        json["H"] = _tData.h10;    
        return 0;
    }
};


#endif //_UMP_DHT_H_

