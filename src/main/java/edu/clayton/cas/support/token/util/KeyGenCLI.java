package edu.clayton.cas.support.token.util;

import org.apache.commons.cli.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides a simple tool for generating AES-128 key strings and
 * JSON objects for a
 * {@link edu.clayton.cas.support.token.keystore.JSONKeystore}.
 */
public class KeyGenCLI {
  static public void main(String[] args) {
    Options options = KeyGenCLI.buildOptions();
    HelpFormatter helpFormatter = new HelpFormatter();
    CommandLineParser parser = new GnuParser();

    try {
      CommandLine commandLine = parser.parse(options, args);
      String service = commandLine.getOptionValue("service");
      String key = Crypto.generateAes256KeyWithSeed(service);

      if (key == null) {
        throw new Exception("Key was not generated!");
      }

      JSONObject keyObject = new JSONObject();
      keyObject.put("name", service);
      keyObject.put("data", key);

      System.out.println(String.format("Key: `%s`\n", key));

      System.out.println("Key object:");
      System.out.println(keyObject.toString());
    } catch (MissingOptionException e) {
     System.out.println("Missing required option(s)!");
     helpFormatter.printHelp(
          "\nKeyGenCLI",
          "This tool generates a unique key and prints the result.",
          options,
          "",
          true
      );
    } catch (UnrecognizedOptionException e) {
      System.out.println("Unrecognized option!");
      helpFormatter.printHelp(
          "\nKeyGenCLI",
          "This tool generates a unique key and prints the result.",
          options,
          "",
          true
      );
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Options buildOptions() {
    Options options = new Options();

    Option tokenService = OptionBuilder
        .withArgName("service")
        .hasArg()
        .withDescription("set the name of calling service")
        .isRequired()
        .create("service");
    options.addOption(tokenService);

    return options;
  }
}