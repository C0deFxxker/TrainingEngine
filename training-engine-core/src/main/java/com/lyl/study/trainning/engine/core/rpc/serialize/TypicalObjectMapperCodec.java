package com.lyl.study.trainning.engine.core.rpc.serialize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 带类型标记的ObjectMapper编解码器
 *
 * @param <T> 支持序列化/反序列化的类
 * @author liyilin
 */
public class TypicalObjectMapperCodec<T> implements Codec<T, byte[]> {
    private ObjectMapper objectMapper;
    private Class<T> clazz;

    public TypicalObjectMapperCodec(Class<T> clazz) {
        this.clazz = clazz;
        this.objectMapper = defaultObjectMapper();
    }

    private ObjectMapper defaultObjectMapper() {
        // 默认ObjectMapper
        ObjectMapper om = new ObjectMapper();
        // 日期使用时间戳
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        // 遇到未知属性不报错
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 遇到空集合不报错
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 忽略transient修饰的属性
        om.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        //去掉值为null的字段的序列化，减少带宽，
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 在Json中给出对象详细类型
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return om;
    }

    @Override
    public byte[] encode(T t) throws EncodeException {
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            throw new EncodeException(e.getMessage());
        }
    }

    @Override
    public T decode(byte[] bytes) throws DecodeException {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new DecodeException(e.getMessage());
        }
    }
}
