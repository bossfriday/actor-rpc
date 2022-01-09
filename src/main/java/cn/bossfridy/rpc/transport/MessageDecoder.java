package cn.bossfridy.rpc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * decode
     */
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Message msg = null;
        try {
            msg = Message.decode(in);
        } finally {
            in.skipBytes(in.readableBytes());
            ctx.close();
        }

        return msg;
    }
}
