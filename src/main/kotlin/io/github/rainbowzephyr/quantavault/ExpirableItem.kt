package io.github.rainbowzephyr.quantavault

import java.time.Instant

// Runnable for simpler Java interoperability
@JvmRecord
data class ExpirableItem<T>(
    val item: T,
    val insertionTime: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExpirableItem<*>

        return item == other.item
    }

    override fun hashCode(): Int = item?.hashCode() ?: 0
}
