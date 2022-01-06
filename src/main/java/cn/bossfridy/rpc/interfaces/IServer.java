package cn.bossfridy.rpc.interfaces;

import io.netty.channel.EventLoopGroup;

public interface IServer {
    /**
     * run
     *
     * @param bossGroup
     * @param workerGroup
     * @throws Exception
     */
    public void run(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws Exception;

    /**
     * close
     *
     * @throws Exception
     */
    public void close() throws Exception;
}
