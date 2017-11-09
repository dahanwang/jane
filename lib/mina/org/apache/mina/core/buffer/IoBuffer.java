/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.mina.core.buffer;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

/**
 * A byte buffer used by MINA applications.
 * <p>
 * This is a replacement for {@link ByteBuffer}.
 * Please refer to {@link ByteBuffer} documentation for preliminary usage.
 * MINA does not use NIO {@link ByteBuffer} directly for two reasons:
 * <ul>
 *   <li>It doesn't provide useful getters and putters such as <code>fill</code>,
 *       <code>get/putString</code>, and <code>get/putAsciiInt()</code> enough.</li>
 * </ul>
 *
 * <h2>Allocation</h2>
 * <p>
 *   You can allocate a new heap buffer.
 *
 *   <pre>
 *     IoBuffer buf = IoBuffer.allocate(1024, false);
 *   </pre>
 *
 *   You can also allocate a new direct buffer:
 *
 *   <pre>
 *     IoBuffer buf = IoBuffer.allocate(1024, true);
 *   </pre>
 *
 *   or you can set the default buffer type.
 *
 *   <pre>
 *     // Allocate heap buffer by default.
 *     IoBuffer.setUseDirectBuffer(false);
 *
 *     // A new heap buffer is returned.
 *     IoBuffer buf = IoBuffer.allocate(1024);
 *   </pre>
 *
 * <h2>Wrapping existing NIO buffers and arrays</h2>
 * <p>
 *   This class provides a few <tt>wrap(...)</tt> methods that wraps any NIO
 *   buffers and byte arrays.
 *
 * <h2>Changing Buffer Allocation Policy</h2>
 * <p>
 *   The {@link IoBufferAllocator} interface lets you override the default buffer
 *   management behavior. There is only one allocator provided out-of-the-box:
 *   <ul>
 *     <li>{@link SimpleBufferAllocator} (default)</li>
 *   </ul>
 *   You can implement your own allocator and use it by calling
 *   {@link #setAllocator(IoBufferAllocator)}.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public abstract class IoBuffer implements Comparable<IoBuffer>, WriteRequest {
	/** The allocator used to create new buffers */
	private static IoBufferAllocator allocator = SimpleBufferAllocator.instance;

	/** A flag indicating which type of buffer we are using: heap or direct */
	private static boolean useDirectBuffer;

	/**
	 * @return the allocator used by existing and new buffers
	 */
	public static IoBufferAllocator getAllocator() {
		return allocator;
	}

	/**
	 * Sets the allocator used by existing and new buffers
	 *
	 * @param newAllocator the new allocator to use
	 */
	public static void setAllocator(IoBufferAllocator newAllocator) {
		if (newAllocator == null) {
			throw new IllegalArgumentException("allocator");
		}

		IoBufferAllocator oldAllocator = allocator;
		allocator = newAllocator;
		oldAllocator.dispose();
	}

	/**
	 * @return <tt>true</tt> if and only if a direct buffer is allocated by
	 * default when the type of the new buffer is not specified.
	 * The default value is <tt>false</tt>.
	 */
	public static boolean isUseDirectBuffer() {
		return useDirectBuffer;
	}

	/**
	 * Sets if a direct buffer should be allocated by default when the type of
	 * the new buffer is not specified. The default value is <tt>false</tt>.
	 *
	 * @param useDirectBuffer Tells if direct buffers should be allocated
	 */
	public static void setUseDirectBuffer(boolean useDirectBuffer) {
		IoBuffer.useDirectBuffer = useDirectBuffer;
	}

	/**
	 * Returns the direct or heap buffer which is capable to store the specified amount of bytes.
	 *
	 * @param capacity the capacity of the buffer
	 * @return a IoBuffer which can hold up to capacity bytes
	 *
	 * @see #setUseDirectBuffer(boolean)
	 */
	public static IoBuffer allocate(int capacity) {
		return allocate(capacity, useDirectBuffer);
	}

	/**
	 * Returns a direct or heap IoBuffer which can contain the specified number of bytes.
	 *
	 * @param capacity the capacity of the buffer
	 * @param isDirectBuffer <tt>true</tt> to get a direct buffer,
	 *                       <tt>false</tt> to get a heap buffer.
	 * @return a direct or heap  IoBuffer which can hold up to capacity bytes
	 */
	public static IoBuffer allocate(int capacity, boolean isDirectBuffer) {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity: " + capacity);
		}

		return allocator.allocate(capacity, isDirectBuffer);
	}

	public static IoBuffer reallocate(IoBuffer buf, int capacity) {
		if (buf.capacity() >= capacity) {
			return buf;
		}

		int limit = buf.limit();
		int pos = buf.position();
		ByteOrder bo = buf.order();

		IoBuffer newBuf = getAllocator().allocate(capacity, buf.isDirect());
		buf.clear();
		newBuf.put(buf);
		newBuf.limit(limit);
		newBuf.position(pos);
		newBuf.order(bo);
		buf.free();

		return newBuf;
	}

	public static IoBuffer reallocateNew(IoBuffer buf, int capacity) {
		return reallocate(buf, capacity).clear();
	}

	public static IoBuffer reallocateRemain(IoBuffer buf, int remainCapacity) {
		buf = reallocate(buf, buf.position() + remainCapacity);
		return buf.limit(buf.capacity());
	}

	/**
	 * Wraps the specified NIO {@link ByteBuffer} into a MINA buffer (either direct or heap).
	 *
	 * @param nioBuffer The {@link ByteBuffer} to wrap
	 * @return a IoBuffer containing the bytes stored in the {@link ByteBuffer}
	 */
	public static IoBuffer wrap(ByteBuffer nioBuffer) {
		return allocator.wrap(nioBuffer);
	}

	/**
	 * Wraps the specified byte array into a MINA heap buffer.
	 * Note that the byte array is not copied,
	 * so any modification done on it will be visible by both sides.
	 *
	 * @param byteArray The byte array to wrap
	 * @return a heap IoBuffer containing the byte array
	 */
	public static IoBuffer wrap(byte[] byteArray) {
		return wrap(ByteBuffer.wrap(byteArray));
	}

	/**
	 * Wraps the specified byte array into MINA heap buffer.
	 * We just wrap the bytes starting from offset up to offset + length.
	 * Note that the byte array is not copied,
	 * so any modification done on it will be visible by both sides.
	 *
	 * @param byteArray The byte array to wrap
	 * @param offset The starting point in the byte array
	 * @param length The number of bytes to store
	 * @return a heap IoBuffer containing the selected part of the byte array
	 */
	public static IoBuffer wrap(byte[] byteArray, int offset, int length) {
		return wrap(ByteBuffer.wrap(byteArray, offset, length));
	}

	@Override
	public final Object getMessage() {
		return this;
	}

	@Override
	public final WriteFuture getFuture() {
		return DefaultWriteRequest.UNUSED_FUTURE;
	}

	/**
	 * @return the underlying NIO {@link ByteBuffer} instance.
	 */
	public abstract ByteBuffer buf();

	/**
	 * @see ByteBuffer#duplicate()
	 *
	 * @return the modified IoBuffer
	 */
	public abstract IoBuffer duplicate();

	/**
	 * Declares this buffer and all its derived buffers are not used anymore
	 * so that it can be reused by some {@link IoBufferAllocator} implementations.
	 * It is not mandatory to call this method,
	 * but you might want to invoke this method for maximum performance.
	 */
	public abstract void free();

	/**
	 * @see ByteBuffer#isDirect()
	 *
	 * @return <tt>True</tt> if this is a direct buffer
	 */
	public final boolean isDirect() {
		return buf().isDirect();
	}

	/**
	 * @see ByteBuffer#capacity()
	 *
	 * @return the buffer capacity
	 */
	public final int capacity() {
		return buf().capacity();
	}

	/**
	 * @see java.nio.Buffer#position()
	 *
	 * @return The current position in the buffer
	 */
	public final int position() {
		return buf().position();
	}

	/**
	 * @see java.nio.Buffer#position(int)
	 *
	 * @param newPosition Sets the new position in the buffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer position(int newPosition) {
		buf().position(newPosition);
		return this;
	}

	/**
	 * @see java.nio.Buffer#limit()
	 *
	 * @return the modified IoBuffer's limit
	 */
	public final int limit() {
		return buf().limit();
	}

	/**
	 * @see java.nio.Buffer#limit(int)
	 *
	 * @param newLimit The new buffer's limit
	 * @return the modified IoBuffer
	 */
	public final IoBuffer limit(int newLimit) {
		buf().limit(newLimit);
		return this;
	}

	/**
	 * @see java.nio.Buffer#clear()
	 *
	 * @return the modified IoBuffer
	 */
	public final IoBuffer clear() {
		buf().clear();
		return this;
	}

	/**
	 * Clears this buffer and fills its content with <tt>NUL</tt>.
	 * The position is set to zero, the limit is set to the capacity.
	 *
	 * @return the modified IoBuffer
	 */
	public final IoBuffer sweep() {
		clear();
		return fillAndReset(remaining());
	}

	/**
	 * double Clears this buffer and fills its content with <tt>value</tt>.
	 * The position is set to zero, the limit is set to the capacity.
	 *
	 * @param value The value to put in the buffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer sweep(byte value) {
		clear();
		return fillAndReset(value, remaining());
	}

	/**
	 * @see java.nio.Buffer#flip()
	 *
	 * @return the modified IoBuffer
	 */
	public final IoBuffer flip() {
		buf().flip();
		return this;
	}

	/**
	 * @see java.nio.Buffer#rewind()
	 *
	 * @return the modified IoBuffer
	 */
	public final IoBuffer rewind() {
		buf().rewind();
		return this;
	}

	/**
	 * @see java.nio.Buffer#remaining()
	 *
	 * @return The remaining bytes in the buffer
	 */
	public final int remaining() {
		ByteBuffer byteBuffer = buf();
		return byteBuffer.limit() - byteBuffer.position();
	}

	/**
	 * @see java.nio.Buffer#hasRemaining()
	 *
	 * @return <tt>true</tt> if there are some remaining bytes in the buffer
	 */
	public final boolean hasRemaining() {
		ByteBuffer byteBuffer = buf();
		return byteBuffer.limit() > byteBuffer.position();
	}

	/**
	 * @see ByteBuffer#hasArray()
	 *
	 * @return <tt>true</tt> if the {@link #array()} method will return a byte[]
	 */
	public final boolean hasArray() {
		return buf().hasArray();
	}

	/**
	 * @see ByteBuffer#array()
	 *
	 * @return A byte[] if this IoBuffer supports it
	 */
	public final byte[] array() {
		return buf().array();
	}

	/**
	 * @see ByteBuffer#arrayOffset()
	 *
	 * @return The offset in the returned byte[] when the {@link #array()} method is called
	 */
	public final int arrayOffset() {
		return buf().arrayOffset();
	}

	/**
	 * @see ByteBuffer#get()
	 *
	 * @return The byte at the current position
	 */
	public final byte get() {
		return buf().get();
	}

	/**
	 * Reads one unsigned byte as a integer.
	 *
	 * @return the unsigned byte at the current position
	 */
	public final int getUnsigned() {
		return get() & 0xff;
	}

	/**
	 * @see ByteBuffer#put(byte)
	 *
	 * @param b The byte to put in the buffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer put(byte b) {
		buf().put(b);
		return this;
	}

	/**
	 * @see ByteBuffer#get(int)
	 *
	 * @param index The position for which we want to read a byte
	 * @return the byte at the given position
	 */
	public final byte get(int index) {
		return buf().get(index);
	}

	/**
	 * Reads one byte as an unsigned integer.
	 *
	 * @param index The position for which we want to read an unsigned byte
	 * @return the unsigned byte at the given position
	 */
	public final int getUnsigned(int index) {
		return get(index) & 0xff;
	}

	/**
	 * @see ByteBuffer#put(int, byte)
	 *
	 * @param index The position where the byte will be put
	 * @param b The byte to put
	 * @return the modified IoBuffer
	 */
	public final IoBuffer put(int index, byte b) {
		buf().put(index, b);
		return this;
	}

	/**
	 * @see ByteBuffer#get(byte[], int, int)
	 *
	 * @param dst The destination buffer
	 * @param offset The position in the original buffer
	 * @param length The number of bytes to copy
	 * @return the modified IoBuffer
	 */
	public final IoBuffer get(byte[] dst, int offset, int length) {
		buf().get(dst, offset, length);
		return this;
	}

	/**
	 * @see ByteBuffer#get(byte[])
	 *
	 * @param dst The byte[] that will contain the read bytes
	 * @return the IoBuffer
	 */
	public final IoBuffer get(byte[] dst) {
		return get(dst, 0, dst.length);
	}

	/**
	 * Writes the content of the specified <tt>src</tt> into this buffer.
	 *
	 * @param src The source ByteBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer put(ByteBuffer src) {
		buf().put(src);
		return this;
	}

	/**
	 * Writes the content of the specified <tt>src</tt> into this buffer.
	 *
	 * @param src The source IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer put(IoBuffer src) {
		return put(src.buf());
	}

	/**
	 * @see ByteBuffer#put(byte[], int, int)
	 *
	 * @param src The byte[] to put
	 * @param offset The position in the source
	 * @param length The number of bytes to copy
	 * @return the modified IoBuffer
	 */
	public final IoBuffer put(byte[] src, int offset, int length) {
		buf().put(src, offset, length);
		return this;
	}

	/**
	 * @see ByteBuffer#put(byte[])
	 *
	 * @param src The byte[] to put
	 * @return the modified IoBuffer
	 */
	public final IoBuffer put(byte[] src) {
		return put(src, 0, src.length);
	}

	/**
	 * @see ByteBuffer#compact()
	 *
	 * @return the modified IoBuffer
	 */
	public final IoBuffer compact() {
		if (capacity() == 0) {
			return this;
		}

		buf().compact();
		return this;
	}

	/**
	 * @see ByteBuffer#order()
	 *
	 * @return the IoBuffer ByteOrder
	 */
	public final ByteOrder order() {
		return buf().order();
	}

	/**
	 * @see ByteBuffer#order(ByteOrder)
	 *
	 * @param bo The new ByteBuffer to use for this IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer order(ByteOrder bo) {
		buf().order(bo);
		return this;
	}

	/**
	 * @see ByteBuffer#getChar()
	 *
	 * @return The char at the current position
	 */
	public final char getChar() {
		return buf().getChar();
	}

	/**
	 * @see ByteBuffer#putChar(char)
	 *
	 * @param value The char to put at the current position
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putChar(char value) {
		buf().putChar(value);
		return this;
	}

	/**
	 * @see ByteBuffer#getChar(int)
	 *
	 * @param index The index in the IoBuffer where we will read a char from
	 * @return the char at 'index' position
	 */
	public final char getChar(int index) {
		return buf().getChar(index);
	}

	/**
	 * @see ByteBuffer#putChar(int, char)
	 *
	 * @param index The index in the IoBuffer where we will put a char in
	 * @param value The char to put at the current position
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putChar(int index, char value) {
		buf().putChar(index, value);
		return this;
	}

	/**
	 * @see ByteBuffer#getShort()
	 *
	 * @return The read short
	 */
	public final short getShort() {
		return buf().getShort();
	}

	/**
	 * Reads two bytes unsigned integer.
	 *
	 * @return The read unsigned short
	 */
	public final int getUnsignedShort() {
		return getShort() & 0xffff;
	}

	/**
	 * @see ByteBuffer#putShort(short)
	 *
	 * @param value The short to put at the current position
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putShort(short value) {
		buf().putShort(value);
		return this;
	}

	/**
	 * @see ByteBuffer#getShort()
	 *
	 * @param index The index in the IoBuffer where we will read a short from
	 * @return The read short
	 */
	public final short getShort(int index) {
		return buf().getShort(index);
	}

	/**
	 * Reads two bytes unsigned integer.
	 *
	 * @param index The index in the IoBuffer where we will read an unsigned short from
	 * @return the unsigned short at the given position
	 */
	public final int getUnsignedShort(int index) {
		return getShort(index) & 0xffff;
	}

	/**
	 * @see ByteBuffer#putShort(int, short)
	 *
	 * @param index The position at which the short should be written
	 * @param value The short to put at the current position
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putShort(int index, short value) {
		buf().putShort(index, value);
		return this;
	}

	/**
	 * @see ByteBuffer#getInt()
	 *
	 * @return The int read
	 */
	public final int getInt() {
		return buf().getInt();
	}

	/**
	 * Reads four bytes unsigned integer.
	 *
	 * @return The unsigned int read
	 */
	public final long getUnsignedInt() {
		return getInt() & 0xffffffffL;
	}

	/**
	 * Relative <i>get</i> method for reading a medium int value.
	 * <p>
	 * Reads the next three bytes at this buffer's current position, composing them into an int value
	 * according to the current byte order, and then increments the position by three.
	 *
	 * @return The medium int value at the buffer's current position
	 */
	public final int getMediumInt() {
		byte b1 = get();
		byte b2 = get();
		byte b3 = get();
		return ByteOrder.BIG_ENDIAN.equals(order()) ?
			getMediumInt(b1, b2, b3) :
			getMediumInt(b3, b2, b1);
	}

	/**
	 * Relative <i>get</i> method for reading an unsigned medium int value.
	 * <p>
	 * Reads the next three bytes at this buffer's current position, composing them into an int value
	 * according to the current byte order, and then increments the position by three.
	 *
	 * @return The unsigned medium int value at the buffer's current position
	 */
	public final int getUnsignedMediumInt() {
		int b1 = getUnsigned();
		int b2 = getUnsigned();
		int b3 = getUnsigned();
		return ByteOrder.BIG_ENDIAN.equals(order()) ?
			b1 << 16 | b2 << 8 | b3 :
			b3 << 16 | b2 << 8 | b1;
	}

	/**
	 * Absolute <i>get</i> method for reading a medium int value.
	 * <p>
	 * Reads the next three bytes at this buffer's current position, composing
	 * them into an int value according to the current byte order.
	 *
	 * @param index The index from which the medium int will be read
	 * @return The medium int value at the given index
	 * @throws IndexOutOfBoundsException If <tt>index</tt> is negative or not smaller than the buffer's limit
	 */
	public final int getMediumInt(int index) {
		byte b1 = get(index);
		byte b2 = get(index + 1);
		byte b3 = get(index + 2);
		return ByteOrder.BIG_ENDIAN.equals(order()) ?
			getMediumInt(b1, b2, b3) :
			getMediumInt(b3, b2, b1);
	}

	private static int getMediumInt(byte b1, byte b2, byte b3) {
		int ret = (b1 << 16) & 0xff0000 | (b2 << 8) & 0xff00 | b3 & 0xff;
		// Check to see if the medium int is negative (high bit in b1 set)
		if ((b1 & 0x80) == 0x80) {
			// Make the the whole int negative
			ret |= 0xff000000;
		}
		return ret;
	}

	/**
	 * Absolute <i>get</i> method for reading an unsigned medium int value.
	 * <p>
	 * Reads the next three bytes at this buffer's current position, composing
	 * them into an int value according to the current byte order.
	 *
	 * @param index The index from which the unsigned medium int will be read
	 * @return The unsigned medium int value at the given index
	 * @throws IndexOutOfBoundsException If <tt>index</tt> is negative or not smaller than the buffer's limit
	 */
	public final int getUnsignedMediumInt(int index) {
		int b1 = getUnsigned(index);
		int b2 = getUnsigned(index + 1);
		int b3 = getUnsigned(index + 2);
		return ByteOrder.BIG_ENDIAN.equals(order()) ?
			b1 << 16 | b2 << 8 | b3 :
			b3 << 16 | b2 << 8 | b1;
	}

	/**
	 * Relative <i>put</i> method for writing a medium int value.
	 * <p>
	 * Writes three bytes containing the given int value, in the current byte order,
	 * into this buffer at the current position, and then increments the position by three.
	 *
	 * @param value The medium int value to be written
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putMediumInt(int value) {
		byte b1 = (byte) (value >> 16);
		byte b2 = (byte) (value >> 8);
		byte b3 = (byte) value;

		if (ByteOrder.BIG_ENDIAN.equals(order())) {
			put(b1).put(b2).put(b3);
		} else {
			put(b3).put(b2).put(b1);
		}

		return this;
	}

	/**
	 * Absolute <i>put</i> method for writing a medium int value.
	 * <p>
	 * Writes three bytes containing the given int value, in the current byte order, into this buffer at the given index.
	 *
	 * @param index The index at which the bytes will be written
	 * @param value The medium int value to be written
	 * @return the modified IoBuffer
	 * @throws IndexOutOfBoundsException If <tt>index</tt> is negative or not smaller than the buffer's limit, minus three
	 */
	public final IoBuffer putMediumInt(int index, int value) {
		byte b1 = (byte) (value >> 16);
		byte b2 = (byte) (value >> 8);
		byte b3 = (byte) value;

		if (ByteOrder.BIG_ENDIAN.equals(order())) {
			put(index, b1).put(index + 1, b2).put(index + 2, b3);
		} else {
			put(index, b3).put(index + 1, b2).put(index + 2, b1);
		}

		return this;
	}

	/**
	 * @see ByteBuffer#putInt(int)
	 *
	 * @param value The int to put at the current position
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putInt(int value) {
		buf().putInt(value);
		return this;
	}

	/**
	 * Writes an unsigned int into the ByteBuffer
	 *
	 * @param value the byte to write
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putUnsignedInt(byte value) {
		buf().putInt(value & 0xff);
		return this;
	}

	/**
	 * Writes an unsigned int into the ByteBuffer at a specified position
	 *
	 * @param index the position in the buffer to write the value
	 * @param value the byte to write
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putUnsignedInt(int index, byte value) {
		buf().putInt(index, value & 0xff);
		return this;
	}

	/**
	 * Writes an unsigned int into the ByteBuffer
	 *
	 * @param value the short to write
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putUnsignedInt(short value) {
		buf().putInt(value & 0xffff);
		return this;
	}

	/**
	 * Writes an unsigned int into the ByteBuffer at a specified position
	 *
	 * @param index the position in the buffer to write the value
	 * @param value the short to write
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putUnsignedInt(int index, short value) {
		buf().putInt(index, value & 0xffff);
		return this;
	}

	/**
	 * Writes an unsigned short into the ByteBuffer
	 *
	 * @param value the byte to write
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putUnsignedShort(byte value) {
		buf().putShort((short) (value & 0xff));
		return this;
	}

	/**
	 * Writes an unsigned Short into the ByteBuffer at a specified position
	 *
	 * @param index the position in the buffer to write the value
	 * @param value the byte to write
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putUnsignedShort(int index, byte value) {
		buf().putShort(index, (short) (value & 0xff));
		return this;
	}

	/**
	 * @see ByteBuffer#getInt(int)
	 *
	 * @param index The index in the IoBuffer where we will read an int from
	 * @return the int at the given position
	 */
	public final int getInt(int index) {
		return buf().getInt(index);
	}

	/**
	 * Reads four bytes unsigned integer.
	 *
	 * @param index The index in the IoBuffer where we will read an unsigned int from
	 * @return The long at the given position
	 */
	public final long getUnsignedInt(int index) {
		return getInt(index) & 0xffffffffL;
	}

	/**
	 * @see ByteBuffer#putInt(int, int)
	 *
	 * @param index The position where to put the int
	 * @param value The int to put in the IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putInt(int index, int value) {
		buf().putInt(index, value);
		return this;
	}

	/**
	 * @see ByteBuffer#getLong()
	 *
	 * @return The long at the current position
	 */
	public final long getLong() {
		return buf().getLong();
	}

	/**
	 * @see ByteBuffer#putLong(int, long)
	 *
	 * @param value The log to put in the IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putLong(long value) {
		buf().putLong(value);
		return this;
	}

	/**
	 * @see ByteBuffer#getLong(int)
	 *
	 * @param index The index in the IoBuffer where we will read a long from
	 * @return the long at the given position
	 */
	public final long getLong(int index) {
		return buf().getLong(index);
	}

	/**
	 * @see ByteBuffer#putLong(int, long)
	 *
	 * @param index The position where to put the long
	 * @param value The long to put in the IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putLong(int index, long value) {
		buf().putLong(index, value);
		return this;
	}

	/**
	 * @see ByteBuffer#getFloat()
	 *
	 * @return the float at the current position
	 */
	public final float getFloat() {
		return buf().getFloat();
	}

	/**
	 * @see ByteBuffer#putFloat(float)
	 *
	 * @param value The float to put in the IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putFloat(float value) {
		buf().putFloat(value);
		return this;
	}

	/**
	 * @see ByteBuffer#getFloat(int)
	 *
	 * @param index The index in the IoBuffer where we will read a float from
	 * @return The float at the given position
	 */
	public final float getFloat(int index) {
		return buf().getFloat(index);
	}

	/**
	 * @see ByteBuffer#putFloat(int, float)
	 *
	 * @param index The position where to put the float
	 * @param value The float to put in the IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putFloat(int index, float value) {
		buf().putFloat(index, value);
		return this;
	}

	/**
	 * @see ByteBuffer#getDouble()
	 *
	 * @return the double at the current position
	 */
	public final double getDouble() {
		return buf().getDouble();
	}

	/**
	 * @see ByteBuffer#putDouble(double)
	 *
	 * @param value The double to put at the IoBuffer current position
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putDouble(double value) {
		buf().putDouble(value);
		return this;
	}

	/**
	 * @see ByteBuffer#getDouble(int)
	 *
	 * @param index The position where to get the double from
	 * @return The double at the given position
	 */
	public final double getDouble(int index) {
		return buf().getDouble(index);
	}

	/**
	 * @see ByteBuffer#putDouble(int, double)
	 *
	 * @param index The position where to put the double
	 * @param value The double to put in the IoBuffer
	 * @return the modified IoBuffer
	 */
	public final IoBuffer putDouble(int index, double value) {
		buf().putDouble(index, value);
		return this;
	}

	/**
	 * Returns hexdump of this buffer.
	 * The data and pointer are not changed as a result of this method call.
	 *
	 * @return hexidecimal representation of this buffer
	 */
	public final String getHexDump() {
		return getHexDump(Integer.MAX_VALUE);
	}

	/** The getHexdump digits lookup table */
	private static final byte[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * Return hexdump of this buffer with limited length.
	 *
	 * @param lengthLimit The maximum number of bytes to dump from the current buffer position.
	 * @return hexidecimal representation of this buffer
	 */
	public final String getHexDump(int lengthLimit) {
		if (lengthLimit == 0) {
			throw new IllegalArgumentException("lengthLimit: " + lengthLimit + " (expected: 1+)");
		}

		boolean truncate = (remaining() > lengthLimit);
		int size = (truncate ? lengthLimit : remaining());
		if (size <= 0) {
			return "empty";
		}

		StringBuilder out = new StringBuilder(size * 3 + 2);

		int mark = position();

		for (;;) {
			int byteValue = get() & 0xff;
			out.append((char) digits[byteValue >> 4]);
			out.append((char) digits[byteValue & 15]);
			if (--size <= 0) {
				break;
			}
			out.append(' ');
		}

		position(mark);

		if (truncate) {
			out.append("...");
		}

		return out.toString();
	}

	////////////////////////////////
	// String getters and putters //
	////////////////////////////////

	/**
	 * Reads a <code>NUL</code>-terminated string from this buffer using the specified <code>decoder</code> and returns it.
	 * This method reads until the limit of this buffer if no <tt>NUL</tt> is found.
	 *
	 * @param decoder The {@link CharsetDecoder} to use
	 * @return the read String
	 * @exception CharacterCodingException Thrown when an error occurred while decoding the buffer
	 */
	public final String getString(CharsetDecoder decoder) throws CharacterCodingException {
		if (!hasRemaining()) {
			return "";
		}

		boolean utf16 = decoder.charset().name().startsWith("UTF-16");

		int oldPos = position();
		int oldLimit = limit();
		int end = -1;
		int newPos;

		if (!utf16) {
			end = indexOf((byte) 0);
			if (end < 0) {
				newPos = end = oldLimit;
			} else {
				newPos = end + 1;
			}
		} else {
			int i = oldPos;
			for (;;) {
				boolean wasZero = get(i) == 0;
				i++;

				if (i >= oldLimit) {
					break;
				}

				if (get(i) != 0) {
					i++;
					if (i >= oldLimit) {
						break;
					}

					continue;
				}

				if (wasZero) {
					end = i - 1;
					break;
				}
			}

			if (end < 0) {
				newPos = end = oldPos + ((oldLimit - oldPos) & 0xfffffffe);
			} else {
				newPos = (end + 2 <= oldLimit ? end + 2 : end);
			}
		}

		if (oldPos == end) {
			position(newPos);
			return "";
		}

		limit(end);
		decoder.reset();

		int expectedLength = (int) (remaining() * decoder.averageCharsPerByte()) + 1;
		CharBuffer out = CharBuffer.allocate(expectedLength);
		for (;;) {
			CoderResult cr = (hasRemaining() ? decoder.decode(buf(), out, true) : decoder.flush(out));

			if (cr.isUnderflow()) {
				break;
			}

			if (cr.isOverflow()) {
				CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
				out.flip();
				o.put(out);
				out = o;
				continue;
			}

			if (cr.isError()) {
				// Revert the buffer back to the previous state.
				limit(oldLimit);
				position(oldPos);
				cr.throwException();
			}
		}

		limit(oldLimit);
		position(newPos);
		return out.flip().toString();
	}

	private static void checkFieldSize(int fieldSize) {
		if (fieldSize < 0) {
			throw new IllegalArgumentException("fieldSize cannot be negative: " + fieldSize);
		}
	}

	/**
	 * Reads a <code>NUL</code>-terminated string from this buffer using the specified <code>decoder</code> and returns it.
	 *
	 * @param fieldSize the maximum number of bytes to read
	 * @param decoder The {@link CharsetDecoder} to use
	 * @return the read String
	 * @exception CharacterCodingException Thrown when an error occurred while decoding the buffer
	 */
	public final String getString(int fieldSize, CharsetDecoder decoder) throws CharacterCodingException {
		checkFieldSize(fieldSize);

		if (fieldSize == 0) {
			return "";
		}

		if (!hasRemaining()) {
			return "";
		}

		boolean utf16 = decoder.charset().name().startsWith("UTF-16");

		if (utf16 && (fieldSize & 1) != 0) {
			throw new IllegalArgumentException("fieldSize is not even.");
		}

		int oldPos = position();
		int oldLimit = limit();
		int end = oldPos + fieldSize;

		if (oldLimit < end) {
			throw new BufferUnderflowException();
		}

		int i;
		if (!utf16) {
			for (i = oldPos; i < end; i++) {
				if (get(i) == 0) {
					break;
				}
			}
		} else {
			for (i = oldPos; i < end; i += 2) {
				if (get(i) == 0 && get(i + 1) == 0) {
					break;
				}
			}
		}
		limit(i);

		if (!hasRemaining()) {
			limit(oldLimit);
			position(end);
			return "";
		}
		decoder.reset();

		int expectedLength = (int) (remaining() * decoder.averageCharsPerByte()) + 1;
		CharBuffer out = CharBuffer.allocate(expectedLength);
		for (;;) {
			CoderResult cr = (hasRemaining() ? decoder.decode(buf(), out, true) : decoder.flush(out));

			if (cr.isUnderflow()) {
				break;
			}

			if (cr.isOverflow()) {
				CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
				out.flip();
				o.put(out);
				out = o;
				continue;
			}

			if (cr.isError()) {
				// Revert the buffer back to the previous state.
				limit(oldLimit);
				position(oldPos);
				cr.throwException();
			}
		}

		limit(oldLimit);
		position(end);
		return out.flip().toString();
	}

	/**
	 * Writes the content of <code>in</code> into this buffer using the specified <code>encoder</code>.
	 * This method doesn't terminate string with <tt>NUL</tt>. You have to do it by yourself.
	 *
	 * @param val The CharSequence to put in the IoBuffer
	 * @param encoder The CharsetEncoder to use
	 * @return The modified IoBuffer
	 * @throws CharacterCodingException When we have an error while decoding the String
	 */
	public final IoBuffer putString(CharSequence val, CharsetEncoder encoder) throws CharacterCodingException {
		if (val.length() == 0) {
			return this;
		}

		CharBuffer in = CharBuffer.wrap(val);
		encoder.reset();

		for (;;) {
			CoderResult cr = (in.hasRemaining() ? encoder.encode(in, buf(), true) : encoder.flush(buf()));

			if (cr.isUnderflow()) {
				break;
			}
			cr.throwException();
		}
		return this;
	}

	/**
	 * Writes the content of <code>in</code> into this buffer as a
	 * <code>NUL</code>-terminated string using the specified <code>encoder</code>.
	 * <p>
	 * If the charset name of the encoder is UTF-16, you cannot specify odd <code>fieldSize</code>,
	 * and this method will append two <code>NUL</code>s as a terminator.
	 * <p>
	 * Please note that this method doesn't terminate with <code>NUL</code> if
	 * the input string is longer than <tt>fieldSize</tt>.
	 *
	 * @param val The CharSequence to put in the IoBuffer
	 * @param fieldSize the maximum number of bytes to write
	 * @param encoder The CharsetEncoder to use
	 * @return The modified IoBuffer
	 * @throws CharacterCodingException When we have an error while decoding the String
	 */
	public final IoBuffer putString(CharSequence val, int fieldSize, CharsetEncoder encoder) throws CharacterCodingException {
		checkFieldSize(fieldSize);

		if (fieldSize == 0) {
			return this;
		}

		boolean utf16 = encoder.charset().name().startsWith("UTF-16");

		if (utf16 && (fieldSize & 1) != 0) {
			throw new IllegalArgumentException("fieldSize is not even.");
		}

		int oldLimit = limit();
		int end = position() + fieldSize;

		if (oldLimit < end) {
			throw new BufferOverflowException();
		}

		if (val.length() == 0) {
			put((byte) 0);
			if (utf16) {
				put((byte) 0);
			}
			position(end);
			return this;
		}

		CharBuffer in = CharBuffer.wrap(val);
		limit(end);
		encoder.reset();

		for (;;) {
			CoderResult cr = (in.hasRemaining() ? encoder.encode(in, buf(), true) : encoder.flush(buf()));

			if (cr.isUnderflow() || cr.isOverflow()) {
				break;
			}
			cr.throwException();
		}

		limit(oldLimit);

		if (position() < end) {
			put((byte) 0);
			if (utf16) {
				put((byte) 0);
			}
		}

		position(end);
		return this;
	}

	/////////////////////
	// IndexOf methods //
	/////////////////////

	/**
	 * Returns the first occurrence position of the specified byte from the
	 * current position to the current limit.
	 *
	 * @param b The byte we are looking for
	 * @return <tt>-1</tt> if the specified byte is not found
	 */
	public final int indexOf(byte b) {
		if (hasArray()) {
			int arrayOffset = arrayOffset();
			int beginPos = arrayOffset + position();
			int limit = arrayOffset + limit();
			byte[] array = array();

			for (int i = beginPos; i < limit; i++) {
				if (array[i] == b) {
					return i - arrayOffset;
				}
			}
		} else {
			int beginPos = position();
			int limit = limit();

			for (int i = beginPos; i < limit; i++) {
				if (get(i) == b) {
					return i;
				}
			}
		}

		return -1;
	}

	//////////////////////////
	// Skip or fill methods //
	//////////////////////////

	/**
	 * Forwards the position of this buffer as the specified <code>size</code> bytes.
	 *
	 * @param size The added size
	 * @return The modified IoBuffer
	 */
	public final IoBuffer skip(int size) {
		return position(position() + size);
	}

	/**
	 * Fills this buffer with the specified value. This method moves buffer position forward.
	 *
	 * @param value The value to fill the IoBuffer with
	 * @param size The added size
	 * @return The modified IoBuffer
	 */
	public final IoBuffer fill(byte value, int size) {
		int q = size >>> 3;
		int r = size & 7;

		if (q > 0) {
			int intValue = value & 0xff | (value << 8) & 0xff00 | (value << 16) & 0xff0000 | value << 24;
			long longValue = intValue & 0xffffffffL | (long)intValue << 32;

			for (int i = q; i > 0; i--) {
				putLong(longValue);
			}
		}

		q = r >>> 2;
		r = r & 3;

		if (q > 0) {
			int intValue = value & 0xff | (value << 8) & 0xff00 | (value << 16) & 0xff0000 | (value << 24);
			putInt(intValue);
		}

		q = r >> 1;
		r = r & 1;

		if (q > 0) {
			short shortValue = (short) (value & 0xff | (value << 8));
			putShort(shortValue);
		}

		if (r > 0) {
			put(value);
		}

		return this;
	}

	/**
	 * Fills this buffer with the specified value. This method does not change buffer position.
	 *
	 * @param value The value to fill the IoBuffer with
	 * @param size The added size
	 * @return The modified IoBuffer
	 */
	public final IoBuffer fillAndReset(byte value, int size) {
		int pos = position();
		try {
			fill(value, size);
		} finally {
			position(pos);
		}
		return this;
	}

	/**
	 * Fills this buffer with <code>NUL (0x00)</code>. This method moves buffer position forward.
	 *
	 * @param size The added size
	 * @return The modified IoBuffer
	 */
	public final IoBuffer fill(int size) {
		int q = size >>> 3;
		int r = size & 7;

		for (int i = q; i > 0; i--) {
			putLong(0L);
		}

		q = r >>> 2;
		r = r & 3;

		if (q > 0) {
			putInt(0);
		}

		q = r >> 1;
		r = r & 1;

		if (q > 0) {
			putShort((short) 0);
		}

		if (r > 0) {
			put((byte) 0);
		}

		return this;
	}

	/**
	 * Fills this buffer with <code>NUL (0x00)</code>. This method does not change buffer position.
	 *
	 * @param size The added size
	 * @return The modified IoBuffer
	 */
	public final IoBuffer fillAndReset(int size) {
		int pos = position();
		try {
			fill(size);
		} finally {
			position(pos);
		}

		return this;
	}

	@Override
	public int hashCode() {
		int h = 1;
		int p = position();
		for (int i = limit() - 1; i >= p; i--) {
			h = 31 * h + get(i);
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IoBuffer)) {
			return false;
		}

		IoBuffer that = (IoBuffer) o;
		if (this.remaining() != that.remaining()) {
			return false;
		}

		int p = this.position();
		for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
			byte v1 = this.get(i);
			byte v2 = that.get(j);
			if (v1 != v2) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(IoBuffer that) {
		int n = this.position() + Math.min(this.remaining(), that.remaining());
		for (int i = this.position(), j = that.position(); i < n; i++, j++) {
			byte v1 = this.get(i);
			byte v2 = that.get(j);
			if (v1 == v2) {
				continue;
			}
			if (v1 < v2) {
				return -1;
			}

			return +1;
		}
		return this.remaining() - that.remaining();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		buf.append(isDirect() ? "DirectBuffer[pos=" : "HeapBuffer[pos=");
		buf.append(position());
		buf.append(" lim=").append(limit());
		buf.append(" cap=").append(capacity());
		buf.append(": ").append(getHexDump(16)).append(']');
		return buf.toString();
	}
}
