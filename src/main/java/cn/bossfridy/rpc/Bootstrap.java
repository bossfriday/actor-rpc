package cn.bossfridy.rpc;

import cn.bossfridy.rpc.exception.SysException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bootstrap {
    public static void main(String[] args) throws SysException {
        log.info("info");
        throw new SysException("test");
    }
}
