package cn.bossfridy.rpc.interfaces;

import cn.bossfridy.rpc.transport.RpcMessage;

public interface IMsgHandler {
    /**
     * msgHandle
     *
     * @param msg
     */
    void msgHandle(RpcMessage msg);
}
