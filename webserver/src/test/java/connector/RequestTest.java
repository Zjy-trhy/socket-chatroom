package connector;

import connector.Request;
import org.junit.Assert;
import org.junit.Test;
import utils.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class RequestTest {

    private static final String validRequest = "Get /index.html HTTP/1.1";

    @Test
    public void givenValidRequest_thenExtrackUri() {
        Request request = TestUtils.createRequest(validRequest);
        Assert.assertEquals("/index.html", request.getRequestUri());
    }
}
