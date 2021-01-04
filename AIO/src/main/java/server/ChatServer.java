package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String LOCAL_HOST = "localhost";
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;
    private static final int THREADPOOL_SIZE = 8;
    private List<ClientHandler> connectClients;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer(int port) {
        this.port = port;
        this.connectClients = new ArrayList<>();
    }

    public ChatServer() {
        this(DEFAULT_PORT);
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

    private boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    private synchronized void addClient(ClientHandler handler) {
        connectClients.add(handler);
        System.out.println(getClientName(handler.clientChannel) + "已连接到服务器");
    }

    private String getClientName(AsynchronousSocketChannel clientChannel) {
        int clientPort =-1;
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
            clientPort = remoteAddress.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "客户端[" + clientPort + "]：";
    }

    private synchronized void forwardMsg(AsynchronousSocketChannel clientChannel, String fwdMsg) {
        for (ClientHandler handler : connectClients) {
            //不转发到发送消息的那个客户端
            if (handler.clientChannel.equals(clientChannel)) continue;
            //将要发送的消息写入到缓冲区
            ByteBuffer buffer = charset.encode(getClientName(handler.clientChannel) + "：" + fwdMsg);
            //将相应的信息写入到用户通道中，用户再通过获取通道中的信息读取到对应的转发内容
            handler.clientChannel.write(buffer, null, handler);
        }
    }

    /**
     * 对客户端发送的消息进行解码
     * @param buffer
     * @return
     */
    private synchronized String receive(ByteBuffer buffer) {
        return String.valueOf(charset.decode(buffer));
    }

    private synchronized void removeClient(ClientHandler clientHandler) {
        connectClients.remove(clientHandler);
        System.out.println(getClientName(clientHandler.clientChannel) + "已断开连接");
        //关闭改客户对应的流
        close(clientHandler.clientChannel);
    }

    public void start() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverChannel.bind(new InetSocketAddress(LOCAL_HOST, port));
            System.out.println("启动服务器，监听端口：" + port);
            while (true) {
                serverChannel.accept(null, new AcceptHandler());
                System.in.read();//为了避免浪费系统资源，过于频繁地调用accept()函数
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            if (serverChannel.isOpen()) {//只要服务器还在运行，就要继续等待新的连接请求
                serverChannel.accept(null, this);
            }
            if (clientChannel != null && clientChannel.isOpen()) {
                ClientHandler handler = new ClientHandler(clientChannel);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
                clientChannel.read(buffer, buffer, handler);
                //第一个buffer，将从clientChannel中读到的数据放入buffer中，
                //第二个buffer，作为attachment传入handler，使之可以使用buffer中的数据
                //将新用户添加到在线用户列表中去，实际添加进去的是对应的Handler
                addClient(handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败：" + exc);
        }
    }

    private class ClientHandler implements CompletionHandler<Integer, Object> {

        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            if (buffer != null) {
                //刚刚完成的是read操作
                if (result <= 0) {
                    //客户端异常
                    //将客户移出在线客户列表
                } else {
                    buffer.flip();
                    String fwdMsg = receive(buffer);
                    System.out.println(getClientName(clientChannel) + "：" + fwdMsg);
                    forwardMsg(clientChannel, fwdMsg);
                    buffer.clear();

                    //检测发送的是不是退出请求
                    if (readyToQuit(fwdMsg)) {
                        removeClient(this);
                    } else {
                        //继续监听用户发送消息
                        clientChannel.read(buffer, buffer, this);
                    }
                }
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("读写失败：" + exc);
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
