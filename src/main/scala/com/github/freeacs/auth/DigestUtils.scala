/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.freeacs.auth

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.binary.StringUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Operations to simplify common {@link MessageDigest} tasks.
 * This class is immutable and thread-safe.
 *
 * @version $Id: DigestUtils.java 1634433 2014-10-27 01:10:47Z ggregory $
 */
object DigestUtils {
  private val STREAM_BUFFER_LENGTH = 1024

  /**
   * Read through an InputStream and returns the digest for the data
   *
   * @param digest
   * The MessageDigest to use (e.g. MD5)
   * @param data
   * Data to digest
   * @return the digest
   * @throws IOException
   * On error reading from the stream
   */
  @throws[IOException]
  private def digest(digest: MessageDigest, data: InputStream) =
    updateDigest(digest, data).digest

  /**
   * Returns a <code>MessageDigest</code> for the given <code>algorithm</code>.
   *
   * @param algorithm
   * the name of the algorithm requested. See <a
   * href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA"
   * >Appendix A in the Java Cryptography Architecture Reference Guide</a> for information about standard
   * algorithm names.
   * @return A digest instance.
   * @see MessageDigest#getInstance(String)
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught.
   */
  def getDigest(algorithm: String): MessageDigest =
    try MessageDigest.getInstance(algorithm)
    catch {
      case e: NoSuchAlgorithmException =>
        throw new IllegalArgumentException(e)
    }

  /**
   * Returns an MD2 MessageDigest.
   *
   * @return An MD2 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught, which should never happen because MD2 is a
   *                built-in algorithm
   * @see MessageDigestAlgorithms#MD2
   * @since 1.7
   */
  def getMd2Digest: MessageDigest = getDigest(MessageDigestAlgorithms.MD2)

  /**
   * Returns an MD5 MessageDigest.
   *
   * @return An MD5 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught, which should never happen because MD5 is a
   *                built-in algorithm
   * @see MessageDigestAlgorithms#MD5
   */
  def getMd5Digest: MessageDigest = getDigest(MessageDigestAlgorithms.MD5)

  /**
   * Returns an SHA-1 digest.
   *
   * @return An SHA-1 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught, which should never happen because SHA-1 is a
   *                built-in algorithm
   * @see MessageDigestAlgorithms#SHA_1
   * @since 1.7
   */
  def getSha1Digest: MessageDigest = getDigest(MessageDigestAlgorithms.SHA_1)

  /**
   * Returns an SHA-256 digest.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @return An SHA-256 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught, which should never happen because SHA-256 is a
   *                built-in algorithm
   * @see MessageDigestAlgorithms#SHA_256
   */
  def getSha256Digest: MessageDigest =
    getDigest(MessageDigestAlgorithms.SHA_256)

  /**
   * Returns an SHA-384 digest.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @return An SHA-384 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught, which should never happen because SHA-384 is a
   *                built-in algorithm
   * @see MessageDigestAlgorithms#SHA_384
   */
  def getSha384Digest: MessageDigest =
    getDigest(MessageDigestAlgorithms.SHA_384)

  /**
   * Returns an SHA-512 digest.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @return An SHA-512 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught, which should never happen because SHA-512 is a
   *                built-in algorithm
   * @see MessageDigestAlgorithms#SHA_512
   */
  def getSha512Digest: MessageDigest =
    getDigest(MessageDigestAlgorithms.SHA_512)

  /**
   * Returns an SHA-1 digest.
   *
   * @return An SHA-1 digest instance.
   * @throws IllegalArgumentException
   * when a { @link NoSuchAlgorithmException} is caught
   * @deprecated Use { @link #getSha1Digest()}
   */
  @deprecated def getShaDigest: MessageDigest = getSha1Digest

  /**
   * Calculates the MD2 digest and returns the value as a 16 element <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return MD2 digest
   * @since 1.7
   */
  def md2(data: Array[Byte]): Array[Byte] = getMd2Digest.digest(data)

  /**
   * Calculates the MD2 digest and returns the value as a 16 element <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return MD2 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.7
   */
  @throws[IOException]
  def md2(data: InputStream): Array[Byte] = digest(getMd2Digest, data)

  /**
   * Calculates the MD2 digest and returns the value as a 16 element <code>byte[]</code>.
   *
   * @param data
   * Data to digest; converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return MD2 digest
   * @since 1.7
   */
  def md2(data: String): Array[Byte] = md2(StringUtils.getBytesUtf8(data))

