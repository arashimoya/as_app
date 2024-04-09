package com.agrosense.app.timeprovider

import org.joda.time.DateTime

class CurrentTimeProvider: TimeProvider {

    override fun now(): DateTime = DateTime.now()
}