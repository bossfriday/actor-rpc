package cn.bossfridy.rpc.mailbox;

import cn.bossfridy.rpc.transport.RpcMessage;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfridy.Const.CPU_PROCESSORS;

@Slf4j
public abstract class MailBox {
    protected Disruptor<RpcMessageEvent> queue;
    protected RingBuffer<RpcMessageEvent> ringBuffer;
    protected boolean isStart = true;

    public MailBox(int capacity) {
        this.queue = getMainBoxQueue(capacity);
        queue.handleEventsWith(new RpcMessageEventHandler());
    }

    /**
     * start
     */
    public void start() throws Exception {
        ringBuffer = queue.start();

        if (ringBuffer == null)
            throw new Exception("MailBox.start() error!");
    }

    /**
     * process
     */
    public abstract void process(RpcMessage msg) throws Exception;

    /**
     * stop
     */
    public abstract void stop();

    /**
     * put
     */
    public void put(RpcMessage msg) {
        try {
            EventTranslatorOneArg<RpcMessageEvent, RpcMessage> translator = new RpcMessageEventTranslator();
            ringBuffer.publishEvent(translator, msg);
        } catch (Exception e) {
            log.error("MailBox.put() error!", e);
        }
    }

    public class RpcMessageEvent {
        private RpcMessage msg;

        public RpcMessage getMsg() {
            return msg;
        }

        public void setMsg(RpcMessage msg) {
            this.msg = msg;
        }
    }

    public class MessageEventFactory implements EventFactory<RpcMessageEvent> {
        @Override
        public RpcMessageEvent newInstance() {
            return new RpcMessageEvent();
        }
    }

    public class RpcMessageEventTranslator implements EventTranslatorOneArg<RpcMessageEvent, RpcMessage> {
        @Override
        public void translateTo(RpcMessageEvent rpcMessageEvent, long l, RpcMessage rpcMessage) {
            rpcMessageEvent.setMsg(rpcMessage);
        }
    }

    public class RpcMessageEventHandler implements EventHandler<RpcMessageEvent> {
        @Override
        public void onEvent(RpcMessageEvent messageEvent, long l, boolean b) throws Exception {
            process(messageEvent.getMsg());
        }
    }

    /**
     * getMainBoxQueue
     *
     * @param capacity
     * @return
     */
    private Disruptor<MailBox.RpcMessageEvent> getMainBoxQueue(int capacity) {
        Disruptor<RpcMessageEvent> disruptor = new Disruptor<RpcMessageEvent>(
                new MessageEventFactory(),
                getRingBufferSize(getRingBufferSize(capacity)),
                Executors.newFixedThreadPool(CPU_PROCESSORS),
                ProducerType.MULTI, // ProducerType.SINGLE  ??????????????? ProducerType.MULTI   ????????????
                // BlockingWaitStrategy??????????????????????????????CPU??????????????????
                // SleepingWaitStrategy??????BlockingWaitStrategy????????????????????????????????????????????????
                // YieldingWaitStrategy ????????????????????????????????????????????? CPU ???????????????
                new YieldingWaitStrategy());

        return disruptor;
    }

    /**
     * ??????ringBufferSize?????????2?????????
     */
    private static int getRingBufferSize(int num) {
        int s = 2;
        while (s < num) {
            s <<= 1;
        }

        return s;
    }
}
