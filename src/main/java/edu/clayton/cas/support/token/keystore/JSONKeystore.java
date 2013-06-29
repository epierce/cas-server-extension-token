package edu.clayton.cas.support.token.keystore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class JSONKeystore implements Keystore {
  private static final Logger log = LoggerFactory.getLogger(JSONKeystore.class);

  private File storeFile;
  private ConcurrentHashMap<String, Key> keys;

  /**
   * Initialize and empty keystore. Use
   * {@link JSONKeystore#setStoreFile(java.io.File)} and
   * {@link edu.clayton.cas.support.token.keystore.JSONKeystore#saveStoreFile()}
   * to build a new keystore that can be used with
   * {@link JSONKeystore#JSONKeystore(java.io.File)}.
   */
  public JSONKeystore() {
    this(null);
  }

  /**
   * Initialize a {@linkplain JSONKeystore} from a JSON file on disk.
   *
   * @param storeFile The JSON file to load.
   */
  public JSONKeystore(File storeFile) {
    if (storeFile != null && storeFile.exists()) {
      this.storeFile = storeFile;
      this.loadStoreFile();
    } else {
      // Might as well create an empty store.
      this.keys = new ConcurrentHashMap<String, Key>(0);
    }
  }

  /**
   * Retrieve the complete list of keys from the store as an {@link ArrayList}.
   *
   * @return The stored keys.
   */
  public ArrayList<Key> keys() {
    return new ArrayList<Key>(this.keys.values());
  }

  /**
   * Store a new key in the keystore.
   *
   * @param key The {@linkplain Key} to add.
   */
  public void addKey(Key key) {
    this.keys.put(key.name(), key);
  }

  /**
   * Retrieve a named key from the keystore.
   *
   * @param name The name of the {@linkplain Key}.
   * @return
   */
  public Key getKeyNamed(String name) {
    return this.keys.get(name);
  }

  /**
   * Read the instance's associated keystore file into memory.
   */
  public void loadStoreFile() {
    try {
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(this.storeFile)
      );
      JSONTokener jsonTokener = new JSONTokener(new InputStreamReader(bis));
      JSONArray array = new JSONArray(jsonTokener);

      bis.close();

      // Init our keys array with the correct size.
      this.keys = new ConcurrentHashMap<String, Key>(array.length());

      for (int i = 0, j = array.length(); i < j; i += 1) {
        JSONObject obj = array.getJSONObject(i);
        Key key = new Key(obj.getString("name"), obj.getString("data"));
        log.debug("Adding {} key to keystore.", key.name());
        this.addKey(key);
      }
    } catch (FileNotFoundException e) {
      log.error("Could not find JSONKeystore file!");
      log.debug(e.toString());
    } catch (JSONException e) {
      log.error("Error parsing JSON!");
      log.debug(e.toString());
    } catch (IOException e) {
      log.error("Could not close JSONKeystore file!");
      log.debug(e.toString());
    }
  }

  /**
   * Remove a key with a given name from the keystore. You should remember
   * to call {@link edu.clayton.cas.support.token.keystore.JSONKeystore#saveStoreFile()}
   * if you want to update the on-disk store.
   *
   * @param name The key name to remove.
   */
  public void removeKeyNamed(String name) {
    this.keys.remove(name);
  }

  /**
   * Writes the {@linkplain JSONKeystore}, as it is in memory, to the
   * keystore file associated with the instance.
   */
  public void saveStoreFile() {
    JSONArray array = new JSONArray(this.keys.values());

    try {
      if (this.storeFile.exists()) {
        this.storeFile.delete();
        this.storeFile.createNewFile();
      }

      BufferedOutputStream bos = new BufferedOutputStream(
          new FileOutputStream(this.storeFile)
      );
      OutputStreamWriter osw = new OutputStreamWriter(bos);
      osw.write(array.toString());
      osw.close();
    } catch (FileNotFoundException e) {
      log.error("Could not find JSONKeystore file!");
      log.debug(e.toString());
    } catch (IOException e) {
      log.error("Could not create new JSONKeystore file!");
      log.debug(e.toString());
    }
  }

  /**
   * Set the instance's keystore file to the given {@link File}. This will
   * also invoke
   * {@link edu.clayton.cas.support.token.keystore.JSONKeystore#loadStoreFile()}.
   * Thus, you should be sure that the instance is okay to re-initialize.
   *
   * @param file The JSON file.
   */
  public void setStoreFile(File file) {
    this.storeFile = file;
    this.loadStoreFile();
  }
}