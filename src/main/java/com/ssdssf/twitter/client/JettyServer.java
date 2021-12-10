package com.ssdssf.twitter.client;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

  public void start() throws Exception {
    int port = 80;
    Server server = new Server(port);
    ServletHandler servletHandler = new ServletHandler();
    servletHandler.addServletWithMapping(LoginServlet.class, "/login");
    servletHandler.addServletWithMapping(CallbackServlet.class, "/callback");
    server.setHandler(servletHandler);
    // The HTTP configuration object.
    HttpConfiguration httpConfig = new HttpConfiguration();
// Add the SecureRequestCustomizer because we are using TLS.
    httpConfig.addCustomizer(new SecureRequestCustomizer());

// The ConnectionFactory for HTTP/1.1.
    HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

// Configure the SslContextFactory with the keyStore information.
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    sslContextFactory.setKeyStorePath("server.keystore");
    sslContextFactory.setKeyStorePassword("Welcome1");
    sslContextFactory.setTrustAll(true);
    sslContextFactory.setEndpointIdentificationAlgorithm(null);

// The ConnectionFactory for TLS.
    SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());

// The ServerConnector instance.
    ServerConnector connector = new ServerConnector(server, tls, http11);
    connector.setPort(443);

    server.addConnector(connector);
    server.start();
    LOGGER.info("Server is running at http://localhost:" + port);
    server.join();
  }

  public static void main(String[] args) throws Exception {
    new JettyServer().start();
  }
}
