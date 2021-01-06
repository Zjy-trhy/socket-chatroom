import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8888);
        OutputStream out = socket.getOutputStream();
        out.write("GET /index.html Http/1.1".getBytes());
        socket.shutdownOutput();

        InputStream input = socket.getInputStream();
        byte[] buffer = new byte[2048];
        int length = input.read(buffer);
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < length; i++) {
            response.append((char) buffer[i]);
        }
        System.out.println(response.toString());
        socket.shutdownInput();
        socket.close();
    }
}
