package edu.clayton.cas.support.token.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
    
    //Create a random initialization vector
    SecureRandom random = new SecureRandom();
    byte[] randBytes = new byte[16];
    random.nextBytes(randBytes);
    IvParameterSpec iv = new IvParameterSpec(randBytes);

    SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skey, iv);

    byte[] ivBytes = iv.getIV();
    byte[] inputBytes = string.getBytes();
    byte[] plaintext = new byte[ivBytes.length + inputBytes.length];

    System.arraycopy(ivBytes, 0, plaintext, 0, ivBytes.length);
    System.arraycopy(inputBytes, 0, plaintext, ivBytes.length, inputBytes.length);
    
    encryptedStringData = cipher.doFinal(plaintext);
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
    byte[] rawData = Base64.decodeBase64(string);
    byte[] iv = new byte[16];
    byte[] cipherText = new byte[rawData.length - iv.length];

    System.arraycopy(rawData, 0, iv, 0, 16);
    System.arraycopy(rawData, 16, cipherText, 0, cipherText.length);
    
    SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));
    decryptedStringData = cipher.doFinal(cipherText);
    decryptedString = new String(decryptedStringData);

    return decryptedString;
  }
}