  /**
   * Calculates the MD2 digest and returns the value as a 32 character hex string.
   *
   * @param data
   * Data to digest
   * @return MD2 digest as a hex string
   * @since 1.7
   */
  def md2Hex(data: Array[Byte]): String = Hex.encodeHexString(md2(data))

  /**
   * Calculates the MD2 digest and returns the value as a 32 character hex string.
   *
   * @param data
   * Data to digest
   * @return MD2 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.7
   */
  @throws[IOException]
  def md2Hex(data: InputStream): String = Hex.encodeHexString(md2(data))

  def md2Hex(data: String): String = Hex.encodeHexString(md2(data))

  /**
   * Calculates the MD5 digest and returns the value as a 16 element <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return MD5 digest
   */
  def md5(data: Array[Byte]): Array[Byte] = getMd5Digest.digest(data)

  /**
   * Calculates the MD5 digest and returns the value as a 16 element <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return MD5 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def md5(data: InputStream): Array[Byte] = digest(getMd5Digest, data)

  /**
   * Calculates the MD5 digest and returns the value as a 16 element <code>byte[]</code>.
   *
   * @param data
   * Data to digest; converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return MD5 digest
   */
  def md5(data: String): Array[Byte] = md5(StringUtils.getBytesUtf8(data))

  /**
   * Calculates the MD5 digest and returns the value as a 32 character hex string.
   *
   * @param data
   * Data to digest
   * @return MD5 digest as a hex string
   */
  def md5Hex(data: Array[Byte]): String = Hex.encodeHexString(md5(data))

  /**
   * Calculates the MD5 digest and returns the value as a 32 character hex string.
   *
   * @param data
   * Data to digest
   * @return MD5 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def md5Hex(data: InputStream): String = Hex.encodeHexString(md5(data))

  def md5Hex(data: String): String = Hex.encodeHexString(md5(data))

  /**
   * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest
   * @deprecated Use { @link #sha1(byte[])}
   */
  @deprecated def sha(data: Array[Byte]): Array[Byte] = sha1(data)

  /**
   * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   * @deprecated Use { @link #sha1(InputStream)}
   */
  @deprecated
  @throws[IOException]
  def sha(data: InputStream): Array[Byte] = sha1(data)

  /**
   * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest
   * @deprecated Use { @link #sha1(String)}
   */
  @deprecated def sha(data: String): Array[Byte] = sha1(data)

  /**
   * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest
   * @since 1.7
   */
  def sha1(data: Array[Byte]): Array[Byte] = getSha1Digest.digest(data)

  /**
   * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.7
   */
  @throws[IOException]
  def sha1(data: InputStream): Array[Byte] = digest(getSha1Digest, data)

  /**
   * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
   *
   * @param data
   * Data to digest; converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return SHA-1 digest
   */
  def sha1(data: String): Array[Byte] = sha1(StringUtils.getBytesUtf8(data))

  /**
   * Calculates the SHA-1 digest and returns the value as a hex string.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest as a hex string
   * @since 1.7
   */
  def sha1Hex(data: Array[Byte]): String = Hex.encodeHexString(sha1(data))

  /**
   * Calculates the SHA-1 digest and returns the value as a hex string.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.7
   */
  @throws[IOException]
  def sha1Hex(data: InputStream): String = Hex.encodeHexString(sha1(data))

  def sha1Hex(data: String): String = Hex.encodeHexString(sha1(data))

  /**
   * Calculates the SHA-256 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-256 digest
   * @since 1.4
   */
  def sha256(data: Array[Byte]): Array[Byte] = getSha256Digest.digest(data)

  /**
   * Calculates the SHA-256 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-256 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def sha256(data: InputStream): Array[Byte] = digest(getSha256Digest, data)

  /**
   * Calculates the SHA-256 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest; converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return SHA-256 digest
   * @since 1.4
   */
  def sha256(data: String): Array[Byte] = sha256(StringUtils.getBytesUtf8(data))

  /**
   * Calculates the SHA-256 digest and returns the value as a hex string.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-256 digest as a hex string
   * @since 1.4
   */
  def sha256Hex(data: Array[Byte]): String = Hex.encodeHexString(sha256(data))

  /**
   * Calculates the SHA-256 digest and returns the value as a hex string.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-256 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def sha256Hex(data: InputStream): String = Hex.encodeHexString(sha256(data))

  def sha256Hex(data: String): String = Hex.encodeHexString(sha256(data))

  /**
   * Calculates the SHA-384 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-384 digest
   * @since 1.4
   */
  def sha384(data: Array[Byte]): Array[Byte] = getSha384Digest.digest(data)

