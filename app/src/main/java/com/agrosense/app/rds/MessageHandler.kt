package com.agrosense.app.rds

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.agrosense.app.dsl.ReadingInserter
import com.agrosense.app.rds.bluetooth.MESSAGE_READ
import com.agrosense.app.rds.bluetooth.MESSAGE_TOAST
import com.agrosense.app.rds.bluetooth.MESSAGE_WRITE
import com.agrosense.app.rds.parser.MessageSerializer

class MessageHandler(context: Context) : Handler() {
    private val contextReference = context
    private val inserter = ReadingInserter.fromContext(context)
    private val parser = MessageSerializer()

    override fun handleMessage(msg: Message) {
        val context = contextReference

        when (msg.what) {
            MESSAGE_READ -> {
                val bytes = msg.obj as ByteArray
                val readMessage = String(bytes, 0, msg.arg1)
                Log.i(TAG, "Message read: $readMessage")

                inserter.insert(parser.process(readMessage))
            }

            MESSAGE_WRITE -> {
            }

            MESSAGE_TOAST -> {
                val toastMessage = msg.data.getString("toast")
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }

    }

    companion object {
        const val TAG = "MessageHandler"
    }
}