/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 * Builder class for sequences of bytes.
 *
 * @since 5.0
 */
public final class ByteArrayBuilder {

    private CharsetEncoder charsetEncoder;
    private ByteBuffer buffer;

    public ByteArrayBuilder() {
    }

    public ByteArrayBuilder(final int initialCapacity) {
        this.buffer = ByteBuffer.allocate(initialCapacity);
    }

    public int capacity() {
        return this.buffer != null ? this.buffer.capacity() : 0;
    }

    static ByteBuffer ensureFreeCapacity(final ByteBuffer buffer, final int capacity) {
        if (buffer == null) {
            return ByteBuffer.allocate(capacity);
        }
        if (buffer.remaining() < capacity) {
            final ByteBuffer newBuffer = ByteBuffer.allocate(buffer.position() + capacity);
            buffer.flip();
            newBuffer.put(buffer);
            return newBuffer;
        }
        return buffer;
    }

    static ByteBuffer encode(
            final ByteBuffer buffer, final CharBuffer in, final CharsetEncoder encoder) throws CharacterCodingException {

        final int capacity = (int) (in.remaining() * encoder.averageBytesPerChar());
        ByteBuffer out = ensureFreeCapacity(buffer, capacity);
        for (;;) {
            CoderResult result = in.hasRemaining() ? encoder.encode(in, out, true) : CoderResult.UNDERFLOW;
            if (result.isError()) {
                result.throwException();
            }
            if (result.isUnderflow()) {
                result = encoder.flush(out);
            }
            if (result.isUnderflow()) {
                break;
            }
            if (result.isOverflow()) {
                out = ensureFreeCapacity(out, capacity);
            }
        }
        return out;
    }

    public void ensureFreeCapacity(final int freeCapacity) {
        this.buffer = ensureFreeCapacity(this.buffer, freeCapacity);
    }

    private void doAppend(final CharBuffer charBuffer) {
        if (this.charsetEncoder == null) {
            this.charsetEncoder = StandardCharsets.US_ASCII.newEncoder()
                    .onMalformedInput(CodingErrorAction.IGNORE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        this.charsetEncoder.reset();
        try {
            this.buffer = encode(this.buffer, charBuffer, this.charsetEncoder);
        } catch (final CharacterCodingException ex) {
            // Should never happen
            throw new IllegalStateException("Unexpected character coding error", ex);
        }
    }

    public ByteArrayBuilder charset(final Charset charset) {
        if (charset == null) {
            this.charsetEncoder = null;
        } else {
            this.charsetEncoder = charset.newEncoder()
                    .onMalformedInput(CodingErrorAction.IGNORE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        return this;
    }

    public ByteArrayBuilder append(final byte[] b, final int off, final int len) {
        if (b == null) {
            return this;
        }
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) < 0) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.length);
        }
        ensureFreeCapacity(len);
        this.buffer.put(b, off, len);
        return this;
    }

    public ByteArrayBuilder append(final byte[] b) {
        if (b == null) {
            return this;
        }
        return append(b, 0, b.length);
    }

    public ByteArrayBuilder append(final CharBuffer charBuffer) {
        if (charBuffer == null) {
            return this;
        }
        doAppend(charBuffer);
        return this;
    }

    public ByteArrayBuilder append(final char[] b, final int off, final int len) {
        if (b == null) {
            return this;
        }
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) < 0) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.length);
        }
        return append(CharBuffer.wrap(b, off, len));
    }

    public ByteArrayBuilder append(final char[] b) {
        if (b == null) {
            return this;
        }
        return append(b, 0, b.length);
    }

    public ByteArrayBuilder append(final String s) {
        if (s == null) {
            return this;
        }
        return append(CharBuffer.wrap(s));
    }

    public ByteBuffer toByteBuffer() {
        return this.buffer != null ? this.buffer.duplicate() : ByteBuffer.allocate(0);
    }

    public byte[] toByteArray() {
        if (this.buffer == null) {
            return new byte[] {};
        }
        this.buffer.flip();
        final byte[] b = new byte[this.buffer.remaining()];
        this.buffer.get(b);
        this.buffer.clear();
        return b;
    }

    public void reset() {
        if (this.charsetEncoder != null) {
            this.charsetEncoder.reset();
        }
        if (this.buffer != null) {
            this.buffer.clear();
        }
    }

    @Override
    public String toString() {
        return this.buffer != null ? this.buffer.toString() : "null";
    }

}
