package cn.bossfridy.rpc.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<Message> {
    public NettyClientHandler(){

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
