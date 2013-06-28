package edu.clayton.cas.support.token.util;

import org.apache.commons.cli.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Provides a simple tool for generating an encrypted token and
 * a query parameter string.
 */
public class TokenGenCLI {
  static public void main(String[] args) {
    Options options = TokenGenCLI.buildOptions();
    HelpFormatter helpFormatter = new HelpFormatter();
    CommandLineParser parser = new GnuParser();

    try {
      CommandLine commandLine = parser.parse(options, args);

      String service = commandLine.getOptionValue("service");
      String key = commandLine.getOptionValue("key");
      String firstName = commandLine.getOptionValue("fname");
      String lastName = commandLine.getOptionValue("lname");
      String email = commandLine.getOptionValue("email");
      String username = commandLine.getOptionValue("username");

      JSONObject credentials = new JSONObject();
      credentials.put("firstname", firstName);
      credentials.put("lastname", lastName);
      credentials.put("username", username);
      credentials.put("email", email);

      JSONObject token = new JSONObject();
      token.put("generated", (new Date()).getTime());
      token.put("credentials", credentials);

      String encryptedToken = Crypto.encryptWithKey(token.toString(), key);

      System.out.println("Token:");
      System.out.println(encryptedToken);

      System.out.println("Query parameters:");
      System.out.println(
          String.format(
              "?username=%s&token_service=%s&auth_token=%s",
              username,
              service,
              URLEncoder.encode(encryptedToken, "UTF-8")
          )
      );
    } catch (MissingOptionException e) {
     System.out.println("Missing required option(s)!");
     helpFormatter.printHelp(
          "\nTokenGenCLI",
          "This tool builds a JSON token, encrypts it, and prints the result.",
          options,
          "",
          true
      );
    } catch (UnrecognizedOptionException e) {
      System.out.println("Unrecognized option!");
      helpFormatter.printHelp(
          "\nTokenGenCLI",
          "This tool builds a JSON token, encrypts it, and prints the result.",
          options,
          "",
          true
      );
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
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

    Option apiKeyData = OptionBuilder
        .withArgName("key")
        .hasArg()
        .withDescription("set the encryption key string")
        .isRequired()
        .create("key");
    options.addOption(apiKeyData);

    Option username = OptionBuilder
        .withArgName("username")
        .hasArg()
        .withDescription("set the username")
        .isRequired()
        .create("username");
    options.addOption(username);

    Option firstName = OptionBuilder
        .withArgName("fname")
        .hasArg()
        .withDescription("set the user's first name")
        .isRequired()
        .create("fname");
    options.addOption(firstName);

    Option lastName = OptionBuilder
        .withArgName("lname")
        .hasArg()
        .withDescription("set the user's last name")
        .isRequired()
        .create("lname");
    options.addOption(lastName);

    Option email = OptionBuilder
        .withArgName("email")
        .hasArg()
        .withDescription("set the user's email")
        .isRequired()
        .create("email");
    options.addOption(email);

    return options;
  }
}