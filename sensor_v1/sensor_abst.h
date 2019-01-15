#ifndef _UMP_AS_H_
#define _UMP_AS_H_

class SensAbstract {
public:
  inline SensAbstract(int16_t pin) : _pin(pin) {}
  virtual int16_t init() = 0;
  virtual int16_t cfg() = 0;
  virtual int16_t measure() = 0;
  virtual int16_t toJson(JsonObject &json) = 0;
 protected:  
  int16_t _pin;
};

#endif //_UMP_AS_H_

