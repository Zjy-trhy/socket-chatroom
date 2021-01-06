package connector;

import processor.StaticProcessor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable {

    private static int DEFAULT_PORT = 8888;
    private ServerSocket server;
    private int port;

    public Connector() {
        this(DEFAULT_PORT);
    }

    public Connector(int port) {
        this.port = port;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            System.out.println("服务器启动，监听端酒：" + port);

            while (true) {
                Socket socket = server.accept();
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();

                Request request = new Request(input);
                request.parse();

                Response response = new Response(output);
                response.setRequest(request);
                StaticProcessor processor = new StaticProcessor();
                processor.process(request, response);

                close(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(server);
        }
    }
}
