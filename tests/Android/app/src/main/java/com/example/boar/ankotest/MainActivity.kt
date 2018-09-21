package com.example.boar.ankotest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        verticalLayout {
            val name = editText()
            button("Say Hello") {
                onClick { toast("Hello, ${name.text}!") }
            }
        }
    }
}
