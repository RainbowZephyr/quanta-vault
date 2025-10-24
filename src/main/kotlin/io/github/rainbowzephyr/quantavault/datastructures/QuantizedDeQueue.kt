package io.github.rainbowzephyr.quantavault.datastructures

import io.github.rainbowzephyr.quantavault.ExpirableItem
import io.github.rainbowzephyr.quantavault.ExpiryDuration
import io.github.rainbowzephyr.quantavault.QuantizedStructure
import jakarta.validation.constraints.NotEmpty
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

open class QuantizedDeQueue<T : Any> :
    QuantizedStructure<T>,
    Deque<T> {
    @NotEmpty
    private val queue: ArrayDeque<ExpirableItem<T>>

//    constructor(
//        expiryExpiryDuration: ExpiryDuration, sizeLimit: Int, callback: Runnable, forceEviction: Boolean = false
//    ) : super(
//        expiryExpiryDuration, sizeLimit, callback, forceEviction
//    ) {
//        this.queue = ArrayDeque(sizeLimit)
//        initializeScheduler()
//    }

    constructor(
        expiryExpiryDuration: ExpiryDuration,
        sizeLimit: Int,
        callback: Runnable,
        forceEviction: Boolean = false,
    ) : super(
        expiryExpiryDuration,
        callback,
        forceEviction,
    ) {
        this.queue = ArrayDeque(sizeLimit)
        initializeScheduler()
    }

//    private constructor(
//        expiryExpiryDuration: ExpiryDuration, callback: Runnable, forceEviction: Boolean = false
//    ) : super(
//        expiryExpiryDuration, callback, forceEviction
//    ) {
//        this.queue = ArrayDeque(1)
//    }

//    constructor(sizeLimit: Int, callback: Runnable, forceEviction: Boolean = false) : super(
//        sizeLimit, callback, forceEviction
//    ) {
//        this.queue = ArrayDeque(sizeLimit)
//        initializeScheduler()
//    }

    override fun initializeScheduler() {
        if (expiryDuration != null) {
            val runnable =
                Runnable {
                    val itemsToRemove = ArrayList<ExpirableItem<T>>()

                    for (i in queue) {
                        // Check if insertion date is no longer valid
                        val duration = Duration.of(expiryDuration.value, expiryDuration.unit)
                        if (i.insertionTime.plus(duration).isBefore(Instant.now())) {
                            try {
                                evictionCallback.run()
                                itemsToRemove.add(i)
                            } catch (e: Exception) {
                                throw RuntimeException("Failed to remove item ${i.item}", e)
                            }
                        }
                    }

                    for (item in itemsToRemove) {
                        queue.remove(item)
                    }
                }

            this.scheduledExecutor.scheduleWithFixedDelay(
                runnable,
                expiryDuration.value,
                expiryDuration.value,
                TimeUnit.of(expiryDuration.unit),
            )
        }
    }

    override fun addFirst(e: T) {
        queue.addFirst(ExpirableItem(e, Instant.now()))
    }

    override fun addLast(e: T) {
        queue.addLast(ExpirableItem(e, Instant.now()))
    }

    override fun offerFirst(e: T): Boolean = queue.offerFirst(ExpirableItem(e, Instant.now()))

    override fun offerLast(e: T): Boolean = queue.offerLast(ExpirableItem(e, Instant.now()))

    override fun removeFirst(): T = queue.removeFirst().item

    override fun removeLast(): T = queue.removeLast().item

    override fun pollFirst(): T? = queue.pollFirst()?.item

    override fun pollLast(): T? = queue.pollLast()?.item

    override fun getFirst(): T = queue.first.item

    override fun getLast(): T = queue.last.item

    override fun peekFirst(): T? = queue.peekFirst()?.item

    override fun peekLast(): T? = queue.peekLast()?.item

    override fun removeFirstOccurrence(o: Any): Boolean = queue.removeFirstOccurrence(ExpirableItem(o, Instant.now()))

    override fun removeLastOccurrence(o: Any): Boolean = queue.removeLastOccurrence(ExpirableItem(o, Instant.now()))

    override fun add(e: T): Boolean = queue.add(ExpirableItem(e, Instant.now()))

    override fun offer(e: T): Boolean = queue.offer(ExpirableItem(e, Instant.now()))

    override fun remove(): T = queue.remove().item

    override fun remove(o: T): Boolean = queue.remove(ExpirableItem(o, Instant.now()))

    override fun poll(): T? = queue.poll()?.item

    override fun element(): T = queue.element().item

    override fun peek(): T? = queue.peek()?.item

    override fun addAll(c: Collection<T>): Boolean = queue.addAll(c.map { ExpirableItem(it, Instant.now()) })

    override fun push(e: T) = queue.push(ExpirableItem(e, Instant.now()))

    override fun pop(): T = queue.pop().item

    override fun contains(o: T): Boolean = queue.contains(ExpirableItem(o, Instant.now()))

    override val size: Int
        get() = queue.size

    override fun iterator(): MutableIterator<T> {
        val tmp = ArrayDeque<T>(queue.map { it.item })
        return tmp.iterator()
    }

    override fun descendingIterator(): Iterator<T> {
        val tmp = queue.map { it.item }.iterator()
        return tmp
    }

    override fun clear() {
        queue.clear()
    }

    override fun removeAll(elements: Collection<T>): Boolean = queue.removeAll(elements.map { ExpirableItem(it, Instant.now()) }.toSet())

    override fun retainAll(elements: Collection<T>): Boolean = queue.retainAll(elements.map { ExpirableItem(it, Instant.now()) }.toSet())

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun containsAll(elements: Collection<T>): Boolean = queue.containsAll(elements.map { ExpirableItem(it, Instant.now()) }.toSet())
}
