package com.boar.smartserver.UI

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor
import com.boar.smartserver.domain.SensorList
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import java.util.ArrayList

//import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity(), ToolbarManager {
    override val tag = "Main activity"
    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    override fun getLayout() = R.layout.activity_main
    override fun getActivityTitle() = R.string.app_name

   // private val sensors: ArrayList<Sensor> = arrayListOf()
   private val sensors = SensorList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.v(tag, "[ ON CREATE - CHECK]")

        sensorList.layoutManager = LinearLayoutManager(this)

        initToolbar()
        loadSensors()
    }

    /*
    private fun loadForecast() = async(UI) {
        val result = bg { RequestForecastCommand(zipCode).execute()
        }
        updateUI(result.await())
    }
*/

    private fun loadSensors() {
        doAsync {
            sensors.add(Sensor(1, "Window", System.currentTimeMillis() - 60 * 1000 * 15, 21.5f, 3.04f))
            sensors.add(Sensor(2, "Balcony", System.currentTimeMillis() - 60 * 1000 * 5, -28.5f, 3.14f))
        }
        updateUI()
    }

    private fun updateUI() {
        val adapter = SensorListAdapter(sensors) {
            /*
            startActivity<DetailActivity>(DetailActivity.ID to it.id,
                    DetailActivity.CITY_NAME to weekForecast.city)
                    */
        }

        sensorList.adapter = adapter
        toolbarTitle = "Updated"
    }

}
