#include <ArduinoJson.h>

#include "sensor_ds.h"


SensDS::SensDS(int16_t pin) :
    SensAbstract(pin), 
    oneWire(pin), 
    sensors(&oneWire), _tData{0}
{
}

int16_t SensDS::init() {
    oneWire.begin(_pin);
    sensors.begin();
    return 0;
}

int16_t SensDS::measure() {
    // diagnostics
  
  
    if(!sensors.getAddress(tempDeviceAddress, 0)) {
        Serial.println("Failed to get device"); 
        return -1;
    }

    _tData.isParasite = sensors.isParasitePowerMode();
    _tData.res = sensors.getResolution(tempDeviceAddress);
    _tData.make = tempDeviceAddress[0];
  
    Serial.print("Parasite: "); Serial.print(_tData.isParasite); 
    Serial.print("  Res: "); Serial.print(_tData.res); 
    Serial.print("  Make: "); Serial.println(_tData.make, HEX); 

    sensors.requestTemperatures(); // Send the command to get temperatures
    // After we got the temperatures, we can print them here.
    // We use the function ByIndex, and as an example get the temperature from the first sensor only.
    float t=sensors.getTempCByIndex(0);
    Serial.print("Temperature is: ");
    Serial.println(t);    
    //#define DEVICE_DISCONNECTED_C -127
  
    _tData.t10 = (int16_t)(t*10);

    if(_tData.t10==-1270) {
        Serial.println(F("Bad temp!"));
        return -2;
    }
    return 0;
}

int16_t SensDS::toJson(JsonObject &json) {
    json["M"] = _tData.make;
    json["P"] = _tData.isParasite;
    json["R"] = _tData.res;
    json["T"] = _tData.t10;    
    return 0;
}

int16_t SensDS::cfg() {
     Serial.println("Dallas Temperature IC Setup");
  // Grab a count of devices on the wire
  int numberOfDevices = sensors.getDeviceCount();
  // locate devices on the bus
  Serial.print("Locating devices...");
  Serial.print("Found "); Serial.print(numberOfDevices, DEC); Serial.println(" devices.");

  // report parasite power requirements
  Serial.print("Parasite power is: "); 
  if (sensors.isParasitePowerMode()) Serial.println("ON");
  else Serial.println("OFF");
  
  // Loop through each device, print out address
  for(int i=0;i<numberOfDevices; i++)
  {
    // Search the wire for address
    if(sensors.getAddress(tempDeviceAddress, i))
  {
    Serial.print("Found device "); Serial.print(i, DEC);
    Serial.print(" with address: "); printAddress(tempDeviceAddress);
    Serial.println();
    
    Serial.print("Setting resolution to "); Serial.println(TEMPERATURE_PRECISION, DEC);
    // set the resolution to TEMPERATURE_PRECISION bit (Each Dallas/Maxim device is capable of several different resolutions)
    sensors.setResolution(tempDeviceAddress, TEMPERATURE_PRECISION);   
    Serial.print("Resolution actually set to: ");
    Serial.print(sensors.getResolution(tempDeviceAddress), DEC); 
    Serial.println();
  }else{
    Serial.print("Found ghost device at ");
    Serial.print(i, DEC);
    Serial.print(" but could not detect address. Check power and cabling");
  }
  }
    return 0;
}

// function to print a device address
void SensDS::printAddress(DeviceAddress deviceAddress)
{
  for (uint8_t i = 0; i < 8; i++)
  {
    if (deviceAddress[i] < 16) Serial.print("0");
    Serial.print(deviceAddress[i], HEX);
  }
}
