package com.lyl.study.trainning.engine.core.rpc.serialize;

/**
 * 编解码器接口
 *
 * @param <IN>  原文数据类型
 * @param <OUT> 密文数据类型
 * @author liyilin
 */
public interface Codec<IN, OUT> {

    Class<IN> getInClass();

    Class<OUT> getOutClass();

    /**
     * 编码操作
     *
     * @param in 原文对象
     * @return 编码后的密文对象
     * @throws EncodeException 编码异常
     */
    OUT encode(IN in) throws EncodeException;

    /**
     * 解码操作
     *
     * @param out 密文对象
     * @return 解码后的原文对象
     * @throws DecodeException 解码异常
     */
    IN decode(OUT out) throws DecodeException;
}
