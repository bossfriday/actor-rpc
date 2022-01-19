package cn.bossfridy.rpc.interfaces;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.transport.RpcMessage;

public interface IExecutor {
    /**
     * process
     *
     * @param message
     * @param actorSystem
     */
    void process(RpcMessage message, ActorSystem actorSystem);

    /**
     * destroy
     */
    void destroy();
}
