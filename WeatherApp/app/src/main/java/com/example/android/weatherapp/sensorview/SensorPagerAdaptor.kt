package com.example.android.weatherapp.sensorview

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import androidx.fragment.app.FragmentPagerAdapter
import com.example.android.weatherapp.domain.Sensor

class SensorPagerAdapter(fm: FragmentManager, val sensor: Sensor) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val PAGE_COUNT = 3
    private val tabTitles = arrayOf("Info", "Log", "Chart")

    init {
        Log.v("SENSORPAGEADAPTER", "Inited for ${sensor.description}")
    }

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        //return SensorBaseFragment.newInstance(SensorDetailFragment(), idx)

//        return when(position) {
//            0 -> SensorDetailFragment.newInstance(SensorDetailFragment(), sensor)
//            1 -> SensorDetailFragment.newInstance(SensorDetailFragment(), sensor)
//            else -> SensorDetailFragment.newInstance(SensorDetailFragment(), sensor)
//        }

        Log.v("SENSORPAGEADAPTER", "getItem $position for ${sensor.description}")

        return when(position) {
            0 -> SensorBaseFragment.newInstance(SensorDetailFragment(), sensor)
            1 -> SensorBaseFragment.newInstance(SensorDetailFragment(), sensor)
            else -> SensorBaseFragment.newInstance(SensorDetailFragment(), sensor)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return tabTitles[position]
    }
}