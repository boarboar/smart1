package com.boar.smartserver.UI

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import com.boar.smartserver.R
import com.boar.smartserver.UI.fragments.SensorBaseFragment
import com.boar.smartserver.UI.fragments.SystemLogFragment
import com.boar.smartserver.UI.fragments.SystemStatFragment
import kotlinx.android.synthetic.main.activity_sensor_pager.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class SystemPagerActivity() : BaseActivity(), ToolbarManager {

    override val tag = "System activity"

    override fun getLayout() = R.layout.activity_sensor_pager

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    companion object {
        //
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        initToolbar(R.menu.menu_sensor) {
            when (it) {

                else -> toast("Unknown option")
            }
        }
    */

        show()

        enableHomeAsUp { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachReceiver()
                .onSensorUpdate { ; }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachReceiver()
    }

    private fun show() {
        val page = viewpager.currentItem
        toolbarTitle = "System"
        viewpager.adapter = SystemPagerAdapter(supportFragmentManager)
        viewpager.currentItem = page
        sliding_tabs.setupWithViewPager(viewpager)

    }
}

class SystemPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val PAGE_COUNT = 2
    private val tabTitles = arrayOf("Stat", "Log")

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when(position) {
            1 -> SensorBaseFragment.newInstance(SystemLogFragment())
            else -> SensorBaseFragment.newInstance(SystemStatFragment())
        }

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}