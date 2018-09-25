package com.example.boar.ankotest

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.jetbrains.anko.*

/**
 * Created by abduaziz on 5/25/18.
 */

class MovieUI : AnkoComponent<ViewGroup>{

    companion object {
        val tvTitleId = 1
        val tvYearId = 2
    }

    override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui){
        verticalLayout {
            //lparams(matchParent, wrapContent)
            padding = dip(16)

            textView {
                id = tvTitleId
                layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
                text = "Sherlock"
                textSize = 16f // <- it is sp, no worries
                textColor = Color.BLACK
            }

            textView {
                id = tvYearId
                layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
                text = "2009"
                textSize = 14f
            }
        }//.lparams{width = matchParent, height = matchParent}
    }
}
