package org.emdev.utils.bytes;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ByteArray {

    private static final int SIZE = 4 * 1024;

    private int size;

    // private Buffer last;
    private final ArrayList<byte[]> buffers = new ArrayList<byte[]>();

    public final DataArrayInputStream in = new DataArrayInputStream();
    public final DataArrayOutputStream out = new DataArrayOutputStream();

    public ByteArray() {
    }

    public int size() {
        return size;
    }

    public void recycle() {
        buffers.clear();
    }

    private class Output extends OutputStream {

        @Override
        public void write(final int value) {
            if (size % SIZE == 0) {
                buffers.add(new byte[SIZE]);
            }
            buffers.get(size / SIZE)[size % SIZE] = (byte) value;
            size++;
        }

        @Override
        public void write(final byte[] buffer, int offset, int len) {
            if (len == 0) {
                return;
            }
            if (size % SIZE == 0) {
                buffers.add(new byte[SIZE]);
            }
            final int firstBuf = size / SIZE;
            final int dstPos = size % SIZE;
            final int rem = SIZE - (dstPos);

            if (len <= rem) {
                System.arraycopy(buffer, offset, buffers.get(firstBuf), dstPos, len);
                size += len;
                return;
            }

            System.arraycopy(buffer, offset, buffers.get(firstBuf), dstPos, rem);
            offset += rem;
            len -= rem;
            size += rem;

            while (len > 0) {
                final byte[] buf = new byte[SIZE];
                buffers.add(buf);
                final int toCopy = Math.min(len, SIZE);
                System.arraycopy(buffer, offset, buf, 0, toCopy);
                offset += toCopy;
                len -= toCopy;
                size += toCopy;
            }
        }

    }

    public class DataArrayOutputStream extends DataOutputStream {

        private DataArrayOutputStream() {
            super(new Output());
        }
    }

    public class DataArrayInputStream {

        protected int pos;

        protected final byte[] temp4 = new byte[4];
        protected final byte[] temp8 = new byte[8];

        private DataArrayInputStream() {
        }

        public int size() {
            return size;
        }

        public int available() {
            return Math.max(0, size - pos);
        }

        public int position() {
            return pos;
        }

        public int position(final int position) {
            final int prev = pos;
            pos = position;
            return prev;
        }

        public int skipInt() {
            pos += 4;
            return pos;
        }

        public byte readByte() throws IOException {
            if (pos < size) {
                return buffers.get(pos / SIZE)[(pos++) % SIZE];
            }
            throw new EOFException();
        }

        public final boolean readBoolean() throws IOException {
            if (pos < size) {
                return buffers.get(pos / SIZE)[(pos++) % SIZE] != 0;
            }
            throw new EOFException();
        }

        public int readInt() throws IOException {
            if (pos + 4 <= size) {
                for (int i = 0; i < 4; i++) {
                    final int index = i + pos;
                    temp4[i] = buffers.get(index / SIZE)[(index) % SIZE];
                }
                final int value = ByteConvertUtils.getInteger(temp4, 0, 4);
                pos += 4;
                return value;
            }
            throw new EOFException();
        }

        public long readLong() throws IOException {
            if (pos + 8 <= size) {
                for (int i = 0; i < 8; i++) {
                    final int index = i + pos;
                    temp8[i] = buffers.get(index / SIZE)[(index) % SIZE];
                }
                final long value = ByteConvertUtils.getLong(temp8, 0, 8);
                pos += 8;
                return value;
            }
            throw new EOFException();
        }

        public float readFloat() throws IOException {
            if (pos + 4 <= size) {
                for (int i = 0; i < 4; i++) {
                    final int index = i + pos;
                    temp4[i] = buffers.get(index / SIZE)[(index) % SIZE];
                }
                final float value = ByteConvertUtils.getFloat(temp4, 0);
                pos += 4;
                return value;
            }
            throw new EOFException();
        }

        public void read(final byte[] bytes) throws IOException {
            if (pos + bytes.length <= size) {
                for (int i = 0; i < bytes.length; i++) {
                    final int index = i + pos;
                    bytes[i] = buffers.get(index / SIZE)[(index) % SIZE];
                }
                pos += bytes.length;
                return;
            }
            throw new EOFException();
        }

    }
}
