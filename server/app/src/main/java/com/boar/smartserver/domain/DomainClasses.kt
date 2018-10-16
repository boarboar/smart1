package com.boar.smartserver.domain

import java.util.ArrayList

data class Sensor(val id: Short, val description: String,
                  val dateLast: Long,  val tempLast: Float, val vccLast: Float
                  //, val measList
                  )

class SensorList : ArrayList<Sensor>() {
}