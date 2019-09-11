package org.xli;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * @author 谢力
 * @Description C-S I/O操作
 * @Date 创建于 2019/8/21 21:23
 */
public class Communication {
    private SocketChannel socketChannel;
    private ByteBuffer buffer;

    public Communication(ByteBuffer buffer, SocketChannel socketChannel) {
        this.buffer = buffer;
        this.socketChannel = socketChannel;
    }

    //Channel -> Buffer -> String
    public String readMessage() throws IOException {
        StringBuilder sb = new StringBuilder();
        int readLen = 0;

        buffer.clear();
        while ((readLen = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes, 0, buffer.remaining());
            sb.append(new String(bytes));
            buffer.clear();
        }

        return sb.toString();
    }

    //String -> Buffer -> Channel
    public void writeMessage(String msg) throws IOException {
        byte[] bytes = msg.getBytes(); // 10
        int offset = 0;

        while (offset < bytes.length) {
            System.out.println("off:" + offset + ", pos:" + buffer.position());
            buffer.clear();
            buffer.put(bytes, offset, bytes.length - offset > buffer.capacity() ? buffer.capacity() : bytes.length - offset);
            System.out.println("Limit:" + buffer.limit());
            offset += buffer.position();
            System.out.println(new String(buffer.array()));

            buffer.flip();
            int i = socketChannel.write(buffer);
            System.out.println("写了:" + i);
        }
    }
}
