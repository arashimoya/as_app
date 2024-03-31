package com.agrosense.app.parser

import com.agrosense.app.domain.message.TemperatureMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageSerializerTest {

    private val serializer: MessageSerializer = MessageSerializer()


    @Test
    fun should_return_an_empty_list_given_an_empty_message() {
        val message = ""

        val result = serializer.process(message)

        assertTrue(result.isEmpty())
    }

    @Test
    fun should_return_an_empty_list_given_an_uncompleted_message() {
        val message = INCOMPLETE_MESSAGE

        val result = serializer.process(message)

        assertTrue(result.isEmpty())
    }

    @Test
    fun should_return_a_list_of_one_message_given_correctly_formatted_message() {
        val message = COMPLETE_MESSAGE

        val result = serializer.process(message)

        assertFalse(result.isEmpty())
        assertEquals(1, result.size)
        assertEquals(TemperatureMessage(TEMPERATURE, NOW), result[0])
    }

    @Test
    fun should_return_a_list_of_one_message_given_divided_message() {
        val message = INCOMPLETE_MESSAGE
        val theRest = INCOMPLETE_MESSAGE_PART_2

        val result = serializer.process(message)
        val resultComplete = serializer.process(theRest)

        assertTrue(result.isEmpty())
        assertFalse(resultComplete.isEmpty())
        assertEquals(1, resultComplete.size)
        assertEquals(TemperatureMessage(TEMPERATURE, NOW), resultComplete[0])
    }

    @Test
    fun should_return_an_empty_list_given_divided_message_but_revered() {
        val message = INCOMPLETE_MESSAGE_PART_2
        val theRest = INCOMPLETE_MESSAGE

        val result = serializer.process(message)
        val resultComplete = serializer.process(theRest)

        assertTrue(result.isEmpty())
        assertTrue(resultComplete.isEmpty())

    }

    @Test
    fun should_return_a_list_of_one_message_given_a_message_split_into_3_parts(){
        val message1 = SPLIT3_1
        val message2 = SPLIT3_2
        val message3 = SPLIT3_3

        val result1 = serializer.process(message1)
        val result2 = serializer.process(message2)
        val result3 = serializer.process(message3)

        assertTrue(result1.isEmpty())
        assertTrue(result2.isEmpty())
        assertFalse(result3.isEmpty())
        assertEquals(listOf(TemperatureMessage(TEMPERATURE, NOW)), result3)
    }

    @Test
    fun should_return_a_list_of_two_given_two_messages_split_into_2_parts(){
        val message1 = SPLIT2_1
        val message2 = SPLIT2_2

        val result1 = serializer.process(message1)
        val result2 = serializer.process(message2)

        assertTrue(result1.isEmpty())
        assertFalse(result2.isEmpty())
        assertEquals(2,result2.size)
        assertEquals(listOf(TemperatureMessage(TEMPERATURE, NOW), TemperatureMessage(TEMPERATURE2, NOW2)), result2)
    }

    companion object {
        private const val NOW: Long = 1711808586
        private const val TEMPERATURE: Double = 28.5
        private const val NOW2: Long = 1711808820
        private const val TEMPERATURE2: Double = 30.5
        private const val INCOMPLETE_MESSAGE = "{$TEMPERATURE, 17"
        private const val INCOMPLETE_MESSAGE_PART_2 = "11808586}"
        private const val COMPLETE_MESSAGE = "{$TEMPERATURE, $NOW}"

        private const val SPLIT3_1 = "{28.5"
        private const val SPLIT3_2 = ", 171180"
        private const val SPLIT3_3 = "8586}"

        private const val SPLIT2_1 = "{28.5"
        private const val SPLIT2_2 = ", 1711808586}{30.5, 1711808820}"


    }
}

