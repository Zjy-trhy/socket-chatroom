import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        final String QUIT = "quit";
        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听：" + DEFAULT_PORT + " 端口");

            while (true) {
                //等待客户端连接，阻塞式
                Socket accept = serverSocket.accept();
                System.out.println("客户端 [" + accept.getPort() + "]，已经连接");
                BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accept.getOutputStream()));

                String msg = null;
                //读取客户端的消息
                while ((msg = reader.readLine()) != null) {

                    System.out.println("客户端 [" + accept.getPort() + "]：" + msg);

                    //回复客户，复读机
                    writer.write("服务器：" + msg + "\n");
                    writer.flush();
                    if (QUIT.equals(msg)) {
                        System.out.println("客户端 [" + accept.getPort() + "]，断开连接");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("关闭serverSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
