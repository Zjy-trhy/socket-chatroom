package client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChatClient {

    private static final int DEFAULT_PORT = 8888;
    private static final String LOCAL_HOST = "localhost";
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    /**
     * 客户端的异步通道
     */
    private AsynchronousSocketChannel clientChannel;

    /**
     * 编码方式
     */
    private Charset charset = Charset.forName("UTF-8");

    /**
     * 回收资源
     * @param closeable
     */
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String msg) throws ExecutionException, InterruptedException {
        if (msg.isEmpty()) {
            return;
        }
        ByteBuffer byteBuffer = charset.encode(msg);
        Future<Integer> writeResult = clientChannel.write(byteBuffer);
        writeResult.get();
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public void start() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(LOCAL_HOST, DEFAULT_PORT));
            //阻塞调用，等待客户端连接成功
            future.get();
            System.out.println("已连接到服务器");

            //处理用户输入事件
            new Thread(new UserInputHandler(this)).start();

            //主线程中循环读取服务器转发过来的其他客户端信息
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
            while (true) {
                Future<Integer> readResult = clientChannel.read(buffer);
                Integer result = readResult.get();
                if (result <= 0) {
                    //发生异常，没有读取到数据
                    close(clientChannel);
                    break;
                } else {
                    //正常读取到信息
                    buffer.flip();
                    String msg = String.valueOf(charset.decode(buffer));
                    buffer.clear();
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            close(clientChannel);
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
