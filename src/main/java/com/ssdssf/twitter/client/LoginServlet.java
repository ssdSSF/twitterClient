package com.ssdssf.twitter.client;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.ssdssf.twitter.client.TwitterClient.postHttpResponse;
import static com.ssdssf.twitter.client.TwitterConstants.getApplicationBaseURL;

public class LoginServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String url = "https://api.twitter.com/oauth/request_token";

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("oauth_callback", getApplicationBaseURL() + "/callback");
    final HttpResponse<String> response;
    try {
      response = postHttpResponse(queryParams, url);
    } catch (InterruptedException e) {

      LOGGER.error("InterruptedException when posting to " + url, e);
      resp.sendError(500, "InterruptedException when posting to " + url);
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
