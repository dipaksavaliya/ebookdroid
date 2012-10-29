package org.emdev.utils.bytes;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.emdev.utils.LengthUtils;

public final class ByteConvertUtils {

  /**
   * Fake constructor.
   *
   */
  private ByteConvertUtils() {
  }

  /**
   * Returns an integer value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @return an integer value
   */
  public static int getInteger(final byte[] bytes) {
    return getInteger(bytes, 0, LengthUtils.length(bytes));
  }

  /**
   * Returns an integer value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @param offset
   *          start offset
   * @param length
   *          number of bytes to read
   * @return an integer value
   */
  public static int getInteger(final byte[] bytes, final int offset, final int length) {
    int value = 0;
    if (length > 0) {
      int shift = 0;
      for (int i = Math.min(4, length) - 1; i > 0; i--) {
        value += ((bytes[offset + i] & 0xFF) << shift);
        shift += 8;
      }
      value += ((bytes[offset]) << shift);
    }
    return value;
  }

  /**
   * Returns a long value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @return a long value
   */
  public static long getLong(final byte[] bytes) {
    return getLong(bytes, 0, LengthUtils.length(bytes));
  }

  /**
   * Returns a long value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @param offset
   *          start offset
   * @param length
   *          number of bytes to read
   * @return a long value
   */
  public static long getLong(final byte[] bytes, final int offset, final int length) {
    long value = 0;
    if (length > 0) {
      int shift = 0;
      for (int i = Math.min(8, length) - 1; i > 0; i--) {
        value += ((long) (bytes[offset + i] & 0xFF) << shift);
        shift += 8;
      }
      value += ((long) (bytes[offset]) << shift);
    }
    return value;
  }

  /**
   * Returns an unsigned integer value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @return an integer value
   */
  public static long getUnsignedInt(final byte[] bytes) {
    return getUnsignedInt(bytes, 0, LengthUtils.length(bytes));
  }

  /**
   * Returns an unsigned integer value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @param offset
   *          start offset
   * @param length
   *          number of bytes to read
   * @return an integer value
   */
  public static long getUnsignedInt(final byte[] bytes, final int offset, final int length) {
    long value = 0;
    if (length > 0) {
      int shift = 0;
      for (int i = Math.min(8, length) - 1; i >= 0; i--) {
        value += ((long) (bytes[offset + i] & 0xFF) << shift);
        shift += 8;
      }
    }
    return value;
  }

  /**
   * Returns an unsigned long value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @return a long value
   */
  public static BigInteger getUnsignedLong(final byte[] bytes) {
    return getUnsignedLong(bytes, 0, LengthUtils.length(bytes));
  }

  /**
   * Returns an unsigned long value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @param offset
   *          start offset
   * @param length
   *          number of bytes to read
   * @return a long value
   */
  public static BigInteger getUnsignedLong(final byte[] bytes, final int offset, final int length) {
    byte[] val = bytes;
    if (offset != 0 || length != 8) {
      val = new byte[length];
      System.arraycopy(bytes, offset, val, 0, length);
    }
    return new BigInteger(1, val);
  }

  /**
   * Returns a float value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @return a float value
   */
  public static float getFloat(final byte[] bytes) {
    return Float.intBitsToFloat(getInteger(bytes, 0, 4));
  }

  /**
   * Returns a float value read from the given bytes.
   *
   * @param bytes
   *          bytes to read
   * @return a float value
   */
  public static float getFloat(final byte[] bytes, int offset) {
    return Float.intBitsToFloat(getInteger(bytes, offset, 4));
  }

  /**
   * Converts HEX string to bytes
   *
   * @param hexString
   *          string containing HEX values
   * @return an array of bytes
   */
  public static byte[] hexToBytes(final String hexString) {
    if (hexString == null) {
      return null;
    }
    int effLength = hexString.length() / 2;
    final ByteArrayOutputStream stream = new ByteArrayOutputStream(effLength);
    char[] chars = hexString.toCharArray();
    for (int i = 0; i < effLength; i++) {
      final int offset = i * 2;
      final int byteValue = (Character.digit(chars[offset], 16) << 4) + Character.digit(chars[offset + 1], 16);
      stream.write(byteValue & 0xFF);
    }
    return stream.toByteArray();
  }

  /**
   * Converts bytes to HEX string.
   *
   * @param bytes
   *          byte array
   * @return string
   */
  public static String toHexString(final byte... bytes) {
    return toUnsignedString(bytes, 4, 2);
  }

  /**
   * Converts bytes to a string.
   *
   * @param bytes
   *          byte array
   * @param shift
   *          radix shift
   * @param length
   *          length of a byte in symbols
   * @return string
   */
  private static String toUnsignedString(final byte[] bytes, final int shift, final int length) {
    final StringBuilder result = new StringBuilder(bytes.length * length);

    final char[] buf = new char[32];
    final int radix = 1 << shift;
    final int mask = radix - 1;

    for (int index = 0; index < bytes.length; index++) {
      int charPos = 32;
      int integer = bytes[index] & 0xFF;
      do {
        buf[--charPos] = digits[integer & mask];
        integer >>>= shift;
      } while (integer != 0);
      int l = (32 - charPos);
      while (l < length) {
        buf[--charPos] = '0';
        l++;
      }

      result.append(buf, charPos, l);
    }

    return result.toString();
  }

  /**
   * All possible chars for representing a number as a String
   */
  private static final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
      'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

}
