package edu.clayton.cas.support.token.keystore;

import org.springframework.util.Assert;

/**
 * Defines a key to be used in encrypting/decrypting data. Keys can be
 * identified by a name.
 */
public class Key {
  private String name;
  private byte[] data;

  /**
   * Create a {@linkplain Key} using a {@link String} for both the name
   * and the data.
   *
   * @param name The key name and data.
   */
  public Key(String name) {
    this(name, name.getBytes());
  }

  /**
   * Create a {@linkplain Key} with a given name and key data using strings.
   *
   * @param name The name of the key.
   * @param data A string to use for key data.
   */
  public Key(String name, String data) {
    this(name, (data != null) ? data.getBytes() : null);
  }

  /**
   * Create a {@linkplain Key} with a given name and key data.
   * @param name The name of the key.
   * @param data A byte array to use as the key data.
   */
  public Key(String name, byte[] data) {
    Assert.notNull(name, "key name cannot be null");
    Assert.notNull(data, "key data cannot be null");
    this.name = name;
    this.data = data;
  }

  /** Get the key's data. */
  public byte[] data() {
    return this.data;
  }

  /** Get the key's name. */
  public String name() {
    return this.name;
  }
}