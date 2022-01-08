package cn.bossfridy.rpc.mailbox;

import cn.bossfridy.rpc.transport.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public abstract class MailBox {
    protected final LinkedBlockingQueue<Message> queue;
    protected boolean isStart = true;

    public MailBox(LinkedBlockingQueue<Message> queue) {
        this.queue = queue;
    }

    /**
     * start
     */
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart) {
                    try {
                        Message msg = queue.take();
                        process(msg);
                    } catch (Exception e) {
                        log.error("MailBox.process() error!", e);
                    }
                }
            }
        }).start();
    }

    /**
     * process
     */
    public abstract void process(Message msg) throws Exception;

    /**
     * stop
     */
    public abstract void stop();

    /**
     * put
     */
    public void put(Message msg) {
        try {
            this.queue.put(msg);
        } catch (Exception e) {
            log.error("MailBox.put() error!", e);
        }
    }
}
