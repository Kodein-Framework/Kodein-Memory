package org.kodein.memory.io

import org.kodein.memory.Closeable
import kotlin.math.ceil


public interface ReusableMemory {
    /**
     * On requireCanWrite: checks that the needed bytes are available after position.
     * If not, expands the buffer.
     * Attention: When expanding the buffer, only the content BEFORE position is copied.
     * This means that calling `requireCanWrite` as first call in `write` is guaranteed without copy.
     *
     * @return A ReadMemory that is only valid until the next call to `write`,
     */
    public fun slice(write: CursorWriteable.() -> Unit): ReadMemory

    public val bytesCopied: Int

    public companion object {
        public operator fun invoke(initialCapacity: Int, alloc: (Int) -> Memory): ReusableMemory = ReusableMemoryImpl(initialCapacity, alloc)
        public fun array(initialCapacity: Int): ReusableMemory = ReusableMemory(initialCapacity, Memory::array)
    }
}

public interface ReusableAllocation : ReusableMemory, Closeable {
    public companion object {
        public operator fun invoke(initialCapacity: Int, alloc: (Int) -> Allocation): ReusableAllocation = ReusableAllocationImpl(initialCapacity, alloc)
        public fun native(initialCapacity: Int): ReusableAllocation = ReusableAllocationImpl(initialCapacity, Allocation::native)
    }
}


private open class ReusableMemoryImpl<M : Memory>(private val initialCapacity: Int, private val alloc: (Int) -> M): ReusableMemory {

    var memory = alloc(initialCapacity)

    override var bytesCopied = 0

    final override fun slice(write: CursorWriteable.() -> Unit): ReadMemory {
        val w = W()
        w.write()
        return memory.slice(0, w.position)
    }

    open fun close(memory: M) {}

    inner class W : CursorWriteable {
        override var position: Int = 0

        override fun requestCanWrite(needed: Int) {
            val totalNeeded = position + needed

            if (totalNeeded <= memory.size) return

            val factor = ceil(totalNeeded.toDouble() / initialCapacity.toDouble()).toInt()

            val previousMemory = memory
            memory = alloc(factor * initialCapacity)

            try {
                if (position > 0) {
                    memory.setBytes(0, previousMemory, length = position)
                    bytesCopied += position
                }
            } finally {
                close(previousMemory)
            }
        }

        private inline fun <T> writeValue(size: Int, value: T, setValue: Memory.(Int, T) -> Unit) {
            requestCanWrite(size)
            memory.setValue(position, value)
            position += size
        }
        override fun writeByte(value: Byte) = writeValue(1, value, Memory::setByte)
        override fun writeShort(value: Short) = writeValue(2, value, Memory::setShort)
        override fun writeInt(value: Int) = writeValue(4, value, Memory::setInt)
        override fun writeLong(value: Long) = writeValue(8, value, Memory::setLong)

        override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
            requestCanWrite(length)
            memory.setBytes(position, src, srcOffset, length)
            position += length
        }

        override fun writeBytes(src: ReadMemory, srcOffset: Int, length: Int) {
            requestCanWrite(length)
            memory.setBytes(position, src, srcOffset, length)
            position += length
        }

        override fun writeBytes(src: Readable, length: Int) {
            requestCanWrite(length)
            memory.setBytes(position, src, length)
            position += length
        }

        override fun flush() {}

        override fun skip(count: Int) {
            require(count >= 0) { "count: $count < 0" }
            requestCanWrite(count)
            position += count
        }
    }
}

private class ReusableAllocationImpl(initialCapacity: Int, alloc: (Int) -> Allocation) : ReusableMemoryImpl<Allocation>(initialCapacity, alloc), ReusableAllocation {
    override fun close(memory: Allocation) { memory.close() }
    override fun close() { memory.close() }
}
