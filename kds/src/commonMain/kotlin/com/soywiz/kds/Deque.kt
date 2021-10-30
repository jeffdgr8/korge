package com.soywiz.kds

import com.soywiz.kds.internal.*
import kotlin.math.*

typealias Deque<TGen> = TGenDeque<TGen>

// AUTOGENERATED: DO NOT MODIFY MANUALLY!


typealias CircularList<TGen> = TGenDeque<TGen>

/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
open class TGenDeque<TGen>(initialCapacity: Int) : MutableCollection<TGen> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: Array<TGen> = arrayOfNulls<Any>(initialCapacity) as Array<TGen>
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = arrayOfNulls<Any>(this.data.size * 2) as Array<TGen>
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: Array<TGen>, istart: Int, o: Array<TGen>, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<TGen>) {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addAllFirst(items: Collection<TGen>) {
        resizeIfRequiredFor(items.size)
        _start = (_start - items.size) umod capacity
        _size += items.size
        var pos = _start
        for (it in items) data[pos++ umod capacity] = it
    }

    fun addFirst(item: TGen) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: TGen) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): TGen {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): TGen {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    fun removeAt(index: Int): TGen {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: TGen): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<TGen>): Boolean = true.apply { addAll(elements as Iterable<TGen>) }
    override fun clear() { _size = 0 }
    override fun remove(element: TGen): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<TGen>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<TGen>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<TGen>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: TGen get() = data[_start]
    val last: TGen get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: TGen): Unit { data[internalIndex(index)] = value }
    operator fun get(index: Int): TGen = data[internalIndex(index)]

    override fun contains(element: TGen): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: TGen): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<TGen>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<TGen> {
        val that = this
        return object : MutableIterator<TGen> {
            var index = 0
            override fun next(): TGen = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove(): Unit { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is TGenDeque<*/*_TGen_*/>) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}


// Int

typealias IntCircularList = IntDeque

/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
open class IntDeque(initialCapacity: Int) : MutableCollection<Int> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: IntArray = IntArray(initialCapacity) as IntArray
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = IntArray(this.data.size * 2) as IntArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: IntArray, istart: Int, o: IntArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Int>) {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addAllFirst(items: Collection<Int>) {
        resizeIfRequiredFor(items.size)
        _start = (_start - items.size) umod capacity
        _size += items.size
        var pos = _start
        for (it in items) data[pos++ umod capacity] = it
    }

    fun addFirst(item: Int) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Int) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): Int {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Int {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    fun removeAt(index: Int): Int {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Int): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Int>): Boolean = true.apply { addAll(elements as Iterable<Int>) }
    override fun clear() { _size = 0 }
    override fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Int>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Int>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Int>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Int get() = data[_start]
    val last: Int get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Int): Unit { data[internalIndex(index)] = value }
    operator fun get(index: Int): Int = data[internalIndex(index)]

    override fun contains(element: Int): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Int): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Int> {
        val that = this
        return object : MutableIterator<Int> {
            var index = 0
            override fun next(): Int = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove(): Unit { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is IntDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}


// Double

typealias DoubleCircularList = DoubleDeque

/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
open class DoubleDeque(initialCapacity: Int) : MutableCollection<Double> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: DoubleArray = DoubleArray(initialCapacity) as DoubleArray
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = DoubleArray(this.data.size * 2) as DoubleArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: DoubleArray, istart: Int, o: DoubleArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Double>) {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addAllFirst(items: Collection<Double>) {
        resizeIfRequiredFor(items.size)
        _start = (_start - items.size) umod capacity
        _size += items.size
        var pos = _start
        for (it in items) data[pos++ umod capacity] = it
    }

    fun addFirst(item: Double) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Double) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): Double {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Double {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    fun removeAt(index: Int): Double {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Double): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Double>): Boolean = true.apply { addAll(elements as Iterable<Double>) }
    override fun clear() { _size = 0 }
    override fun remove(element: Double): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Double>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Double>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Double>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Double get() = data[_start]
    val last: Double get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Double): Unit { data[internalIndex(index)] = value }
    operator fun get(index: Int): Double = data[internalIndex(index)]

    override fun contains(element: Double): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Double): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Double>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Double> {
        val that = this
        return object : MutableIterator<Double> {
            var index = 0
            override fun next(): Double = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove(): Unit { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is DoubleDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}


// Float

typealias FloatCircularList = FloatDeque

/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
open class FloatDeque(initialCapacity: Int) : MutableCollection<Float> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: FloatArray = FloatArray(initialCapacity) as FloatArray
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = FloatArray(this.data.size * 2) as FloatArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: FloatArray, istart: Int, o: FloatArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Float>) {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addAllFirst(items: Collection<Float>) {
        resizeIfRequiredFor(items.size)
        _start = (_start - items.size) umod capacity
        _size += items.size
        var pos = _start
        for (it in items) data[pos++ umod capacity] = it
    }

    fun addFirst(item: Float) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Float) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): Float {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Float {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    fun removeAt(index: Int): Float {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Float): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Float>): Boolean = true.apply { addAll(elements as Iterable<Float>) }
    override fun clear() { _size = 0 }
    override fun remove(element: Float): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Float>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Float>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Float>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Float get() = data[_start]
    val last: Float get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Float): Unit { data[internalIndex(index)] = value }
    operator fun get(index: Int): Float = data[internalIndex(index)]

    override fun contains(element: Float): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Float): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Float>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Float> {
        val that = this
        return object : MutableIterator<Float> {
            var index = 0
            override fun next(): Float = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove(): Unit { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is FloatDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}


// Byte

typealias ByteCircularList = ByteDeque

/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
open class ByteDeque(initialCapacity: Int) : MutableCollection<Byte> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: ByteArray = ByteArray(initialCapacity) as ByteArray
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = ByteArray(this.data.size * 2) as ByteArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: ByteArray, istart: Int, o: ByteArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Byte>) {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addAllFirst(items: Collection<Byte>) {
        resizeIfRequiredFor(items.size)
        _start = (_start - items.size) umod capacity
        _size += items.size
        var pos = _start
        for (it in items) data[pos++ umod capacity] = it
    }

    fun addFirst(item: Byte) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Byte) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun addFirstAll(items: ByteArray, offset: Int = 0, size: Int = items.size - offset) {
        for (n in 0 until size) addFirst(items[size - offset + n - 1])
    }

    fun addLastAll(items: ByteArray, offset: Int = 0, size: Int = items.size - offset) {
        for (n in 0 until size) addLast(items[offset + n])
    }

    fun addAll(items: ByteArray, offset: Int = 0, size: Int = items.size - offset) {
        addLastAll(items, offset, size)
    }

    fun removeFirst(): Byte {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Byte {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    fun removeAt(index: Int): Byte {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Byte): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Byte>): Boolean = true.apply { addAll(elements as Iterable<Byte>) }
    override fun clear() { _size = 0 }
    override fun remove(element: Byte): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Byte>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Byte>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Byte>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Byte get() = data[_start]
    val last: Byte get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Byte): Unit { data[internalIndex(index)] = value }
    operator fun get(index: Int): Byte = data[internalIndex(index)]

    override fun contains(element: Byte): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Byte): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Byte>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Byte> {
        val that = this
        return object : MutableIterator<Byte> {
            var index = 0
            override fun next(): Byte = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove(): Unit { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is ByteDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}
