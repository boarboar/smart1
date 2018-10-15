package com.boar.smartserver.UI

//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.boar.smartserver.R
import com.boar.smartserver.domain.Sensor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

//import com.boar.smartserver.SmartServer
//import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity() {
    override val tag = "Main activity"

    override fun getLayout() = R.layout.activity_main
    override fun getActivityTitle() = R.string.app_name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(tag, "[ ON CREATE - CHECK]")

        sensorList.layoutManager = LinearLayoutManager(this)

        val sensors: ArrayList<Sensor> = arrayListOf()
        sensors.add(Sensor(1, "Window", System.currentTimeMillis()-60*1000*15, 20.5f, 3.04f))
        sensors.add(Sensor(2, "Balcony", System.currentTimeMillis()-60*1000*5, 26.5f, 3.14f))

        val adapter = SensorListAdapter(sensors) {
            /*
            startActivity<DetailActivity>(DetailActivity.ID to it.id,
                    DetailActivity.CITY_NAME to weekForecast.city)
                    */
        }

        sensorList.adapter = adapter
    }
}
