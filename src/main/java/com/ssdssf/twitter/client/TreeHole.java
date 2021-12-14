package com.ssdssf.twitter.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ssdssf.twitter.client.TwitterClient.postHttpResponse;
import static com.ssdssf.twitter.client.TwitterConstants.getConsumerSecret;
import static com.ssdssf.twitter.client.TwitterConstants.getOauthConsumerKey;
import static com.ssdssf.twitter.client.TwitterConstants.recaptchaSiteKey;
import static com.ssdssf.twitter.client.TwitterConstants.recaptchaSiteSecret;
import static com.ssdssf.twitter.client.TwitterConstants.treeHoleOAuthToken;
import static com.ssdssf.twitter.client.TwitterConstants.treeHoleOAuthTokenSecret;

public class TreeHole extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

  private String html() {
    return String.format("""
<html>
<head>
<script src="https://www.google.com/recaptcha/api.js?render=%s"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script>
  function onClick() {
    grecaptcha.ready(function() {
      grecaptcha.execute('%s', {action: 'submit'}).then(function(token) {
          // Add your logic to submit to your backend server here.
          console.log('token: ' + token);
          $("#token").val(token);
          $("#treeHoleForm").submit();
      });
    });
  }
  </script>
</head>
<table>
<form method="POST" action="/treeHole" id="treeHoleForm" >
<tr><td><textarea name="tweet" rows="9" cols="52"></textarea></td></tr>
<input type="hidden" id="token" name="token"/>
</form>
<tr><td>It will anonymously tweet to <a target="_blank" href="https://twitter.com/alittlehandler">@alittlehandler</a>.</td></tr>
<tr><td><input type="button" value="Tweet" onclick="onClick();"></td></tr>
<table>
</html>
        """, recaptchaSiteKey(), recaptchaSiteKey());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html; charset=utf-8");
    resp.getWriter().println(html());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html; charset=utf-8");

    if (Objects.isNull(req.getParameter("tweet")) || req.getParameter("tweet").isBlank() || req.getParameter("tweet").isEmpty()) {
      resp.sendError(400, "tweet is not provided");
      return;
    }

    if (req.getParameter("tweet").contains("#管理员")) {
      resp.sendError(401, "Operation not allowed");
      return;
    }

    if (Objects.isNull(req.getParameter("token")) || req.getParameter("token").isBlank() || req.getParameter("token").isEmpty()) {
      resp.sendError(400, "token is not provided");
      return;
    }

    final HttpResponse<String> token;
    try {
      token = HttpClient.newHttpClient().send(HttpRequest.newBuilder()
          .uri(URI.create("https://www.google.com/recaptcha/api/siteverify"))
          .header("content-type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString("secret=" + recaptchaSiteSecret() + "&response=" + req.getParameter("token") + "&remoteip=" + req.getRemoteAddr()))
          .build(), HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      resp.sendError(500, "Cannot talk to Google");
      return;
    }
    Gson gson = new Gson();
    final JsonObject json = gson.fromJson(token.body(), JsonObject.class);
    if (!json.get("success").getAsBoolean()) {
      resp.sendError(400, "Google says it's not success");
      return;
    }
    if (json.get("score").getAsDouble() < 0.9) {
      resp.sendError(400, "Google says you are a robot");
      return;
    }
    Map<String, String> params = new HashMap<>();
    params.put("status", req.getParameter("tweet"));
    String statusesURL = "https://api.twitter.com/1.1/statuses/update.json";
    final HttpResponse<String> httpResponse;
    try {
      httpResponse = postHttpResponse(getOauthConsumerKey(), getConsumerSecret(), treeHoleOAuthToken(), treeHoleOAuthTokenSecret(), params, statusesURL);
    } catch (InterruptedException e) {
      LOGGER.error("InterruptedException posting " + statusesURL, e);
      resp.sendError(500, "Error Posting Tweet");
      return;
    }
    if (httpResponse.statusCode() != 200) {
      resp.sendError(httpResponse.statusCode(), httpResponse.body());
      return;
    }

    resp.getWriter().println("<html>You have successfully <a target='_blank' href='https://twitter.com/alittlehandler/status/" + gson.fromJson(httpResponse.body(), JsonObject.class).get("id_str").getAsString() + "'>tweeted</a>!<html>");
  }
}
