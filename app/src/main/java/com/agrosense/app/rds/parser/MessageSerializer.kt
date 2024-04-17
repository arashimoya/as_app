package com.agrosense.app.rds.parser

import com.agrosense.app.domain.message.TemperatureMessage

class MessageSerializer {



    private val buffer: StringBuilder = StringBuilder()

    fun process(message: String): List<TemperatureMessage> {
        buffer.append(message.trim())
        val list = findCompleteMessages()
        return list.map {parse(it) }
    }

    private fun findCompleteMessages(): MutableList<String> {
        val mutableList = mutableListOf<String>()
        var startIndex = buffer.indexOf(START_BRACKET)
        var endIndex = buffer.indexOf(END_BRACKET)

        while (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            mutableList.add(buffer.substring(startIndex, endIndex + 1))

            buffer.delete(startIndex, endIndex + 1)

            startIndex = buffer.indexOf(START_BRACKET)
            endIndex = buffer.indexOf(END_BRACKET)
        }
        return mutableList
    }

    private fun parse(message: String): TemperatureMessage {
        val temperature = message.substring(TEMPERATURE_START, TEMPERATURE_END).toDouble()
        val timestamp = message.substring(TIMESTAMP_START, message.indexOf(END_BRACKET)).toLong()
        return TemperatureMessage(temperature, timestamp)
    }


    companion object {
        private const val START_BRACKET = "{"
        private const val END_BRACKET = "}"
        private const val TEMPERATURE_START = 1
        private const val TEMPERATURE_END = 5
        private const val TIMESTAMP_START = 7
    }

}