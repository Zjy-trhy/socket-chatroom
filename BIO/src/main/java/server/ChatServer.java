package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {

    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";
    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;

    public ChatServer() {
        connectedClients = new HashMap<>();
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port, writer);
            System.out.println("客户端 [" + port + "] 连接到服务器");
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
                connectedClients.remove(port);
                System.out.println("客户端 [" + port +"] 断开连接");
            }
        }
    }

    public synchronized void forwardMsg(Socket socket, String fwdMsg) throws IOException {
        for (Integer port : connectedClients.keySet()) {
            if (port.intValue() == socket.getPort()) continue;
            Writer writer = connectedClients.get(port);
            writer.write(fwdMsg);
            writer.flush();
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public synchronized void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("serverSocket关闭了~~~");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void start() {
        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT + "~~~");
            while (true) {
                //等待客户端连接
                Socket accept = serverSocket.accept();
                //创建一个ChatHandler线程
                new Thread(new ChatHandler(this, accept)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
