package cn.bossfridy.rpc.test.netty;

import cn.bossfridy.rpc.interfaces.IMsgHandler;
import cn.bossfridy.rpc.interfaces.IServer;
import cn.bossfridy.rpc.transport.RpcMessage;
import cn.bossfridy.rpc.transport.NettyServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class NettyServerTest {
    /**
     * start
     *
     * @throws Exception
     */
    public void start() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        IServer server = new NettyServer(8090, new IMsgHandler() {
            @Override
            public void msgHandle(RpcMessage msg) {
                System.out.println("server received a msg :" + msg.toString());
            }
        });

        server.run(boss, worker);
        System.out.println("server start done.");
        Thread.currentThread().join();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        NettyServerTest test = new NettyServerTest();
        test.start();
    }
}
