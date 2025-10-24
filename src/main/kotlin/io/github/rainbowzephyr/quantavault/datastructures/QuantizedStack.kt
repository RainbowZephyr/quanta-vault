package io.github.rainbowzephyr.quantavault.datastructures

import io.github.rainbowzephyr.quantavault.ExpirableItem
import io.github.rainbowzephyr.quantavault.ExpiryDuration
import io.github.rainbowzephyr.quantavault.QuantizedStructure
import jakarta.validation.constraints.NotEmpty
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class QuantizedStack<T : Any> : QuantizedStructure<T> {
    @NotEmpty
    private val list: ArrayList<ExpirableItem<T>>

    constructor(
        expiryExpiryDuration: ExpiryDuration,
        callback: Runnable,
        forceEviction: Boolean = false,
    ) : super(
        expiryExpiryDuration,
        callback,
        forceEviction,
    ) {
        this.list = ArrayList()
        initializeScheduler()
    }

    fun push(e: T): T {
        this.list.add(ExpirableItem(e, Instant.now()))

        return e
    }

    @Throws(EmptyStackException::class)
    fun pop(): T {
        if (list.isEmpty()) throw EmptyStackException()
        return list.last().item
    }

    fun peek(): T? = if (list.isNotEmpty()) {
        list.last().item
    } else {
        null
    }

    override fun initializeScheduler() {
        if (expiryDuration != null) {
            @Suppress("unchecked_cast")
            val runnable =
                initializeListIterableScheduler(
                    list as java.util.Collection<ExpirableItem<T>>,
                    expiryDuration,
                    evictionCallback,
                )

            this.scheduledExecutor.scheduleWithFixedDelay(
                runnable,
                expiryDuration.value,
                expiryDuration.value,
                TimeUnit.of(expiryDuration.unit),
            )
        }
    }
}
