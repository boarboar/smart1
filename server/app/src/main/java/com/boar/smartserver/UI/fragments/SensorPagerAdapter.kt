package com.boar.smartserver.UI.fragments

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class SensorPagerAdapter(fm: FragmentManager, val idx: Int) : FragmentPagerAdapter(fm) {
    private val PAGE_COUNT = 3
    private val tabTitles = arrayOf("Info", "Log", "Chart")

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        //return SensorDetailFragment.newInstance(idx)
        return when(position) {
            0 -> SensorBaseFragment.newInstance(SensorDetailFragment(), idx)
            1 -> SensorBaseFragment.newInstance(SensorLogFragment(), idx)
            else -> SensorBaseFragment.newInstance(SensorChartFragment(), idx)
        }

    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return tabTitles[position]
    }
}