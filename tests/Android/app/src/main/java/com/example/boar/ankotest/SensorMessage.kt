package com.example.boar.ankotest

import com.google.gson.annotations.SerializedName

// '{"I":"1","M":64,"P":0,"R":8,"T":210,"V":310}'

data class SensorMessage(
        @SerializedName("I") val id: Short,
        @SerializedName("M") val model: Short,
        @SerializedName("P") val isParasite: Short,
        @SerializedName("R") val resolution: Short,
        @SerializedName("T") val temp10: Short,
        @SerializedName("V") val voltage100: Short
)
