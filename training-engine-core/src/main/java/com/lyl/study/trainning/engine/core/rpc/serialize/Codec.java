package com.lyl.study.trainning.engine.core.rpc.serialize;

/**
 * 序列化接口
 *
 * @author liyilin
 */
public interface Codec<IN, OUT> {
    OUT encode(IN in) throws EncodeException;

    IN decode(OUT out) throws DecodeException;
}
