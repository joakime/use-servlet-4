package demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetTrailersTest
{
    private Server server;
    private HttpClient client;

    @BeforeEach
    public void setup() throws Exception
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addServlet(DumpServlet.class, "/*");
        server.setHandler(contextHandler);
        server.start();

        client = new HttpClient();
        client.start();
    }

    @AfterEach
    public void teardown()
    {
        LifeCycle.stop(server);
        LifeCycle.stop(client);
    }

    @Test
    public void testTrailers() throws ExecutionException, InterruptedException, TimeoutException
    {
        CountDownLatch doneLatch = new CountDownLatch(1);
        AtomicReference<HttpFields> trailersRef = new AtomicReference<>();
        client.newRequest(server.getURI().resolve("/"))
            .header(HttpHeader.CONNECTION, "close")
            .onResponseSuccess((response) ->
            {
                assertEquals(200, response.getStatus());
                HttpResponse httpResponse = (HttpResponse)response;
                trailersRef.set(httpResponse.getTrailers());
                doneLatch.countDown();
            })
            .idleTimeout(5, TimeUnit.SECONDS)
            .send();
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        HttpFields trailers = trailersRef.get();
        assertEquals("Foo", trailers.get("X-Demo"));
    }
}
