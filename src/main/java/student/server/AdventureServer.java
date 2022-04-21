package student.server;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.glassfish.jersey.server.ResourceConfig;


public class AdventureServer {
    private static final URI BASE_URI = URI.create("https://0.0.0.0:8080/adventure/v1");
    private static final String KEYSTORE_SERVER_FILE = "src/main/resources/keystore_server";
    private static final String KEYSTORE_SERVER_PWD = "asdfgh";
    private static final String TRUSTORE_SERVER_FILE = "src/main/resources/truststore_server";
    private static final String TRUSTORE_SERVER_PWD = "asdfgh";

    /**
     * Nested class for use of the config method.
     */
    static class CORSResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
                throws IOException {
            MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PATCH, OPTIONS, PUT, HEAD");
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        }
    }

    public static HttpServer createServer(final Class<?> resourceClass) throws IOException {
        final ResourceConfig resourceConfig = new ResourceConfig(resourceClass);
        resourceConfig.register(
                new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.INFO, Verbosity.PAYLOAD_ANY, 10000)
        );
        resourceConfig.register(CORSResponseFilter.class);

        // Grizzly ssl configuration
        SSLContextConfigurator sslContext = new SSLContextConfigurator();

        // set up security context
        sslContext.setKeyStoreFile(KEYSTORE_SERVER_FILE); // contains server keypair
        sslContext.setKeyStorePass(KEYSTORE_SERVER_PWD);
        sslContext.setTrustStoreFile(TRUSTORE_SERVER_FILE); // contains client certificate
        sslContext.setTrustStorePass(TRUSTORE_SERVER_PWD);

        if (!sslContext.validateConfiguration(true)) {
            Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME).info("self-signed SSL not valid.");
        } else {
            Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME).info("self-signed SSL worked!");
        }

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                BASE_URI,
                resourceConfig,
                true /* secure */,
                new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false)
        );

        return server;
    }
}