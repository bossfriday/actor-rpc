package cn.bossfridy.rpc.transport;

import cn.bossfridy.utils.UUIDUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class Message {
    /**
     * sessionId（16字节，类比XI）
     **/
    private byte[] session;

    /**
     * 目标方法（最大长度：1字节无符号数字）
     **/
    private String targetMethod;

    /**
     * 源方法（最大长度：1字节无符号数字）
     **/
    private String sourceMethod;

    /**
     * 源IP（4字节）
     **/
    private String sourceHost;

    /**
     * 目标IP（4字节）
     **/
    private String targetHost;

    /**
     * 源端口（4字节）
     **/
    private int sourcePort;

    /**
     * 目标端口（4字节）
     **/
    private int targetPort;

    /**
     * 消息产生时间（8字节）
     **/
    private long timestamp;

    /**
     * 版本（为后续扩展留后手）
     **/
    private byte version;

    /**
     * 消息体（protostuff序列化，最大长度：3字节无符号数字）
     **/
    private byte[] data;

    public Message() {
        this.version = (byte) 1;
    }

    /**
     * getSessionString
     */
    public String getSessionString() {
        if (this.session == null || this.session.length == 0)
            throw new RuntimeException("Message.session is null or empty!");

        return UUIDUtil.getShortString(this.session);
    }

    /**
     * hasSource
     */
    public boolean hasSource() {
        return StringUtils.isNotEmpty(sourceHost) && sourcePort > 0 && !"0.0.0.0".equals(sourceHost);
    }

    /**
     * buildTimestamp
     */
    public void buildTimestamp() {
        if (this.timestamp > 0) {
            return;
        }

        this.timestamp = System.currentTimeMillis();
    }
}
