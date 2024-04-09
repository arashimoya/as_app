package com.agrosense.app.timeprovider

import org.joda.time.DateTime

interface TimeProvider {

    fun now(): DateTime
}