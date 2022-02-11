package cn.bossfridy.rpc.actor;

import cn.bossfridy.utils.UUIDUtil;

import static cn.bossfridy.Const.DEAD_LETTER_ACTOR_HOST;
import static cn.bossfridy.Const.DEAD_LETTER_ACTOR_PORT;

public class DeadLetterActorRef extends ActorRef {
    public static final ActorRef Instance = new DeadLetterActorRef();

    public DeadLetterActorRef() {
        super(DEAD_LETTER_ACTOR_HOST, DEAD_LETTER_ACTOR_PORT, UUIDUtil.getUUIDBytes(), (String) null, null);
    }

    @Override
    public void tell(Object message, ActorRef sender) {
        // it is empty
    }
}
