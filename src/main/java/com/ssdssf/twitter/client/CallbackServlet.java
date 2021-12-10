package com.ssdssf.twitter.client;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ssdssf.twitter.client.TwitterClient.postHttpResponse;

public class CallbackServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String oauthToken = req.getParameter("oauth_token");
    String oauth_verifier = req.getParameter("oauth_verifier");
    // $ curl --request POST --url 'https://api.twitter.com/oauth/access_token?oauth_verifier=8835412&oauth_token=y5OSlgAAAAABWWanAAABfYkpXYU'
    String url = "https://api.twitter.com/oauth/access_token";
    Map<String, String> params = new HashMap<>();
    params.put("oauth_token", oauthToken);
    params.put("oauth_verifier", oauth_verifier);

    // $ curl --request POST   --url 'https://api.twitter.com/oauth/access_token?oauth_verifier=8835412&oauth_token=y5OSlgAAAAABWWanAAABfYkpXYU'
    final HttpResponse<String> response;
    try {
      response = HttpClient.newHttpClient().send(HttpRequest.newBuilder()
          .uri(URI.create(url + "?" + params.entrySet().stream()
              .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"))))
          .POST(HttpRequest.BodyPublishers.ofString("")).build(), HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      e.printStackTrace();
      return;
    }
    resp.getWriter().println(response.body());
    // oauth_token=1467369511896690690-6BZl5RVx05QbNK6CHYT0C4lo5XOzP0&oauth_token_secret=WWCXeuqF1gMxwlICFP7PhiomKtExWG88jCcIKVMsMKegf&user_id=1467369511896690690&screen_name=alittlehandler

    String oauth_token = null;
    String oauth_token_secret = null;
    String user_id = null;
    String screen_name = null;
    for (String entry:response.body().split("&")) {
      if (entry.startsWith("oauth_token=")) {
        oauth_token = entry.replace("oauth_token=", "");
      }
      if (entry.startsWith("oauth_token_secret=")) {
        oauth_token_secret = entry.replace("oauth_token_secret=", "");
      }
      if (entry.startsWith("user_id=")) {
        user_id = entry.replace("user_id=", "");
      }
      if (entry.startsWith("screen_name=")) {
        screen_name = entry.replace("screen_name=", "");
      }
    }
    try (Connection conn = DriverManager.getConnection(System.getProperty("CONNECTION_STRING"))) {

      String session = UUID.randomUUID().toString();
      final PreparedStatement query = conn.prepareStatement("select 1 from user_tokens where id_str = ?");
      query.setString(1, user_id);
      if (query.executeQuery().isBeforeFirst()) {
        final PreparedStatement update = conn.prepareStatement("update user_tokens set oauth_token = ?, oauth_token_secret = ?, screen_name = ?, session = ? where id_str = ?");
        update.setString(1, oauth_token);
        update.setString(2, oauth_token_secret);
        update.setString(3, screen_name);
        update.setString(4, session);
        update.setString(5, user_id);
      } else {
        final PreparedStatement insert = conn.prepareStatement("insert into user_tokens (ids_str, oauth_token, oauth_token_secret, screen_name, session) values (?, ?, ?, ? ?)");

        insert.setString(1, user_id);
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }
}
