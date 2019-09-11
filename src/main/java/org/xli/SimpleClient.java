package org.xli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author 谢力
 * @Description
 * @Date 创建于 2019/8/21 14:46
 */
public class SimpleClient {
    private SocketChannel socketChannel;
    private Selector selector;
    private Communication communication;

    public SimpleClient() {
        try {
            socketChannel = SocketChannel.open();
            selector = Selector.open();
            socketChannel.configureBlocking(false);
            SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("localhost", 35496));
            communication = new Communication(ByteBuffer.allocate(16), socketChannel);
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void run() {
        while (true) {
            try {
                System.out.println("select前");
                selector.select();
                System.out.println("select后");
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

    private void eventDispatcher(SelectionKey selectionKey) {
        if (selectionKey.isConnectable()) {
            handleConnectable(selectionKey);
        } else if (selectionKey.isReadable()) {
            handleReadable(selectionKey);
        } else if (selectionKey.isWritable()) {
            handleWritable(selectionKey);
        }
    }

    private void handleConnectable(SelectionKey selectionKey) {
        System.out.println("C端连接就绪！");
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            // 连接就绪后完成连接过程
            socketChannel.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
        System.out.println("C端连接完成！");
    }

    private void handleReadable(SelectionKey selectionKey) {
        System.out.println("读已就绪");
        try {
            String s = communication.readMessage();
            System.out.println("S:" + s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWritable(SelectionKey selectionKey) {
        System.out.println("写已就绪");
        try {
            communication.writeMessage("S, Are You OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SimpleClient();
    }
}
