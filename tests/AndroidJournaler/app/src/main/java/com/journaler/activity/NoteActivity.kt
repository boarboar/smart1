package com.journaler.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.os.*
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.journaler.R
import com.journaler.database.Crud
import com.journaler.database.Db
import com.journaler.execution.TaskExecutor
import com.journaler.location.LocationProvider
import com.journaler.model.MODE
import com.journaler.model.Note
import com.journaler.service.DatabaseService
import kotlinx.android.synthetic.main.activity_note.*
import java.util.*


class NoteActivity : ItemActivity() {
    override val tag = "Note activity"
    override fun getLayout() = R.layout.activity_note

    private val executor = TaskExecutor.getInstance(1)
    private var handler: Handler? = null

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

    private val crudOperationListener = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent?.let {
                val crudResultValue =
                        intent.getIntExtra(MODE.EXTRAS_KEY, 0)
                sendMessage(crudResultValue == 1)
            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location?) {
            /*
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
            */
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note_title.addTextChangedListener(textWatcher)
        note_content.addTextChangedListener(textWatcher)

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                msg?.let {
                    var color = R.color.vermilion
                    if (msg.arg1 > 0) {
                        color = R.color.green
                    }
                    indicator.setBackgroundColor(ContextCompat.getColor(
                            this@NoteActivity,
                            color
                    ))
                }
                super.handleMessage(msg)
            }
        }
        val intentFiler = IntentFilter(Crud.BROADCAST_ACTION)
        registerReceiver(crudOperationListener, intentFiler)
    }

    override fun onDestroy() {
        unregisterReceiver(crudOperationListener)
        super.onDestroy()
    }

    private fun insertNote() {
        val title = getNoteTitle()
        val content = getNoteContent()
        val p0 = Location("dummyprovider")
        p0.longitude = 30.0
        p0.latitude = 60.0

        note = Note(title, content, p0)

        /*
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

            sendMessage(result)

        }
        */

        val dbIntent = Intent(this@NoteActivity,
                DatabaseService::class.java)
        dbIntent.putExtra(DatabaseService.EXTRA_ENTRY, note)
        dbIntent.putExtra(DatabaseService.EXTRA_OPERATION,
                MODE.CREATE.mode)
        startService(dbIntent)
        sendMessage(true)

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

            /*
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

                sendMessage(result)
            }
            */

            // Switching to intent service.
            val dbIntent = Intent(this@NoteActivity,
                    DatabaseService::class.java)
            dbIntent.putExtra(DatabaseService.EXTRA_ENTRY, note)
            dbIntent.putExtra(DatabaseService.EXTRA_OPERATION,
                    MODE.EDIT.mode)
            startService(dbIntent)
            sendMessage(true)

        }
    }

    private fun getNoteContent(): String {
        return note_content.text.toString()
    }
    private fun getNoteTitle(): String {
        return note_title.text.toString()
    }

   private fun sendMessage(result: Boolean) {
        Log.v(tag, "Crud operation result [ $result ]")
        val msg = handler?.obtainMessage()
        if (result) {
            msg?.arg1 = 1
        } else {
            msg?.arg1 = 0
        }
        handler?.sendMessage(msg)
    }
}
