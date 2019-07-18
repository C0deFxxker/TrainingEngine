package com.lyl.study.trainning.engine.core.rpc;

import java.util.UUID;

/**
 * Rpc相关工具类
 *
 * @author liyilin
 */
public abstract class RpcUtils {
    public static String createRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
