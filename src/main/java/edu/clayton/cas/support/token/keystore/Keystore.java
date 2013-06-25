package edu.clayton.cas.support.token.keystore;

import java.util.ArrayList;

/**
 * A key store is a set of {@link Key} objects.
 */
public interface Keystore {
  /**
   * Retrieve all {@link Key}s in the store.
   *
   * @return An {@link ArrayList} of the {@linkplain Key}s.
   */
  public ArrayList<Key> keys();

  /**
   * Put a new {@link Key} in the store.
   *
   * @param key The {@linkplain Key} to add.
   */
  public void addKey(Key key);

  /**
   * Retrieve a {@link Key} from the store that has a specific name.
   *
   * @param name The name of the {@linkplain Key}.
   * @return The {@linkplain Key} or {@code null} if it doesn't exist.
   */
  public Key getKeyNamed(String name);
}