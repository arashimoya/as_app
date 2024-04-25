package com.agrosense.app.datautil.resolver.measurement

import com.agrosense.app.datautil.resolver.DataSetResolver
import com.agrosense.app.domain.entity.Measurement

class MeasurementDataSetResolver: DataSetResolver<Measurement> {
    override fun sort(data: List<Measurement>): MutableList<Measurement> {
        return data.sortedWith(compareBy<Measurement> { it.end != null }.thenByDescending { it.end })
            .toMutableList()
    }

    override fun resolveNewlyAdded(
        oldData: List<Measurement>,
        newData: List<Measurement>
    ): List<Measurement> {
        return newData.filter { new -> oldData.none { old -> old.measurementId == new.measurementId } }
    }

    override fun resolveUpdated(
        oldData: List<Measurement>,
        newData: List<Measurement>
    ): List<Measurement> {
        return newData.filter { new ->
            val old = oldData.find { it.measurementId == new.measurementId }
            old != null && new.end != old.end
        }
    }
}