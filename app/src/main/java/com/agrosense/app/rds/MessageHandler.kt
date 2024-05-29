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

class MessageHandler(
    context: Context, private val inserter: ReadingInserter = ReadingInserter.fromContext(context),
    private val parser: MessageSerializer = MessageSerializer()
) : Handler() {
    private val contextReference = context


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
                val bytes = msg.obj as ByteArray
                val writeMsg = String(bytes, 0, msg.arg1)
                Log.i(TAG, "Message write: $writeMsg")
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