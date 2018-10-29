package com.boar.smartserver.db

import com.boar.smartserver.domain.Sensor
import org.jetbrains.anko.db.*
import java.util.HashMap

class SensorDb(private val dbHelper: DbHelper = DbHelper.instance
    //                 private val dataMapper: DbDataMapper = DbDataMapper()
)   {

    companion object {
        private const val tag = "SNS DB"
        private val parser = rowParser { id: Int, description: String ->
            Sensor(id.toShort(), description)
        }

    }

    /*
    class MyRowParser : RowParser<Pair<Int, String>> {
        override fun parseRow(columns: Array<Any?>): Pair<Int, String> {
            return Pair(columns[0] as Int, columns[1] as String)
        }
    }
    */

    fun requestSensors() : List<Sensor> {

        lateinit var sensors : List<Sensor>

        dbHelper.use {

        sensors = select(SensorTable.NAME).parseList(parser)

        }
        return sensors
    }

    /*
    override fun requestForecastByZipCode(zipCode: Long, date: Long) = forecastDbHelper.use {

        val dailyRequest = "${DayForecastTable.CITY_ID} = ? AND ${DayForecastTable.DATE} >= ?"
        val dailyForecast = select(DayForecastTable.NAME)
                .whereSimple(dailyRequest, zipCode.toString(), date.toString())
                .parseList { DayForecast(HashMap(it)) }

        val city = select(CityForecastTable.NAME)
                .whereSimple("${CityForecastTable.ID} = ?", zipCode.toString())
                .parseOpt { CityForecast(HashMap(it), dailyForecast) }

        //if (city != null) dataMapper.convertToDomain(city) else null
        city?.let { dataMapper.convertToDomain(it) }
    }

    override fun requestDayForecast(id: Long) = forecastDbHelper.use {
        val forecast = select(DayForecastTable.NAME).byId(id).
                parseOpt { DayForecast(HashMap(it)) }

        //if (forecast != null) dataMapper.convertDayToDomain(forecast) else null
        forecast?.let { dataMapper.convertDayToDomain(it) }
    }

    fun saveForecast(forecast: ForecastList) = forecastDbHelper.use {

        clear(CityForecastTable.NAME)
        clear(DayForecastTable.NAME)

        with(dataMapper.convertFromDomain(forecast)) {
            insert(CityForecastTable.NAME, *map.toVarargArray())
            dailyForecast.forEach { insert(DayForecastTable.NAME, *it.map.toVarargArray()) }
        }
    }
*/
}