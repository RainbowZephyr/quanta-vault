package io.github.rainbowzephyr.quantavault.concurrent

import io.github.rainbowzephyr.quantavault.ExpirableItem
import io.github.rainbowzephyr.quantavault.ExpiryDuration
import io.github.rainbowzephyr.quantavault.QuantizedStructure
import jakarta.validation.constraints.NotEmpty
import java.time.Instant
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit


open class QuantizedBlockingQueue<T> : QuantizedStructure<T>, Queue<T> {
    @NotEmpty
    private val queue: ArrayBlockingQueue<ExpirableItem<T>>

//    constructor(
//        expiryExpiryDuration: ExpiryDuration, sizeLimit: Int, callback: Runnable, forceEviction: Boolean = false
//    ) : super(
//        expiryExpiryDuration, sizeLimit, callback, forceEviction
//    ) {
//        this.queue = ArrayBlockingQueue(sizeLimit)
//        initializeScheduler()
//    }

    constructor(
        expiryExpiryDuration: ExpiryDuration, sizeLimit: Int, callback: Runnable, forceEviction: Boolean = false
    ) : super(
        expiryExpiryDuration,  callback, forceEviction
    ) {
        this.queue = ArrayBlockingQueue(sizeLimit)
        initializeScheduler()
    }

    private constructor(
        expiryExpiryDuration: ExpiryDuration, callback: Runnable, forceEviction: Boolean = false
    ) : super(
        expiryExpiryDuration, callback, forceEviction
    ) {
        this.queue = ArrayBlockingQueue(1)
    }

//    constructor(sizeLimit: Int, callback: Runnable, forceEviction: Boolean = false) : super(
//        sizeLimit, callback, forceEviction
//    ) {
//        this.queue = ArrayBlockingQueue(sizeLimit)
//        initializeScheduler()
//    }

    override fun initializeScheduler() {
        if (expiryDuration != null) {
            @Suppress("unchecked_cast") val runnable = initializeListIterableScheduler(
                queue as java.util.Collection<ExpirableItem<T>>,
                expiryDuration,
                evictionCallback
            )

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

    fun removeWithCallback(): T? {
        val removedItem = remove()
        if(removedItem != null) {
            evictionCallback.run()
        }

        return removedItem
    }

    override fun remove(): T? {
        return queue.remove()?.item
    }

    override fun offer(e: T): Boolean {
        if(queue.size == sizeLimit){
            evictionCallback.run()
        }

        return queue.offer(ExpirableItem(e, Instant.now()))
    }

    override fun add(e: T): Boolean {
        if(queue.size == sizeLimit){
            evictionCallback.run()
        }

        return queue.add(ExpirableItem(e, Instant.now()))
    }

    fun removeWithCallback(e: T): Boolean {
        val isRemoved = remove(e)
        if(isRemoved || forceCallbackOnRemoval) {
            evictionCallback.run()
        }

        return isRemoved
    }

    override fun remove(element: T): Boolean {
        return queue.remove(ExpirableItem(element, Instant.now()))
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return queue.addAll(elements.map { ExpirableItem(it, Instant.now()) })
    }

    override fun clear() {
        queue.clear()
    }

    override fun iterator(): MutableIterator<T> {
        val reducedQueue = ArrayBlockingQueue<T>(queue.size, true, queue.map(ExpirableItem<T>::item))
        return reducedQueue.iterator()
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val expirableItems = elements.map { ExpirableItem(it, Instant.now()) }.toSet()
        return queue.removeAll(expirableItems)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val expirableItems = elements.map { ExpirableItem(it, Instant.now()) }.toSet()
        return queue.retainAll(expirableItems)
    }

    override val size: Int
        get() = queue.size

    override fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    override fun contains(element: T): Boolean {
        return queue.contains(ExpirableItem(element, Instant.now()))
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        val expirableItems = elements.map { ExpirableItem(it, Instant.now()) }.toSet()
        return queue.containsAll(expirableItems)
    }

    override fun toString(): String {
        val tmp = ArrayBlockingQueue<T>(queue.size, false, queue.map { it.item })
        return tmp.toString()
    }

}