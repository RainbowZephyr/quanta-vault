package io.github.rainbowzephyr.quantavault.datastructures

import io.github.rainbowzephyr.quantavault.ExpirableItem
import io.github.rainbowzephyr.quantavault.ExpiryDuration
import io.github.rainbowzephyr.quantavault.QuantizedStructure
import io.github.rainbowzephyr.quantavault.datastructures.SchedulerInitializer
import jakarta.validation.constraints.NotEmpty
import java.time.Instant
import java.util.Queue
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit


open class QuantizedQueue<T : Any> : QuantizedStructure<T>, Queue<T> {
    @NotEmpty
    private val queue: ArrayBlockingQueue<ExpirableItem<T>>


    constructor(
        expiryExpiryDuration: ExpiryDuration,
        sizeLimit: Int,
        callback: Runnable,
        forceEviction: Boolean = false
    ) : super(
        expiryExpiryDuration, sizeLimit, callback, forceEviction
    ) {
        this.queue = ArrayBlockingQueue(sizeLimit)
        initializeScheduler()
    }

    private constructor(
        expiryExpiryDuration: ExpiryDuration,
        callback: Runnable,
        forceEviction: Boolean = false
    ) : super(
        expiryExpiryDuration, callback, forceEviction
    ) {
        this.queue = ArrayBlockingQueue(1)
    }

    constructor(sizeLimit: Int, callback: Runnable, forceEviction: Boolean = false) : super(
        sizeLimit,
        callback,
        forceEviction
    ) {
        this.queue = ArrayBlockingQueue(sizeLimit)
        initializeScheduler()
    }

    override fun initializeScheduler() {
        if (expiryDuration != null) {

//            val runnable = Runnable {
//                val itemsToRemove = ArrayList<ExpirableItem<T>>()
//
//                for (i in queue) {
//                    // Check if insertion date is no longer valid
//                    val duration = Duration.of(expiryDuration.value, expiryDuration.unit)
//                    if (i.insertionTime.plus(duration).isBefore(Instant.now())) {
//                        try {
//                            evictionCallback.run()
//                            itemsToRemove.add(i)
//                        } catch (e: Exception) {
//                            throw RuntimeException("Failed to remove item ${i.item}", e)
//                        }
//                    }
//                }
//
//                for (item in itemsToRemove) {
//                    queue.remove(item)
//                }
//
////                println("After cleanup: $queue")
//            }

            @Suppress("unchecked_cast")
            val runnable = initializeListIterableScheduler(queue as java.util.Collection<ExpirableItem<T>>, expiryDuration, evictionCallback)

            this.scheduledExecutor.scheduleWithFixedDelay(
                runnable, expiryDuration.value, expiryDuration.value, TimeUnit.of(expiryDuration.unit)
            )
        }
    }

    override fun peek(): T? {
        return queue.peek()?.item
    }

    override fun element(): T? {
        return queue.element()?.item
    }

    override fun poll(): T? {
        return queue.poll()?.item
    }

    override fun remove(): T? {
        return queue.remove()?.item
    }

    override fun offer(e: T?): Boolean {
        TODO("Not yet implemented")
    }

    override fun add(e: T): Boolean {
        return queue.add(ExpirableItem(e, Instant.now()))
    }

    override fun remove(element: T): Boolean {
        return queue.remove(ExpirableItem(element, Instant.now()))
    }

    override fun addAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = queue.size

    override fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    override fun contains(element: T): Boolean {
        return queue.contains(ExpirableItem(element, Instant.now()))
    }

    override fun containsAll(elements: Collection<T?>): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        val tmp = ArrayBlockingQueue<T>(queue.size, false, queue.map { it.item })
        return tmp.toString()
    }


}