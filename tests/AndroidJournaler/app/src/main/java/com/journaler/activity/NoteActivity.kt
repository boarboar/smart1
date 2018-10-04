package com.journaler.activity

import android.location.Location
import android.location.LocationListener
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.journaler.R
import com.journaler.database.Db
import com.journaler.execution.TaskExecutor
import com.journaler.location.LocationProvider
import com.journaler.model.Note
import kotlinx.android.synthetic.main.activity_note.*
import java.util.*


class NoteActivity : ItemActivity() {
    override val tag = "Note activity"
    override fun getLayout() = R.layout.activity_note

    private val executor = TaskExecutor.getInstance(1)

    private var note: Note? = null
    private var location: Location? = null

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            updateNote()
        }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2:
        Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2:
        Int, p3: Int) {}
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location?) {
            p0?.let {

                Log.i(
                        tag,
                        String.format(
                                Locale.ENGLISH,
                                "Location [ lat: %s ][ long: %s ]", p0.latitude, p0.longitude
                        )
                )

                LocationProvider.unsubscribe(this)
                location = p0
                val title = getNoteTitle()
                val content = getNoteContent()
                note = Note(title, content, p0)

                executor.execute {
                    val param = note
                    var result = false
                    param?.let {
                        result = Db.NOTE.insert(param)>0
                    }
                    if (result) {
                        Log.i(tag, "Note inserted.")
                    } else {
                        Log.e(tag, "Note not inserted.")
                    }
                }

            }
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note_title.addTextChangedListener(textWatcher)
        note_content.addTextChangedListener(textWatcher)
    }

    private fun insertNote() {
        val title = getNoteTitle()
        val content = getNoteContent()
        val p0 = Location("dummyprovider")
        p0.longitude = 30.0
        p0.latitude = 60.0

        note = Note(title, content, p0)

        executor.execute {
            val param = note
            var result = false
            param?.let {
                result = Db.NOTE.insert(param)>0
            }
            if (result) {
                Log.i(tag, "Note inserted.")
            } else {
                Log.e(tag, "Note not inserted.")
            }
        }
    }

    private fun updateNote() {
        if (note == null) {
            if (!TextUtils.isEmpty(getNoteTitle()) &&
                    !TextUtils.isEmpty(getNoteContent())) {
                //LocationProvider.subscribe(locationListener)
                insertNote()
            }
        } else {
            note?.title = getNoteTitle()
            note?.message = getNoteContent()

            executor.execute {
                val param = note
                var result = false
                param?.let {
                    result = Db.NOTE.update(param)>0
                }
                if (result) {
                    Log.i(tag, "Note updated.")
                } else {
                    Log.e(tag, "Note not updated.")
                }
            }

        }
    }

    private fun getNoteContent(): String {
        return note_content.text.toString()
    }
    private fun getNoteTitle(): String {
        return note_title.text.toString()
    }
}
