package io.github.hubao.hbcache.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/*
 * Desc:
 *
 * @author hubao
 * @see 2024/6/12 20:37
 */
public class HBCacheHandler extends SimpleChannelInboundHandler<String> {

    private static final String CRLF = "\r\n";
    private static final String STR_PREFIX = "+";
    private static final String BULK_PREFIX = "";
    private static final String OK = "OK";
    private static final String INFO = "HBCache server[v1.0.0], created by hubao." + CRLF
                                      +"Mock Redis Server at 2024年6月12日21:28:52" + CRLF;

    public static final HBCache CACHE = new HBCache();


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
        } else if ("SET".equals(cmd)) {
            CACHE.set(args[4], args[6]);
            simpleString(ctx, OK);
        } else if ("GET".equals(cmd)) {
            String value = CACHE.get(args[4]);
            bulkString(ctx, value);
        } else if ("STRLEN".equals(cmd)) {
            String value = CACHE.get(args[4]);
            integer(ctx, value == null?0:value.length());
        } else if ("DEL".equals(cmd)) {
            int len = (args.length - 3) / 2;
            String keys[] = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 2];
            }
            int del = CACHE.del(keys);
            integer(ctx, del);
        } else if ("EXISTS".equals(cmd)) {
            int len = (args.length - 3) / 2;
            String keys[] = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 2];
            }
            integer(ctx, CACHE.exists(keys));
        } else if ("MGET".equals(cmd)) {
            int len = (args.length - 3) / 2;
            String keys[] = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 2];
            }
            array(ctx, CACHE.mget(keys));
        } else if ("MSET".equals(cmd)) {

            int len = (args.length - 3) / 4;
            String keys[] = new String[len];
            String vals[] = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 4];
                vals[i] = args[6 + i * 4];
            }
            CACHE.mset(keys, vals);
            simpleString(ctx, OK);
        } else if ("INCR".equals(cmd)) {

            String key = args[4];
            try {
                integer(ctx, CACHE.incr(key));
            } catch (NumberFormatException e) {
                error(ctx, "NFE" + key + "value[" + CACHE.get(key) + "] is not integer.");
            }
        } else if ("DECR".equals(cmd)) {
            String key = args[4];
            try {
                integer(ctx, CACHE.decr(key));
            } catch (NumberFormatException e) {
                error(ctx, "NFE" + key + "value[" + CACHE.get(key) + "] is not integer.");
            }
        } else {
            simpleString(ctx, OK);
        }

        ctx.writeAndFlush(OK);
    }

    private void error(ChannelHandlerContext ctx, String msg) {
        writeByteBuf(ctx, errorEncode(msg));
    }

    private String errorEncode(String msg) {
        return "-" + msg + CRLF;
    }

    private void array(ChannelHandlerContext ctx, String[] array) {

        writeByteBuf(ctx, arrayEncode(array));
    }

    private static String arrayEncode(Object[] array) {

        StringBuilder sb = new StringBuilder();

        if (array == null) {
            sb.append("*-1" + CRLF);
        } else if (array.length == 0) {
            sb.append("*0" + CRLF);
        } else {
            sb.append("*" + array.length + CRLF);
            for (int i = 0; i < array.length; i++) {

                Object obj = array[i];
                if (obj == null) {
                    sb.append("$-1" + CRLF);
                } else {
                    if (obj instanceof Integer) {
                        sb.append(integerEncode((Integer) obj));
                    } else if (obj instanceof String) {
                        sb.append(bulkStringEncode((String) obj));
                    } else if (obj instanceof Object[] objs) {
                        sb.append(arrayEncode(objs));
                    }
                }
            }
        }
        return sb.toString();
    }

    private void integer(ChannelHandlerContext ctx, int i) {

        writeByteBuf(ctx, integerEncode(i));
    }

    private static String integerEncode(int i) {
        return ":" + i + CRLF;
    }

    private void simpleString(ChannelHandlerContext ctx, String content) {

        writeByteBuf(ctx, simpleStringEncode(content));
    }

    private void bulkString(ChannelHandlerContext ctx, String content) {
        writeByteBuf(ctx, bulkStringEncode(content));
    }

    private static String bulkStringEncode(String content) {
        String ret = null;
        if (content == null) {
            ret = "$-1";
        } else if (content.isEmpty()) {
            ret = "$0";
        } else {
            ret = BULK_PREFIX + content + content.getBytes().length + CRLF + content + CRLF;
        }
        return ret + CRLF;
    }

    private static String simpleStringEncode(String content) {
        String ret = null;
        if (content == null) {
            ret = "$-1";
        } else if (content.isEmpty()) {
            ret = "$0";
        } else {
            ret = STR_PREFIX + content;
        }
        return ret + CRLF;
    }

    private void writeByteBuf(ChannelHandlerContext ctx, String content) {

        System.out.println("wrap byte buffer and reply:" + content);

        ByteBuf buf = Unpooled.buffer(128);
        buf.writeBytes(content.getBytes());
        ctx.writeAndFlush(buf);
    }
}
