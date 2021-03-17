package org.kodein.memory.io

import kotlin.math.min

public interface Writeable {

    public val position: Int

    public fun requestCanWrite(needed: Int)

    public fun writeByte(value: Byte)
    public fun writeShort(value: Short)
    public fun writeInt(value: Int)
    public fun writeLong(value: Long)

    public fun writeBytes(src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    public fun writeBytes(src: ReadMemory, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    public fun writeBytes(src: Readable, length: Int)

    public fun flush()
}

public interface CursorWriteable : Writeable {
    public fun skip(count: Int)
}

public fun Writeable.writeBytes(src: CursorReadable): Int {
    val count = src.remaining
    writeBytes(src, count)
    return count
}

public fun Writeable.writeFloat(value: Float): Unit = writeInt(value.toBits())
public fun Writeable.writeDouble(value: Double): Unit = writeLong(value.toBits())

@ExperimentalUnsignedTypes
public fun Writeable.writeUByte(value: UByte): Unit = writeByte(value.toByte())
@ExperimentalUnsignedTypes
public fun Writeable.writeUShort(value: UShort): Unit = writeShort(value.toShort())
@ExperimentalUnsignedTypes
public fun Writeable.writeUInt(value: UInt): Unit = writeInt(value.toInt())
@ExperimentalUnsignedTypes
public fun Writeable.writeULong(value: ULong): Unit = writeLong(value.toLong())
@ExperimentalUnsignedTypes
public fun Writeable.writeUBytes(src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset): Unit = writeBytes(src.asByteArray(), srcOffset, length)

public fun Writeable.writeBytesBuffered(src: Readable, length: Int, bufferSize: Int = 8 * 1024) {
    val buffer = ByteArray(min(length, bufferSize))
    var left = length
    while (left > 0) {
        val read = min(left, buffer.size)
        src.readBytes(buffer, 0, read)
        this.writeBytes(buffer, 0, read)
        left -= read
    }
}
