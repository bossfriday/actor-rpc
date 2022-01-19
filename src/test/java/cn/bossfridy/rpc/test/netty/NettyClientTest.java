package cn.bossfridy.rpc.test.netty;

import cn.bossfridy.rpc.transport.RpcMessage;
import cn.bossfridy.rpc.transport.NettyClient;
import cn.bossfridy.utils.UUIDUtil;

public class NettyClientTest {
    public static void main(String[] args) {
        NettyClient client = new NettyClient("127.0.0.1", 8090);
        for (int i = 0; i < 10; i++) {
            RpcMessage message = new RpcMessage();
            message.setVersion((byte) 1);
            message.setSession(UUIDUtil.getUUIDBytes());
            message.setSourceHost("127.0.0.1");
            message.setSourcePort(8723);
            message.setSourceMethod("srcMethod");
            message.setTargetHost("127.0.0.1");
            message.setTargetPort(1234);
            message.setTargetMethod("abc");
            message.setPayloadData(new byte[]{(byte) i});

            client.send(message);
        }
    }
}
