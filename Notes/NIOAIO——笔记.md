# NIOAIO——笔记

------

## 值得记录的

### 需要好好学习的基础

IO，装饰器设计模式（IO里面用到了），阻塞，异步的相关概念，多线程。

在这里多线程尤其可以讲解一波，比如到现在第五章，其中的`ChatHandler继承了Runnable`，并且在server中接收到一个新的socket连接请求之后，就会开启一个新的ChatHandler线程。值得注意的是，在线程中共享了同一个HashMap，因为HashMap的不安全性，需要对一些方法加锁。目前位置，这里简单地对相应地方法加上了锁（回顾下不同加锁位置，获取什么锁）

- 并且我觉得这里还有可以优化的地方，例如可以开启一个Cached线程池去新建ChatHandler线程，可以用于函数传参的方式，让各个线程都使用同一个server，而是采用Spring去完成单例的创建（@Resource）。

```java
 public synchronized void addClient(Socket socket) throws IOException
 public synchronized void removeClient(Socket socket) throws IOException
 public synchronized void forwardMsg(Socket socket, String fwdMsg) throws IOException
 public synchronized void close()
```

![image-20201224213810738](NIOAIO——笔记.assets/image-20201224213810738.png)

### 使用cmd命令行执行java程序

直接用IDEA对类进行编译，然后使用IDEA自带的terminal，cd目录到class目录，采用java 类名（不带.class后缀）运行程序

分类有没有包的两种情况：

![image-20201225183526469](NIOAIO——笔记.assets/image-20201225183526469.png)

1. 没有包，直接 java 类名即可
2. 有包，如上所示，则使用 java 包名.类名运行，例如java ChatServer

![image-20201225183335179](NIOAIO——笔记.assets/image-20201225183335179.png)



## 第2章 网络层的解析与协议

![](NIOAIO——笔记.assets/image-20201223154917187.png)

![](NIOAIO——笔记.assets/image-20201223155153930.png)

![](NIOAIO——笔记.assets/image-20201223155657757.png)

![](NIOAIO——笔记.assets/image-20201223160005527.png)

![](NIOAIO——笔记.assets/image-20201223160126732.png)

![](NIOAIO——笔记.assets/image-20201223161416623.png)

![image-20201223163430584](NIOAIO——笔记.assets/image-20201223163430584.png)

## 第3章 解读java.io专业术语

![image-20201223172935999](NIOAIO——笔记.assets/image-20201223172935999.png)

![image-20201223172953887](NIOAIO——笔记.assets/image-20201223172953887.png)

![image-20201223173106207](NIOAIO——笔记.assets/image-20201223173106207.png)

![image-20201223173348591](NIOAIO——笔记.assets/image-20201223173348591.png)

![image-20201223173926519](NIOAIO——笔记.assets/image-20201223173926519.png)

![image-20201223174205639](NIOAIO——笔记.assets/image-20201223174205639.png)

![image-20201223175648554](NIOAIO——笔记.assets/image-20201223175648554.png)

## 第4章 基础搭建

编写了两个类，一个服务端，一个客户端，详见代码：

```java
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
```

```java
public class Client {

    public static void main(String[] args) {
        final String QUIT = "quit";
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        final int DEFAULT_SERVER_PORT = 8888;
        Socket socket = null;
        BufferedWriter writer = null;
        //创建socket
        try {
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            //创建IO流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //等待用户输入信息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();
                writer.write(input + "\n");
                writer.flush();

                //读取服务器返回的信息
                String msg = reader.readLine();
                if (msg != null) {
                    System.out.println(msg);
                }
                if (QUIT.equals(input)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    System.out.println("关闭socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```



## 第5章 BIO编程模型

![image-20201224162008185](NIOAIO——笔记.assets/image-20201224162008185.png)

![image-20201224175359834](NIOAIO——笔记.assets/image-20201224175359834.png)

如BIO编程模型图所示，服务端至少需要N条线程，一条用来接收其他客户端发送的建立连接的请求并创建另外的线程，用来和对应的服务端建立连接，发送消息。

客户端至少需要两条线程，一条阻塞，用来等待用户输入的消息，另外一条用来展示其他客户端发送来的消息。就像QQ聊天窗口那样，上面是消息的展示，下面是输入框。

如我在**值得记录得**中提到得，这个模型可以用线程池来优化，老师肯定也想到了呀

![image-20201225170159695](NIOAIO——笔记.assets/image-20201225170159695.png)

改动如下：

![image-20201225171716229](NIOAIO——笔记.assets/image-20201225171716229.png)

思考，线程池submit和execute的区别？

![image-20201225172322530](NIOAIO——笔记.assets/image-20201225172322530.png)

来源：https://www.cnblogs.com/handsomeye/p/6225033.html

## 第6章 NIO

![image-20201225175818453](NIOAIO——笔记.assets/image-20201225175818453.png)

![image-20201225180851797](NIOAIO——笔记.assets/image-20201225180851797.png)

![image-20201225181149726](NIOAIO——笔记.assets/image-20201225181149726.png)

![image-20201225181430688](NIOAIO——笔记.assets/image-20201225181430688.png)

![image-20201225182050102](NIOAIO——笔记.assets/image-20201225182050102.png)

![image-20201225182216115](NIOAIO——笔记.assets/image-20201225182216115.png)

![image-20201225182521549](NIOAIO——笔记.assets/image-20201225182521549.png)

![image-20201225182724635](NIOAIO——笔记.assets/image-20201225182724635.png)

![image-20201225183000917](NIOAIO——笔记.assets/image-20201225183000917.png)

![image-20201225183114555](NIOAIO——笔记.assets/image-20201225183114555.png)

两种IO和两种NIO的性能对比：数值为耗时（毫秒）

![image-20201226174023321](NIOAIO——笔记.assets/image-20201226174023321.png)

![image-20201226174502875](NIOAIO——笔记.assets/image-20201226174502875.png)

![image-20201226175231261](NIOAIO——笔记.assets/image-20201226175231261.png)

![image-20201226175945691](NIOAIO——笔记.assets/image-20201226175945691.png)

![image-20201226180151270](NIOAIO——笔记.assets/image-20201226180151270.png)

![image-20201226180241003](NIOAIO——笔记.assets/image-20201226180241003.png)





![image-20201227151114447](NIOAIO——笔记.assets/image-20201227151114447.png)

​	

## 第8章 AIO

![image-20201229140749554](NIOAIO——笔记.assets/image-20201229140749554.png)

![image-20201229141038133](NIOAIO——笔记.assets/image-20201229141038133.png)

![image-20201229141553878](NIOAIO——笔记.assets/image-20201229141553878.png)

![image-20201229142341156](NIOAIO——笔记.assets/image-20201229142341156.png)

![image-20201229142524367](NIOAIO——笔记.assets/image-20201229142524367.png)

![image-20201229143030191](NIOAIO——笔记.assets/image-20201229143030191.png)

![image-20201229143445665](NIOAIO——笔记.assets/image-20201229143445665.png)

