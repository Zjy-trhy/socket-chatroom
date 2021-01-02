import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String LOCAL_HOST = "localhost";
    private static final String QUIT = "quit";
    AsynchronousServerSocketChannel serverChannel;

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
        //绑定监听端口
        //AsynchronousChannelGroup，有类似于线程池的功能，开启一个新线程
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(LOCAL_HOST, DEFAULT_PORT));
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);
            while (true) {
                serverChannel.accept(null, new AcceptHandler());
                System.in.read();//让accept不至于太频繁
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            if (serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }
            AsynchronousSocketChannel clientChannel = result;
            if (clientChannel != null && clientChannel.isOpen()) {
                ClientHandler handler = new ClientHandler(clientChannel);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Map<String, Object> info = new HashMap<>();
                info.put("type", "read");
                info.put("buffer", buffer);
                clientChannel.read(buffer, info, handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }

    private class ClientHandler implements CompletionHandler<Integer, Object> {

        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            Map<String, Object> info = (Map<String, Object>) attachment;
            String type = (String) info.get("type");
            if ("read".equals(type)) {
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                buffer.flip();
                info.put("type", "write");
                clientChannel.write(buffer, info, this);
                buffer.clear();
            } else if ("write".equals(type)) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                info.put("type", "read");
                info.put("buffer", buffer);
                clientChannel.read(buffer, info, this);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
