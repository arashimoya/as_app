package com.agrosense.app.rds.bluetooth

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.os.Handler
import com.agrosense.app.rds.MessageHandler

private const val TAG = "BluetoothService"
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

class BluetoothCommunicationService(private val handler: Handler) {

    constructor(context: Context): this(MessageHandler(context))

    private var connectedThread: ConnectedThread? = null

    fun read(socket: BluetoothSocket) {
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
    }

    fun write(data: ByteArray) {
        connectedThread!!.write(data)
    }

    fun stopConnection() {
        connectedThread?.cancel()
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inStream: InputStream = socket.inputStream
        private val outStream: OutputStream = socket.outputStream
        private val buffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int

            while (true) {
                numBytes = try {
                    inStream.read(buffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                val readMsg = handler.obtainMessage(MESSAGE_READ, numBytes, -1, buffer)
                handler.handleMessage(readMsg)
            }
        }

        fun write(bytes: ByteArray){
            try{
                outStream.write(bytes)
            } catch (e: IOException){
                Log.e(TAG, "Error occurred when sending data", e)

                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply { putString("toast", "Couldnt send data to the other device") }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
            }

            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, bytes.size, -1, buffer
            )
            handler.handleMessage(writtenMsg)
        }

        fun cancel(){
            try {
                socket.close()
            } catch (e: IOException){
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}