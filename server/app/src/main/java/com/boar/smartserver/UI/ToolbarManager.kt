package com.boar.smartserver.UI

import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.support.v7.widget.Toolbar
import com.boar.smartserver.R


interface ToolbarManager {

    val toolbar: Toolbar

    var toolbarTitle: String
        get() = toolbar.title.toString()
        set(value) {
            toolbar.title = value
        }

    fun initToolbar(menuId : Int, handle: (Int) -> Unit) {
        toolbar.inflateMenu(menuId)
        toolbar.setOnMenuItemClickListener {
            handle(it.itemId)
            true
        }
    }


    fun enableHomeAsUp(up: () -> Unit) {
        toolbar.navigationIcon = createUpDrawable()
        toolbar.setNavigationOnClickListener { up() }
    }

    private fun createUpDrawable() = DrawerArrowDrawable(toolbar.context).apply { progress = 1f }


    /*
    fun attachToScroll(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) toolbar.slideExit() else toolbar.slideEnter()
            }
        })
    }
    */

}
