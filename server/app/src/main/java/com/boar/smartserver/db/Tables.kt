package com.boar.smartserver.db

object SensorTable {
    val NAME = "Sensors"
    val ID = "_id"
    val DESCRIPTION = "descr"
}

object LogTable {
    val NAME = "Log"
    val ID = "_id"
    val TIMESTAMP = "timestamp"
    val MSG = "msg"
}

object SensorHistoryTable {
    val NAME = "SensorHistory"
    val ID = "_id"
    val TIMESTAMP = "timestamp"
    val SENSOR_ID = "sensor_id"
    val TEMPERATURE = "t"
    val VCC = "v"
    val HUMIDITY = "h"
    val DHUMIDITY = "dh"
}
