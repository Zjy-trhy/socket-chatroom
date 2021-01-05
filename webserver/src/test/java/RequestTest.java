import connector.Request;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class RequestTest {

    private static final String validRequest = "Get /index.html HTTP/1.1";

    @Test
    public void givenValidRequest_thenExtrackUri() {
        InputStream input = new ByteArrayInputStream(validRequest.getBytes());
        Request request = new Request(input);
        request.parse();
        Assert.assertEquals("/webroot/index.html", request.getRequestUri());
    }
}
