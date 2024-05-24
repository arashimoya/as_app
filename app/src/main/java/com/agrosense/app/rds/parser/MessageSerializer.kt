package com.agrosense.app.rds.parser

import com.agrosense.app.domain.message.TemperatureMessage

class MessageSerializer {


    private val buffer: StringBuilder = StringBuilder()

    fun process(message: String): List<TemperatureMessage> {
        buffer.append(message.trim())
        val list = findCompleteMessages()
        return list.map { parse(it) }
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
        return TemperatureMessage(
            parseTemperature(message),
            parseTimestamp(message)
        )
    }

    private fun parseTimestamp(message: String) =
        message.substring(message.indexOf(COMMA) + 1, message.indexOf(END_BRACKET)).trim()
            .toLong()

    private fun parseTemperature(message: String) =
        message.substring(message.indexOf(START_BRACKET) + 1, message.indexOf(COMMA)).trim()
            .toDouble()


    companion object {
        private const val COMMA = ","
        private const val START_BRACKET = "{"
        private const val END_BRACKET = "}"
    }

}