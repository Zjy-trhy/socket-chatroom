package client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatClient {

    private static String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private String host;
    private SocketChannel client;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public void start() {
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host, port));

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    handles(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            System.out.println("已下线");
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey selectionKey) throws IOException {
        //连接请求
        SocketChannel client = (SocketChannel) selectionKey.channel();
        if (selectionKey.isConnectable()) {
            if (client.isConnectionPending()) {
                client.finishConnect();
                //处理用户的输入
                new Thread(new UserInputHandler(this)).start();
            }
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            //READ事件
            String msg = receive(client);
            if (msg.isEmpty()) {
                //服务器异常
                close(selector);
            } else {
                System.out.println(msg);
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
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

    boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ChatClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    void send(String msg) throws IOException {
        if (msg.isEmpty()) {
            return;
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
            client.write(wBuffer);
        }

        //检查用户是否发送退出命令
        if (readyToQuit(msg)) {
            close(selector);
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient(DEFAULT_HOST, 7777);
        chatClient.start();
    }
}