  /**
   * Calculates the SHA-384 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-384 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def sha384(data: InputStream): Array[Byte] = digest(getSha384Digest, data)

  /**
   * Calculates the SHA-384 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest; converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return SHA-384 digest
   * @since 1.4
   */
  def sha384(data: String): Array[Byte] = sha384(StringUtils.getBytesUtf8(data))

  /**
   * Calculates the SHA-384 digest and returns the value as a hex string.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-384 digest as a hex string
   * @since 1.4
   */
  def sha384Hex(data: Array[Byte]): String = Hex.encodeHexString(sha384(data))

  /**
   * Calculates the SHA-384 digest and returns the value as a hex string.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-384 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def sha384Hex(data: InputStream): String = Hex.encodeHexString(sha384(data))

  def sha384Hex(data: String): String = Hex.encodeHexString(sha384(data))

  /**
   * Calculates the SHA-512 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-512 digest
   * @since 1.4
   */
  def sha512(data: Array[Byte]): Array[Byte] = getSha512Digest.digest(data)

  /**
   * Calculates the SHA-512 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-512 digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def sha512(data: InputStream): Array[Byte] = digest(getSha512Digest, data)

  /**
   * Calculates the SHA-512 digest and returns the value as a <code>byte[]</code>.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest; converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return SHA-512 digest
   * @since 1.4
   */
  def sha512(data: String): Array[Byte] = sha512(StringUtils.getBytesUtf8(data))

  /**
   * Calculates the SHA-512 digest and returns the value as a hex string.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-512 digest as a hex string
   * @since 1.4
   */
  def sha512Hex(data: Array[Byte]): String = Hex.encodeHexString(sha512(data))

  /**
   * Calculates the SHA-512 digest and returns the value as a hex string.
   * <p>
   * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
   * </p>
   *
   * @param data
   * Data to digest
   * @return SHA-512 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   */
  @throws[IOException]
  def sha512Hex(data: InputStream): String = Hex.encodeHexString(sha512(data))

  def sha512Hex(data: String): String = Hex.encodeHexString(sha512(data))

  /**
   * Calculates the SHA-1 digest and returns the value as a hex string.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest as a hex string
   * @deprecated Use { @link #sha1Hex(byte[])}
   */
  @deprecated def shaHex(data: Array[Byte]): String = sha1Hex(data)

  /**
   * Calculates the SHA-1 digest and returns the value as a hex string.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest as a hex string
   * @throws IOException
   * On error reading from the stream
   * @since 1.4
   * @deprecated Use { @link #sha1Hex(InputStream)}
   */
  @deprecated
  @throws[IOException]
  def shaHex(data: InputStream): String = sha1Hex(data)

  /**
   * Calculates the SHA-1 digest and returns the value as a hex string.
   *
   * @param data
   * Data to digest
   * @return SHA-1 digest as a hex string
   * @deprecated Use { @link #sha1Hex(String)}
   */
  @deprecated def shaHex(data: String): String = sha1Hex(data)

  /**
   * Updates the given {@link MessageDigest}.
   *
   * @param messageDigest
   * the { @link MessageDigest} to update
   * @param valueToDigest
   * the value to update the { @link MessageDigest} with
   * @return the updated { @link MessageDigest}
   * @since 1.7
   */
  def updateDigest(
      messageDigest: MessageDigest,
      valueToDigest: Array[Byte]
  ): MessageDigest = {
    messageDigest.update(valueToDigest)
    messageDigest
  }

  /**
   * Reads through an InputStream and updates the digest for the data
   *
   * @param digest
   * The MessageDigest to use (e.g. MD5)
   * @param data
   * Data to digest
   * @return the digest
   * @throws IOException
   * On error reading from the stream
   * @since 1.8
   */
  @throws[IOException]
  def updateDigest(digest: MessageDigest, data: InputStream): MessageDigest = {
    val buffer = new Array[Byte](STREAM_BUFFER_LENGTH)
    var read   = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
    while ({
      read > -1
    }) {
      digest.update(buffer, 0, read)
      read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
    }
    digest
  }

  /**
   * Updates the given {@link MessageDigest}.
   *
   * @param messageDigest
   * the { @link MessageDigest} to update
   * @param valueToDigest
   * the value to update the { @link MessageDigest} with;
   *                                 converted to bytes using { @link StringUtils#getBytesUtf8(String)}
   * @return the updated { @link MessageDigest}
   * @since 1.7
   */
  def updateDigest(
      messageDigest: MessageDigest,
      valueToDigest: String
  ): MessageDigest = {
    messageDigest.update(StringUtils.getBytesUtf8(valueToDigest))
    messageDigest
  }
}
