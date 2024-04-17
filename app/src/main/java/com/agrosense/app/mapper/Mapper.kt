package com.agrosense.app.mapper

interface Mapper<K,T> {

    fun map(arg: K, id: Long? = null): T
}