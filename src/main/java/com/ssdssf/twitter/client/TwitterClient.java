package com.ssdssf.twitter.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ssdssf.twitter.client.TwitterConstants.getConsumerSecret;
import static com.ssdssf.twitter.client.TwitterConstants.getOauthConsumerKey;
import static com.ssdssf.twitter.client.TwitterConstants.getOauthToken;
import static com.ssdssf.twitter.client.TwitterConstants.getOauthTokenSecret;

public class TwitterClient {

  private static String getOauthSignature(String key, String signatureBaseString) {
    // Get an hmac_sha1 key from the raw key bytes
    byte[] keyBytes = key.getBytes();
    SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

    // Get an hmac_sha1 Mac instance and initialize with the signing key
    Mac mac;
    try {
      mac = Mac.getInstance("HmacSHA1");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    try {
      mac.init(signingKey);
    } catch (InvalidKeyException e) {
      e.printStackTrace();
      return null;
    }

    // Compute the hmac on input data bytes
    byte[] rawHmac = mac.doFinal(signatureBaseString.getBytes());

    return new String(Base64.getEncoder().encode(rawHmac));
  }

  private static String getSigningKey(String consumerSecret, String oauthTokenSecret) {
    return URLEncoder.encode(consumerSecret, StandardCharsets.UTF_8) + "&" + URLEncoder.encode(oauthTokenSecret, StandardCharsets.UTF_8);
  }

  private static String getSignatureBaseString(String httpMethod, String url, String paramString) {
    return httpMethod.toUpperCase() + "&" + URLEncoder.encode(url, StandardCharsets.UTF_8) + "&" + URLEncoder.encode(paramString, StandardCharsets.UTF_8);
  }

  private static String getParamString(String oauthConsumerKey, String oauthNonce, String oauthTimestamp, String oauthToken, Map<String, String> queryParams) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("oauth_consumer_key", oauthConsumerKey);
    parameters.put("oauth_nonce", oauthNonce);
    parameters.put("oauth_signature_method", "HMAC-SHA1");
    parameters.put("oauth_timestamp", oauthTimestamp);
    parameters.put("oauth_token", oauthToken);
    parameters.put("oauth_version", "1.0");
    parameters.putAll(queryParams);
    return parameters.keySet().stream().sorted().map(e ->
        URLEncoder.encode(e, StandardCharsets.UTF_8) + "="
            + URLEncoder.encode(parameters.get(e), StandardCharsets.UTF_8).replace("+", "%20")).collect(Collectors.joining("&"));
  }

