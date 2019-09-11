package org.xli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author 谢力
 * @Description
 * @Date 创建于 2019/8/21 15:05
 */
public class SimpleServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public SimpleServer() {
    }

    //初始化Server -> 将ServerChannel注册到Selector
    public void startup() {
        try {
            if (selector == null) {
                selector = Selector.open();
            }

            if (serverSocketChannel == null) {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(35496));
                serverSocketChannel.configureBlocking(false);
                // 注册Accept事件
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("服务端启动成功……");
                // 运行，处理C端请求
                run();
            }
        } catch (IOException e) {
            System.out.println("服务端初始化失败！");
            e.printStackTrace();
        }
    }

    private void run() {
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    eventDispatcher(selectionKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 事件分发器
    private void eventDispatcher(SelectionKey selectionKey) {
        if (selectionKey.isAcceptable()) {
            handleAcceptable(selectionKey);
        } else if (selectionKey.isReadable()) {
            handleReadable(selectionKey);
        } else if (selectionKey.isWritable()) {
            handleWritable(selectionKey, "Server:OK!");
        }
    }

    // 事件处理器
    private void handleAcceptable(SelectionKey selectionKey) {
        System.out.println("出现新客户端连接！");
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel client = serverSocketChannel.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE)
                    .attach(new Communication(ByteBuffer.allocate(16), client));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReadable(SelectionKey selectionKey) {
        System.out.println("Channel读已就绪");
        Communication communication = (Communication) selectionKey.attachment();
        try {
            String s = communication.readMessage();
            System.out.println("收到消息[" + s + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWritable(SelectionKey selectionKey, String msg) {
        System.out.println("Channel写已就绪");
        Communication communication = (Communication) selectionKey.attachment();
        try {
            communication.writeMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SimpleServer().startup();
    }
}
