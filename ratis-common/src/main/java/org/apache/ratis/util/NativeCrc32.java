/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ratis.util;

import org.apache.ratis.protocol.ChecksumException;

import java.nio.ByteBuffer;

/**
 * Wrapper around JNI support code to do checksum computation
 * natively.
 */
final class NativeCrc32 {
  private NativeCrc32() {
  }

  /**
   * Return true if the JNI-based native CRC extensions are available.
   */
  public static boolean isAvailable() {
    if (System.getProperty("os.arch").toLowerCase().startsWith("sparc")) {
      return false;
    } else {
      return NativeCodeLoader.isNativeCodeLoaded();
    }
  }

  /**
   * Verify the given buffers of data and checksums, and throw an exception
   * if any checksum is invalid. The buffers given to this function should
   * have their position initially at the start of the data, and their limit
   * set at the end of the data. The position, limit, and mark are not
   * modified.
   * @param bytesPerSum the chunk size (eg 512 bytes)
   * @param checksumType the DataChecksum type constant (NULL is not supported)
   * @param sums the DirectByteBuffer pointing at the beginning of the
   *             stored checksums
   * @param data the DirectByteBuffer pointing at the beginning of the
   *             data to check
   * @param basePos the position in the file where the data buffer starts
   * @param fileName the name of the file being verified
   * @throws ChecksumException if there is an invalid checksum
   */
  public static void verifyChunkedSums(int bytesPerSum, int checksumType,
      ByteBuffer sums, ByteBuffer data, String fileName, long basePos)
      throws ChecksumException {
    nativeComputeChunkedSums(bytesPerSum, checksumType,
        sums, sums.position(),
        data, data.position(), data.remaining(),
        fileName, basePos, true);
  }

  @SuppressWarnings("parameternumber")
  public static void verifyChunkedSumsByteArray(int bytesPerSum,
      int checksumType, byte[] sums, int sumsOffset, byte[] data,
      int dataOffset, int dataLength, String fileName, long basePos)
      throws ChecksumException {
    nativeComputeChunkedSumsByteArray(bytesPerSum, checksumType,
        sums, sumsOffset,
        data, dataOffset, dataLength,
        fileName, basePos, true);
  }

  public static void calculateChunkedSums(int bytesPerSum, int checksumType,
      ByteBuffer sums, ByteBuffer data) {
    nativeComputeChunkedSums(bytesPerSum, checksumType,
        sums, sums.position(),
        data, data.position(), data.remaining(),
        "", 0, false);
  }

  public static void calculateChunkedSumsByteArray(int bytesPerSum,
      int checksumType, byte[] sums, int sumsOffset, byte[] data,
      int dataOffset, int dataLength) {
    nativeComputeChunkedSumsByteArray(bytesPerSum, checksumType,
        sums, sumsOffset,
        data, dataOffset, dataLength,
        "", 0, false);
  }

  @SuppressWarnings("parameternumber")
  private static native void nativeComputeChunkedSums(
      int bytesPerSum, int checksumType,
      ByteBuffer sums, int sumsOffset,
      ByteBuffer data, int dataOffset, int dataLength,
      String fileName, long basePos, boolean verify);

  @SuppressWarnings("parameternumber")
  private static native void nativeComputeChunkedSumsByteArray(
      int bytesPerSum, int checksumType,
      byte[] sums, int sumsOffset,
      byte[] data, int dataOffset, int dataLength,
      String fileName, long basePos, boolean verify);

  // Copy the constants over from DataChecksum so that javah will pick them up
  // and make them available in the native code header.
  public static final int CHECKSUM_CRC32 = 1;  //DataChecksum.CHECKSUM_CRC32
  public static final int CHECKSUM_CRC32C = 2; //DataChecksum.CHECKSUM_CRC32C
}
