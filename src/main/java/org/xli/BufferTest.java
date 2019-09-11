package org.xli;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.Set;

/**
 * @author 谢力
 * @Description Java NIO Buffer使用
 * @Date 创建于 2019/8/20 15:25
 */
public class BufferTest {
    public static void main(String[] args) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile("D:\\xli\\Work\\Storage\\@Study\\niodemo\\src\\main\\resources\\test.txt", "rw");
        // 获取Channel
        FileChannel fileChannel = randomAccessFile.getChannel();
        // 初始化Buffer最大容量
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);

        System.out.println(byteBuffer);
        System.out.println(byteBuffer.put("NMSL".getBytes()));

//        int readLen = -1;
//        // 将数据读到Buffer
//        while ((readLen = fileChannel.read(byteBuffer)) > 0) {
//            // 将写模式翻转为读模式
//            byteBuffer.flip();
//
//            while (byteBuffer.hasRemaining()) {
//                System.out.println("READ:" + (char) byteBuffer.get());
//            }
//
//            // 清空Buffer
//            byteBuffer.clear();
//        }

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 绑定port
        serverSocketChannel.socket().bind(new InetSocketAddress(35496));
        // 注册ServerSocketChannel到Selector
        Selector selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 阻塞直到有满足条件的Channel后才被唤醒
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();

                // 如果Server接收连接就绪则调用accept()，并将SocketChannel和Selector绑定
                if (selectionKey.isAcceptable()) {
                    SocketChannel client = ((ServerSocketChannel) selectionKey.channel()).accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    // 处理读操作
                } else if (selectionKey.isWritable()) {
                    // 处理写操作
                }
            }
        }
    }
}
