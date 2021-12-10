package com.ssdssf.twitter.client;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.ssdssf.twitter.client.TwitterClient.postHttpResponse;

public class LoginServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String url = "https://api.twitter.com/oauth/request_token";

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("oauth_callback", "http://localhost:8080/callback");
    final HttpResponse<String> response;
    try {
      response = postHttpResponse(queryParams, url);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return;
    } catch (InvalidKeyException e) {
      e.printStackTrace();
      return;
    }
    String oauthToken = null;
    for (String entry:response.body().split("&")) {
      if (entry.startsWith("oauth_token=")) {
        oauthToken = entry.split("=")[1];
      }
    }
    resp.getWriter().write("<html><a href='https://api.twitter.com/oauth/authorize?oauth_token=" + oauthToken + "'>Login</a></html>");
  }
}
