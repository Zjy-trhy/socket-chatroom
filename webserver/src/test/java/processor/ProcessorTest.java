package processor;

import connector.Request;
import org.junit.Assert;
import org.junit.Test;
import utils.TestUtils;

import javax.servlet.Servlet;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

public class ProcessorTest {

    private static String servletRequest = "GET /TimeServlet HTTP/1.1";

    @Test
    public void given_ServletRequest_thenLoadServlet() throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        Request request = TestUtils.createRequest(servletRequest);
        ServletProcessor processor = new ServletProcessor();
        URLClassLoader servletLoader = processor.getServletLoader();
        Servlet servlet = processor.getServlet(servletLoader, request);
        Assert.assertEquals("TimeServlet", servlet.getClass().getName());
    }
}
