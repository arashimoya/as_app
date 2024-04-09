package com.agrosense.app

import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.agrosense.app.bluetooth.MESSAGE_READ
import com.agrosense.app.bluetooth.MESSAGE_TOAST
import com.agrosense.app.bluetooth.MESSAGE_WRITE
import com.agrosense.app.parser.MessageSerializer
import java.lang.ref.WeakReference

class MessageHandler(activity: BluetoothActivity) : Handler() {
    private val activityReference = WeakReference(activity)
    private val inserter = ReadingInserter(activityReference)
    private val parser = MessageSerializer()

    override fun handleMessage(msg: Message) {
        val activity = activityReference.get()
        activity?.let {
            when (msg.what) {
                MESSAGE_READ -> {
                    val bytes = msg.obj as ByteArray
                    val readMessage = String(bytes, 0, msg.arg1)
                    Log.i(BluetoothActivity.TAG, "Message read: $readMessage")

                    inserter.insert(parser.process(readMessage))


                }

                MESSAGE_WRITE -> {
                }

                MESSAGE_TOAST -> {
                    val toastMessage = msg.data.getString("toast")
                    Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }
}