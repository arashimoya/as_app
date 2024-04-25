package com.agrosense.app.datautil.resolver

interface DataSetResolver<T> {
    fun sort(data: List<T>): MutableList<T>
    fun resolveNewlyAdded(oldData: List<T>, newData: List<T>): List<T>
    fun resolveUpdated(oldData: List<T>, newData: List<T>): List<T>
}