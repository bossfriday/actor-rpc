package cn.bossfridy.rpc.interfaces;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.transport.Message;

public interface IExecutor {
    /**
     * process
     *
     * @param message
     * @param actorSystem
     */
    void process(Message message, ActorSystem actorSystem);

    /**
     * destroy
     */
    void destroy();
}
