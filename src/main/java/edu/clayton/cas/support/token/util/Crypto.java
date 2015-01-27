package edu.clayton.cas.support.token.util;

import edu.clayton.cas.support.token.keystore.Key;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class Crypto {
  private final static Logger log = LoggerFactory.getLogger(Crypto.class);

  /**
   * Returns an ASCII string that can be used for encrypting/decrypting
   * data with the AES-256 algorithm. The given seed <strong>does not</strong>
   * guarantee the same result for subsequent invocations.
   *
   * @param seed A string to seed the generator with.
   * @return A unique ASCII string that can be used as an AES-256 key, or null.
   */
  public static String generateAes256KeyWithSeed(String seed) {
    String returnKey = null;

    try {

      //get salt
      String salt = Crypto.generateSalt();

      PBEKeySpec password = new PBEKeySpec(seed.toCharArray(), salt.getBytes(), 65536, 256);
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      PBEKey key = (PBEKey) factory.generateSecret(password);
      SecretKey secretKey = new SecretKeySpec(key.getEncoded(), "AES");

      StringBuffer keyBuffer = new StringBuffer();
      for (byte b : secretKey.getEncoded()) {
        int i = Integer.parseInt(String.format("%d", b));
        int j = (Math.abs(i) % 54) + 40; // A chunk of the ASCII table.

        // Clean up a few characters that could be problematic in JSON
        // or a query parameter. Others should be URL encoded by the user.
        switch (j) {
          case 47:
            // Replace '/' with '!'.
            j = 33;
            break;
          case 61:
            // Replace '=' with 'z'.
            j = 122;
          case 92:
            // Replace '\' with '#'.
            j = 35;
            break;
        }

        keyBuffer.append((char) j);
      }
      returnKey = keyBuffer.toString();
    } catch (InvalidKeySpecException e) {
      log.error("Could not find desired key specification.");
      log.debug(e.toString());
    } catch (NoSuchAlgorithmException e) {
      log.error("Could not find encryption algorithm.");
      log.debug(e.toString());
    }

    return returnKey;
  }

  /**
   * Returns a {@link Base64} encoded encrypted string.
   *
   * @param input The string to encrypt.
   * @param key The key to use for encryption.
   * @return The encrypted encoded string.
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public static String encryptWithKey(String input, String key)
      throws NoSuchPaddingException,
      NoSuchAlgorithmException,
      InvalidKeyException,
      BadPaddingException,
      IllegalBlockSizeException,
      InvalidAlgorithmParameterException
  {
    byte[] crypted = null;
    // Create a random initialization vector.
    SecureRandom random = new SecureRandom();
    byte[] randBytes = new byte[16];
    random.nextBytes(randBytes);
    IvParameterSpec iv = new IvParameterSpec(randBytes);

    SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skey, iv);

    byte[] ivBytes = iv.getIV();
    byte[] inputBytes = input.getBytes();
    byte[] plaintext = new byte[ivBytes.length + inputBytes.length];

    // Prepend the IV to the ciphertext.
    System.arraycopy(ivBytes, 0, plaintext, 0, ivBytes.length);
    System.arraycopy(inputBytes, 0, plaintext, ivBytes.length, inputBytes.length);

    crypted = cipher.doFinal(plaintext);

    return new String(Base64.encodeBase64(crypted));
  }

  /**
   * Decrypts a {@link Base64} encoded encrypted string.
   *
   * @param string The encoded string to decrypt.
   * @param key The {@link Key} to use for decryption.
   * @return The decrypted string.
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public static String decryptEncodedStringWithKey(String string, Key key)
      throws NoSuchPaddingException,
      NoSuchAlgorithmException,
      InvalidKeyException,
      BadPaddingException,
      IllegalBlockSizeException,
      InvalidAlgorithmParameterException
  {
    String decryptedString;

    log.debug("Base64 string = `{}`", string);
    byte[] decryptedStringData;
    byte[] rawData = Base64.decodeBase64(string);
    byte[] iv = new byte[16];
    byte[] cipherText = new byte[rawData.length - iv.length];

    System.arraycopy(rawData, 0, iv, 0, 16);
    System.arraycopy(rawData, 16, cipherText, 0, cipherText.length);

    log.debug("iv = `{}`", Crypto.toHex(iv));
    log.debug("cipherText.length = `{}`", cipherText.length);
    log.debug("cipherText = \n{}", Crypto.toHex(cipherText));
    
    SecretKeySpec skey = new SecretKeySpec(key.data(), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));
    decryptedStringData = cipher.doFinal(cipherText);
    decryptedString = new String(decryptedStringData);

    return decryptedString;
  }

  /**
   * Returns a hexadecimal {@link String} representation of
   * a given @{code byte} array. Primarily used for debug
   * logging.
   *
   * @param bytes The byte array to convert.
   * @return The hexadecimal representation as a {@linkplain String}.
   */
  public static String toHex(byte[] bytes) {
    StringBuffer buffer = new StringBuffer();

    for (byte b : bytes) {
      buffer.append(String.format("%02X", b));
    }

    return buffer.toString();
  }

  private static String generateSalt() {
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[20];
    random.nextBytes(bytes);
    String s = new String(bytes);
    return s;
  }
}