  private static String getOAuthHeader(String oauthConsumerKey, String oauthNonce, String oauthTimestamp, String oauthToken, String oauthSignature) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("oauth_consumer_key", oauthConsumerKey);
    parameters.put("oauth_nonce", oauthNonce);
    parameters.put("oauth_signature", oauthSignature);
    parameters.put("oauth_signature_method", "HMAC-SHA1");
    parameters.put("oauth_timestamp", oauthTimestamp);
    parameters.put("oauth_token", oauthToken);
    parameters.put("oauth_version", "1.0");
    return "OAuth " + parameters.keySet().stream().sorted().map(e ->
        URLEncoder.encode(e, StandardCharsets.UTF_8) + "=\""
            + URLEncoder.encode(parameters.get(e), StandardCharsets.UTF_8) + "\"").collect(Collectors.joining(","));
  }

  public static HttpResponse<String> postHttpResponse(Map<String, String> params, String baseURL) throws IOException, InterruptedException {
    String oauthConsumerKey = getOauthConsumerKey(); //"l6pOJdaJF0gt7BWeVTNHoIKLr";
    String consumerSecret = getConsumerSecret(); //"LWrxMteMHkRLRB3lytwFC1jVDl1qdjN7JZornvWreepmInMQMM";
    String oauthToken = getOauthToken(); // "193722284-ohWgdTTTlJJKzWIxOytJP4KyX1WvKfeTJo0j10tJ";
    String oauthTokenSecret = getOauthTokenSecret(); //"yNc15fZl0dchhtMpNpFgOwAcdsJb1G0OsYcA1KIQaFtVO";
    return postHttpResponse(oauthConsumerKey, consumerSecret, oauthToken, oauthTokenSecret, params, baseURL);
  }

  public static HttpResponse<String> postHttpResponse(String oauthConsumerKey, String consumerSecret, String oauthToken, String oauthTokenSecret, Map<String, String> params, String baseURL) throws IOException, InterruptedException {
    final URI uri;
    if (params.size() == 0) {
      uri = URI.create(baseURL);
    } else {
      uri = URI.create(baseURL + "?" + params.entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&")));
    }
    String oauthNonce = UUID.randomUUID().toString().replace("-", "");
    String oauthTimestamp = String.valueOf(java.time.Instant.now().getEpochSecond());
    return HttpClient.newHttpClient().send(HttpRequest.newBuilder()
        .uri(uri)
        .header("Authorization", getOAuthHeader(oauthConsumerKey, oauthNonce, oauthTimestamp, oauthToken, getOauthSignature(getSigningKey(consumerSecret, oauthTokenSecret),
            getSignatureBaseString("POST", baseURL, getParamString(oauthConsumerKey, oauthNonce, oauthTimestamp, oauthToken, params)))))
        .POST(HttpRequest.BodyPublishers.ofString(""))
        .build(), HttpResponse.BodyHandlers.ofString());
  }

  public static HttpResponse<String> getHttpResponse(String oauthConsumerKey, String consumerSecret, String oauthToken, String oauthTokenSecret, Map<String, String> params, String baseURL) throws IOException, InterruptedException {
    final URI uri;
    if (params.size() == 0) {
      uri = URI.create(baseURL);
    } else {
      uri = URI.create(baseURL + "?" + params.entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&")));
    }
    String oauthNonce = UUID.randomUUID().toString().replace("-", "");
    String oauthTimestamp = String.valueOf(java.time.Instant.now().getEpochSecond());
    return HttpClient.newHttpClient().send(HttpRequest.newBuilder()
        .uri(uri)
        .header("Authorization", getOAuthHeader(oauthConsumerKey, oauthNonce, oauthTimestamp, oauthToken, getOauthSignature(getSigningKey(consumerSecret, oauthTokenSecret),
            getSignatureBaseString("GET", baseURL, getParamString(oauthConsumerKey, oauthNonce, oauthTimestamp, oauthToken, params)))))
        .GET()
        .build(), HttpResponse.BodyHandlers.ofString());
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    String oauthConsumerKey = getOauthConsumerKey(); //"l6pOJdaJF0gt7BWeVTNHoIKLr";
    String consumerSecret = getConsumerSecret(); //"LWrxMteMHkRLRB3lytwFC1jVDl1qdjN7JZornvWreepmInMQMM";
    String oauthToken = getOauthToken(); // "193722284-ohWgdTTTlJJKzWIxOytJP4KyX1WvKfeTJo0j10tJ";
    String oauthTokenSecret = getOauthTokenSecret(); //"yNc15fZl0dchhtMpNpFgOwAcdsJb1G0OsYcA1KIQaFtVO";
    Map<String, String> queryParams = new HashMap<>();
    //queryParams.put("oauth_callback", "http://localhost:8080/twitterCallback");
    queryParams.put("oauth_callback", "oob");
    String url = "https://api.twitter.com/oauth/request_token";
    final HttpResponse<String> response = postHttpResponse(oauthConsumerKey, consumerSecret, oauthToken, oauthTokenSecret, queryParams, url);
    System.out.println(response.body());

    Map<String, String> requestedTokens = new HashMap<>();
    for (String entry:response.body().split("&")) {
      if (entry.startsWith("oauth_token")) {
        requestedTokens.put("oauth_token", entry.split("=")[1]);
      }
      if (entry.startsWith("oauth_token_secret")) {
        requestedTokens.put("oauth_token_secret", entry.split("=")[1]);
      }
    }
    System.out.println(requestedTokens);

    // $ curl --request POST   --url 'https://api.twitter.com/oauth/access_token?oauth_verifier=8835412&oauth_token=y5OSlgAAAAABWWanAAABfYkpXYU'
    // oauth_token=1467369511896690690-6BZl5RVx05QbNK6CHYT0C4lo5XOzP0&oauth_token_secret=WWCXeuqF1gMxwlICFP7PhiomKtExWG88jCcIKVMsMKegf&user_id=1467369511896690690&screen_name=alittlehandler

    oauthToken = "1467369511896690690-6BZl5RVx05QbNK6CHYT0C4lo5XOzP0";
    oauthTokenSecret = "WWCXeuqF1gMxwlICFP7PhiomKtExWG88jCcIKVMsMKegf";
    String verifyCredentialsURL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    System.out.println(new Gson().fromJson(getHttpResponse(oauthConsumerKey, consumerSecret, oauthToken, oauthTokenSecret, new HashMap<>(), verifyCredentialsURL).body(), JsonObject.class).toString());

    Map<String, String> params = new HashMap<>();
    params.put("status", "test1");
    String statusesURL = "https://api.twitter.com/1.1/statuses/update.json";
    final HttpResponse<String> httpResponse = postHttpResponse(oauthConsumerKey, consumerSecret, oauthToken, oauthTokenSecret, params, statusesURL);
    System.out.println(httpResponse.body());
  }
}
