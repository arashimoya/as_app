package com.agrosense.app.rds

import android.content.Context
import android.os.Bundle
import android.os.Message
import com.agrosense.app.domain.message.TemperatureMessage
import com.agrosense.app.dsl.ReadingInserter
import com.agrosense.app.rds.bluetooth.MESSAGE_READ
import com.agrosense.app.rds.bluetooth.MESSAGE_TOAST
import com.agrosense.app.rds.bluetooth.MESSAGE_WRITE
import com.agrosense.app.rds.parser.MessageSerializer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class MessageHandlerTest{

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var handler: MessageHandler
    private lateinit var context: Context

    private var inserter: ReadingInserter = mock(ReadingInserter::class.java)

    private var parser: MessageSerializer = mock(MessageSerializer::class.java)

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        handler = MessageHandler(context, inserter, parser)
    }

    @Test
    fun `MESSAGE_READ - should handle empty data`() {
        val sampleData = "test"
        val message = Message.obtain(null, MESSAGE_READ, sampleData.toByteArray().size, 0, sampleData.toByteArray())
        `when`(parser.process(sampleData)).thenReturn(emptyList())

        handler.handleMessage(message)

        verify(parser).process(sampleData)
        verify(inserter).insert(emptyList())
    }

    @Test
    fun `MESSAGE_READ - should handle one normal data`() {
        val sampleData = "test"
        val message = Message.obtain(null, MESSAGE_READ, sampleData.toByteArray().size, 0, sampleData.toByteArray())
        `when`(parser.process(sampleData)).thenReturn(listOf(temperatureMessage))

        handler.handleMessage(message)

        verify(parser).process(sampleData)
        verify(inserter).insert(listOf(temperatureMessage))
    }

    @Test
    fun `MESSAGE_READ - should handle multiple reading data`() {
        val sampleData = "test"
        val message = Message.obtain(null, MESSAGE_READ, sampleData.toByteArray().size, 0, sampleData.toByteArray())
        `when`(parser.process(sampleData)).thenReturn(listOf(temperatureMessage, temperatureMessage, temperatureMessage))

        handler.handleMessage(message)

        verify(parser).process(sampleData)
        verify(inserter).insert(listOf(temperatureMessage, temperatureMessage, temperatureMessage))
    }

    @Test
    fun `handle MESSAGE_TOAST and show toast`() {
        val toastMessage = "Test Toast"
        val bundle = Bundle().apply { putString("toast", toastMessage) }
        val message = Message.obtain(null, MESSAGE_TOAST)
        message.data = bundle

        handler.handleMessage(message)

        // Robolectric allows to check Toast was shown
        val shownToast = ShadowToast.getTextOfLatestToast()
        assertEquals(toastMessage, shownToast)
    }

    @Test
    fun `handle MESSAGE_WRITE and log message`() {
        val sampleData = "Write data"
        val message = Message.obtain(null, MESSAGE_WRITE, sampleData.toByteArray().size, 0, sampleData.toByteArray())

        assertDoesNotThrow { handler.handleMessage(message) }


    }

    companion object{
        private val temperatureMessage = TemperatureMessage(22.0, 124)
    }
}