package edu.clayton.cas.support.token.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Crypto {
  /**
   * Returns a {@link Base64} encoded encrypted string.
   *
   * @param string The string to encrypt.
   * @param key The key to use for encryption.
   * @return The encrypted encoded string.
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public static String encryptWithKey(String string, String key)
      throws NoSuchPaddingException,
      NoSuchAlgorithmException,
      InvalidKeyException,
      BadPaddingException,
      IllegalBlockSizeException
  {
    String encryptedString;

    byte[] encryptedStringData;
    SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skey);
    encryptedStringData = cipher.doFinal(string.getBytes());
    encryptedString = Base64.encodeBase64String(encryptedStringData);

    return encryptedString;
  }

  /**
   * Decrypts a {@link Base64} encoded encrypted string.
   *
   * @param string The encoded string to decrypt.
   * @param key The key to use for decryption.
   * @return The decrypted string.
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public static String decryptEncodedStringWithKey(String string, String key)
      throws NoSuchPaddingException,
      NoSuchAlgorithmException,
      InvalidKeyException,
      BadPaddingException,
      IllegalBlockSizeException
  {
    String decryptedString;

    byte[] decryptedStringData;
    SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skey);
    decryptedStringData = cipher.doFinal(Base64.decodeBase64(string));
    decryptedString = new String(decryptedStringData);

    return decryptedString;
  }
}