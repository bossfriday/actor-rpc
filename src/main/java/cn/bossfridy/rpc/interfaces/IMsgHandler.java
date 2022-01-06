package cn.bossfridy.rpc.interfaces;

import cn.bossfridy.rpc.transport.Message;

public interface IMsgHandler {
    /**
     * msgHandle
     *
     * @param msg
     */
    public void msgHandle(Message msg);
}
