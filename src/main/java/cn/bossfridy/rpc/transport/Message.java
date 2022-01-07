package cn.bossfridy.rpc.transport;

import cn.bossfridy.rpc.utils.NumberUtil;
import cn.bossfridy.rpc.utils.UUIDUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class Message {
    private int type;

    private byte[] session;

    private String targetMethod;

    private String sourceMethod;

    private String sourceHost;

    private String targetHost;

    private int sourcePort;

    private int targetPort;

    private byte[] data;

    private byte[] extra;   // timestamp

    public Message() {
        this.type = 1;
    }

    /**
     * getSessionShortString
     */
    public String getSessionShortString() {
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
     * encode
     *
     * @param msg
     * @param out
     * @throws Exception
     */
    public static void encode(Message msg, ByteBuf out) throws Exception {

    }

    /**
     * decode
     *
     * @param buf
     * @param msgTotalSize
     * @return
     * @throws Exception
     */
    public static Message decode(ByteBuf buf, int msgTotalSize) throws Exception {
        return null;
    }

    /**
     * buildTimestamp
     *
     * @param message
     */
    public static void buildTimestamp(Message message) {
        if (message != null && message.getExtra() == null) {
            byte[] extra = NumberUtil.long2Bytes(System.currentTimeMillis());
            message.setExtra(extra);
        }
    }
}