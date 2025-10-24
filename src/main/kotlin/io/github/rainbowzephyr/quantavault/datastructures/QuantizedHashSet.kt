package io.github.rainbowzephyr.quantavault.datastructures

import io.github.rainbowzephyr.quantavault.ExpirableItem
import io.github.rainbowzephyr.quantavault.ExpiryDuration
import io.github.rainbowzephyr.quantavault.QuantizedStructure
import jakarta.validation.constraints.NotEmpty
import java.time.Duration
import java.time.Instant
import java.util.Set
import java.util.concurrent.TimeUnit

class QuantizedHashSet<T> :
    QuantizedStructure<T>,
    Set<T> {
    @NotEmpty
    private val set: HashSet<ExpirableItem<T>>

//    constructor(
//        expiryExpiryDuration: ExpiryDuration, sizeLimit: Int, callback: Runnable, forceEviction: Boolean
//    ) : super(expiryExpiryDuration, sizeLimit, callback, forceEviction) {
//        this.set = HashSet()
//        initializeScheduler()
//    }

    constructor(expiryExpiryDuration: ExpiryDuration, callback: Runnable, forceEviction: Boolean) : super(
        expiryExpiryDuration,
        callback,
        forceEviction,
    ) {
        this.set = HashSet()
        initializeScheduler()
    }

//    constructor(sizeLimit: Int, callback: Runnable, forceEviction: Boolean) : super(
//        sizeLimit,
//        callback,
//        forceEviction
//    ) {
//        this.set = HashSet()
//        initializeScheduler()
//    }

    override fun initializeScheduler() {
        if (expiryDuration != null) {
            val runnable =
                Runnable {
                    val itemsToRemove = ArrayList<ExpirableItem<T>>()

                    for (i in set) {
                        // Check if insertion date is no longer valid
                        val duration = Duration.of(expiryDuration.value, expiryDuration.unit)
                        if (i.insertionTime.plus(duration).isBefore(Instant.now())) {
                            itemsToRemove.add(i)
                        }
                    }

                    for (item in itemsToRemove) {
                        try {
                            evictionCallback.run()
                            set.remove(item)
                        } catch (e: Exception) {
                            throw RuntimeException("Failed to remove item ${item.item}", e)
                        }
                    }

                    println("After cleanup: $set")
                }

            this.scheduledExecutor.scheduleWithFixedDelay(
                runnable,
                expiryDuration.value,
                expiryDuration.value,
                TimeUnit.of(expiryDuration.unit),
            )
        }
    }

    override val size: Int
        get() = set.size

    override fun isEmpty(): Boolean = set.isEmpty()

    override fun contains(o: T): Boolean = set.contains(ExpirableItem(o, Instant.now()))

    override fun iterator(): MutableIterator<T> {
        val tmp = HashSet(set.map { it.item }.toSet())
        return tmp.iterator()
    }

    override fun toArray(): Array<Any> {
        val array = Array<Any>(set.size) {}
        var counter = 0
        for (item in set) {
            array[counter] = item
            counter++
        }
        return array
    }

    override fun <R : Any> toArray(a: Array<out R>): Array<out R> {
        TODO("Not Implemented Yet")
    }

    override fun add(e: T): Boolean = set.add(ExpirableItem(e, Instant.now()))

    override fun remove(o: T): Boolean = set.remove(ExpirableItem(o, Instant.now()))

    override fun containsAll(c: Collection<T>): Boolean = set.containsAll(c.map { ExpirableItem(it, Instant.now()) })

    override fun addAll(c: Collection<T>): Boolean = set.addAll(c.map { ExpirableItem(it, Instant.now()) })

    override fun retainAll(c: Collection<T>): Boolean = set.retainAll(c.map { ExpirableItem(it, Instant.now()) }.toSet())

    override fun removeAll(c: Collection<T>): Boolean = set.retainAll(c.map { ExpirableItem(it, Instant.now()) }.toSet())

    override fun clear() {
        set.clear()
    }
}
