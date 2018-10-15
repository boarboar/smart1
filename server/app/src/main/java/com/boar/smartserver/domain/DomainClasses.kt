package com.boar.smartserver.domain

data class Sensor(val id: Short, val description: String,
                  val dateLast: Long,  val tempLast: Float, val vccLast: Float
                  //, val measList
                  )

