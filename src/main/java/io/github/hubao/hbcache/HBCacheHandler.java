package io.github.hubao.hbcache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.redis.ArrayHeaderRedisMessage;
import io.netty.handler.codec.redis.BulkStringHeaderRedisMessage;
import io.netty.handler.codec.redis.DefaultBulkStringRedisContent;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.concurrent.EventExecutorGroup;

/*
 * Desc:
 *
 * @author hubao
 * @see 2024/6/12 20:37
 */
public class HBCacheHandler extends SimpleChannelInboundHandler<String> {

    private static final String CRLF = "\r\n";
    private static final String STR_PREFIX = "+";
    private static final String OK = "OK";
    private static final String INFO = "HBCache server[v1.0.0], created by hubao." + CRLF
                                      +"Mock Redis Server at 2024年6月12日21:28:52" + CRLF;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {

        String[] args = message.split(CRLF);
        System.out.println("HBCacheHandler => message:" + String.join(",", args));

        String cmd = args[2];
        if ("COMMAND".equals(cmd)) {
            ctx.writeAndFlush("*2"
                    + CRLF + "$7"
                    + CRLF + "COMMAND"
                    + CRLF + "$4"
                    + CRLF + "PING"
                    + CRLF
            );
        } else if ("PING".equals(cmd)) {
            String ret = "PONG";
            if (args.length >= 5) {
                ret = args[4];
            }

            simpleString(ctx, ret);
        } else if ("INFO".equals(cmd)) {
            bulkString(ctx, INFO);
        } else {
            simpleString(ctx, OK);
        }

        ctx.writeAndFlush(OK);
    }

    private void simpleString(ChannelHandlerContext ctx, String content) {
        writeByteBuf(ctx, STR_PREFIX + content + CRLF);
    }

    private void bulkString(ChannelHandlerContext ctx, String content) {
        writeByteBuf(ctx, "$" + content.getBytes().length + CRLF + content + CRLF);
    }

    private void writeByteBuf(ChannelHandlerContext ctx, String content) {

        System.out.println("wrap byte buffer and reply:" + content);

        ByteBuf buf = Unpooled.buffer(128);
        buf.writeBytes(content.getBytes());
        ctx.writeAndFlush(buf);
    }
}
