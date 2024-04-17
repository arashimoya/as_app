package com.agrosense.app.datautil.measurement

import com.agrosense.app.datautil.resolver.measurement.MeasurementDataSetResolver
import com.agrosense.app.domain.entity.Measurement
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test


internal class MeasurementDataSetResolverTest {

    private val resolver = MeasurementDataSetResolver()

    @Test
    fun `sort two measurements with non-null ends`() {
        val list = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 12, 11, 6, 0),
                30.0,
                20.0
            )
        )

        val result = resolver.sort(list)

        Assert.assertEquals(2L, result[0].measurementId)
    }

    @Test
    fun `sort two measurements with one having null end`() {
        val list = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )

        val result = resolver.sort(list)

        Assert.assertEquals(2L, result[0].measurementId)
    }

    @Test
    fun `resolve newly added when old data is empty`() {
        val oldData = listOf<Measurement>()
        val newData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )

        val result = resolver.resolveNewlyAdded(oldData, newData)

        Assert.assertEquals(newData, result)
    }

    @Test
    fun `resolve newly added when old data has one object`() {
        val oldData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            )
        )
        val newData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )

        val result = resolver.resolveNewlyAdded(oldData, newData)

        Assert.assertEquals(listOf(newData[1]), result)
    }

    @Test
    fun `resolve that there is no newly added objects`() {
        val oldData = listOf(
            Measurement(
                1,
                "test112",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test12",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )
        val newData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )

        val result = resolver.resolveNewlyAdded(oldData, newData)

        Assert.assertEquals(listOf<Measurement>(), result)
    }

    @Test
    fun `resolve updated is of size 0`() {

        val oldData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            )
        )
        val newData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )

        val result = resolver.resolveUpdated(oldData, newData)

        Assert.assertEquals(listOf<Measurement>(), result)

    }

    @Test
    fun `resolve updated objects`() {
        val oldData = listOf(
            Measurement(
                1,
                "test112",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test12",
                DateTime(2024, 10, 11, 12, 0),
                null,
                30.0,
                20.0
            )
        )
        val newData = listOf(
            Measurement(
                1,
                "test1",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            ),
            Measurement(
                2,
                "test2",
                DateTime(2024, 10, 11, 12, 0),
                DateTime(2024, 10, 11, 6, 0),
                30.0,
                20.0
            )
        )

        val result = resolver.resolveUpdated(oldData, newData)

        Assert.assertEquals(
            listOf(
                Measurement(
                    2,
                    "test2",
                    DateTime(2024, 10, 11, 12, 0),
                    DateTime(2024, 10, 11, 6, 0),
                    30.0,
                    20.0
                )
            ), result
        )
    }
}