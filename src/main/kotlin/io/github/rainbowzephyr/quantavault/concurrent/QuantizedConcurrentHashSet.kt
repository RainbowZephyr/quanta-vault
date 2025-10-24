package io.github.rainbowzephyr.quantavault.concurrent

import io.github.rainbowzephyr.quantavault.ExpirableItem
import io.github.rainbowzephyr.quantavault.ExpiryDuration
import io.github.rainbowzephyr.quantavault.QuantizedStructure
import jakarta.validation.constraints.NotEmpty
import java.time.Duration
import java.time.Instant
import java.util.Set
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit


class QuantizedConcurrentHashSet<T> : QuantizedStructure<T>, Set<T> {
    @NotEmpty
    private val set: ConcurrentHashMap<ExpirableItem<T>, Boolean>
    private val lock = Any()

//    constructor(
//        expiryExpiryDuration: ExpiryDuration, sizeLimit: Int, callback: Runnable, forceEviction: Boolean
//    ) : super(expiryExpiryDuration, sizeLimit, callback, forceEviction) {
//        this.set = HashSet()
//        initializeScheduler()
//    }

    constructor(expiryExpiryDuration: ExpiryDuration, callback: Runnable, forceEviction: Boolean) : super(
        expiryExpiryDuration, callback, forceEviction
    ) {
        this.set = ConcurrentHashMap()
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
            val runnable = Runnable {
                val itemsToRemove = ArrayList<ExpirableItem<T>>()

                set.forEach { (i, v) ->
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
                runnable, expiryDuration.value, expiryDuration.value, TimeUnit.of(expiryDuration.unit)
            )
        }
    }

    override val size: Int
        get() = set.size

    override fun isEmpty(): Boolean {
        return set.isEmpty()
    }

    override fun contains(o: T): Boolean {
        return set.contains(ExpirableItem(o, Instant.now()))
    }

    override fun iterator(): MutableIterator<T> {
        val tmp = set.keys.map { it.item }.toMutableList()
        return tmp.iterator()
    }

    override fun toArray(): Array<Any> {
        val array = Array<Any>(set.size) {}
        var counter = 0
        for (item in set.keys) {
            array[counter] = item.item as Any
            counter++
        }
        return array
    }

    override fun <R : Any> toArray(a: Array<out R>): Array<out R> {
        TODO("Not Implemented Yet")
    }

    override fun add(e: T): Boolean {
        val item = ExpirableItem(e, Instant.now())
        if (set.containsKey(item)) {
            return false
        } else {
            set[item] = true
            return true
        }
    }

    override fun remove(o: T): Boolean {
        val item = ExpirableItem(o, Instant.now())
        if (!set.containsKey(item)) {
            return false
        } else {
            set.remove(item)
            return true
        }
    }

    override fun containsAll(c: Collection<T>): Boolean {
        return set.keys.containsAll(c.map { ExpirableItem(it, Instant.now()) })
    }

    override fun addAll(c: Collection<T>): Boolean {
        if(containsAll(c)){
            return false
        }

        set.putAll(c.map { ExpirableItem(it, Instant.now()) to true })
        return true
    }

    override fun retainAll(c: Collection<T>): Boolean {
        val removable = CopyOnWriteArrayList<ExpirableItem<T>>()
        val retainedSet = HashSet(c)

        synchronized(lock) {
            set.forEach { (item, v) ->
                if (!retainedSet.contains(item.item)) {
                    removable.add(item)
                }
            }

            if (removable.isEmpty()) {
                return false
            } else {
                removable.forEach { set.remove(it) }
                return true
            }
        }
    }

    override fun removeAll(c: Collection<T>): Boolean {
        val removable = CopyOnWriteArrayList<ExpirableItem<T>>()
        val retainedSet = HashSet(c)

        synchronized(lock) {
            set.forEach { (item, v) ->
                if (retainedSet.contains(item.item)) {
                    removable.add(item)
                }
            }

            if (removable.isEmpty()) {
                return false
            } else {
                removable.forEach { set.remove(it) }
                return true
            }
        }
    }

    override fun clear() {
        set.clear()
    }
}