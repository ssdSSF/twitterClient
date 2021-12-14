package com.ssdssf.twitter.client;

public class TwitterConstants {

  public static String getOauthConsumerKey() {
    return System.getProperty("oauthConsumerKey");
  }

  public static String getConsumerSecret() {
    return System.getProperty("consumerSecret");
  }

  public static String getOauthToken() {
    return System.getProperty("oauthToken");
  }

  public static String getOauthTokenSecret() {
    return System.getProperty("oauthTokenSecret");
  }

  public static String getApplicationBaseURL() {
    return System.getProperty("applicationBaseURL");
  }

  public static String getKeyStorePassword() {
    return System.getProperty("keyStorePassword");
  }

  public static String treeHoleOAuthToken() {
    return System.getProperty("treeHoleOAuthToken");
  }

  public static String treeHoleOAuthTokenSecret() {
    return System.getProperty("treeHoleOAuthTokenSecret");
  }

  public static String recaptchaSiteKey() {
    return System.getProperty("recaptchaSiteKey");
  }

  public static String recaptchaSiteSecret() {
    return System.getProperty("recaptchaSiteSecret");
  }
}
