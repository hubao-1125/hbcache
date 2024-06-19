package io.github.hubao.hbcache.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Desc:
 *
 * @author hubao
 * @see 2024/6/12 20:46
 */
public class HBCacheDecoder extends ByteToMessageDecoder {


    AtomicLong counter = new AtomicLong();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        System.out.println("HBCacheDecoder count:" + counter.incrementAndGet());

        if (in.readableBytes() <= 0) {
            return;
        }

        int count = in.readableBytes();
        int index = in.readerIndex();
        System.out.println("count:" + count + ",index:" + index);

        byte[] bytes = new byte[count];
        in.readBytes(bytes);
        String ret = new String(bytes);
        System.out.println("ret:" + ret);

        out.add(ret);
    }
}
