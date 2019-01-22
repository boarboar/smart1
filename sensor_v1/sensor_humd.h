#ifndef _UMP_HUMD_H_
#define _UMP_HUMD_H_

#include "sensor_abst.h"

class SensHUMD : public SensAbstract {
protected:
    int16_t _val;
public:
    inline SensHUMD(int16_t pin=DATA_BUS_DEF) : SensAbstract(pin), _val(-1) {}
    inline const char *describe() { return "HUMD"; }
    inline int16_t init() { pinMode(_pin, INPUT_PULLUP); return 0;}
    inline int16_t cfg() {return 0;}
    inline int16_t measure() { 
        _val=digitalRead(_pin); 
        Serial.print("HUMD: "); Serial.println(_val);         
        return 0;}
    inline int16_t toJson(JsonObject &json) { json["HD"] = _val; return 0;} 
};

#endif //_UMP_HUMD_H_