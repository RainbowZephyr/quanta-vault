package io.github.rainbowzephyr.quantavault


import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.Collection

abstract class QuantizedStructure<T> {
    val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    val expiryDuration: ExpiryDuration?
    val sizeLimit: Int?
    val evictionCallback: Runnable
    val forceCallbackOnRemoval: Boolean

//    constructor(expiryExpiryDuration: ExpiryDuration, sizeLimit: Int, callback: Runnable, forceEviction: Boolean) {
//        this.expiryDuration = expiryExpiryDuration
//        this.sizeLimit = sizeLimit
//        this.evictionCallback = callback
//        this.forceCallbackOnRemoval = forceEviction
//    }

    constructor(expiryExpiryDuration: ExpiryDuration, callback: Runnable, forceEviction: Boolean) {
        this.expiryDuration = expiryExpiryDuration
        this.sizeLimit = null
        this.evictionCallback = callback
        this.forceCallbackOnRemoval = forceEviction
    }

//    constructor(sizeLimit: Int, callback: Runnable, forceEviction: Boolean) {
//        this.expiryDuration = null
//        this.sizeLimit = sizeLimit
//        this.evictionCallback = callback
//        this.forceCallbackOnRemoval = forceEviction
//    }

    @Throws(Exception::class)
    protected abstract fun initializeScheduler()

    protected fun initializeListIterableScheduler(
        iterable: Collection<ExpirableItem<T>>, expiryDuration: ExpiryDuration, evictionCallback: Runnable
    ): Runnable {
        return Runnable {
            val itemsToRemove = ArrayList<ExpirableItem<T>>()

            for (i in iterable) {
                // Check if insertion date is no longer valid
                val duration = Duration.of(expiryDuration.value, expiryDuration.unit)
                if (i.insertionTime.plus(duration).isBefore(Instant.now())) {
                    itemsToRemove.add(i)
                }
            }

            for (item in itemsToRemove) {
                try {
                    evictionCallback.run()
                    iterable.remove(item)
                } catch (e: Exception) {
                    throw RuntimeException("Failed to remove item ${item.item}", e)
                }
            }

            println("After cleanup: $iterable")
        }
    }


    @Throws(SecurityException::class)
    fun destroy() {
        scheduledExecutor.shutdownNow()
    }

}