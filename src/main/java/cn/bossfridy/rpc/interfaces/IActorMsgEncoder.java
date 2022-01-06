package cn.bossfridy.rpc.interfaces;

public interface IActorMsgEncoder {
    /**
     * encode
     *
     * @param obj
     * @return
     */
    byte[] encode(Object obj);
}
