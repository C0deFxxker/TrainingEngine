package com.lyl.study.trainning.engine.core.test.serialize;

import com.lyl.study.trainning.engine.core.rpc.serialize.DecodeException;
import com.lyl.study.trainning.engine.core.rpc.serialize.EncodeException;
import com.lyl.study.trainning.engine.core.rpc.serialize.TypicalObjectMapperCodec;
import com.lyl.study.trainning.engine.core.util.Assert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * TypicalObjectMapperCodec单元测试类
 *
 * @author liyilin
 */
@Slf4j
public class TypicalObjectMapperCodecTest {
    @Test
    public void testCodec() throws EncodeException, DecodeException {
        TypicalObjectMapperCodec<Result> codec = new TypicalObjectMapperCodec<>(Result.class);

        Address address = new Address();
        address.setDetail("广东省广州市XXX路100号");
        address.setPostcode("510000");
        Person person = new Person();
        person.setName("Abc");
        person.setAge(20);
        person.setAddress(address);
        Result<Person> result = new Result<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(person);

        log.info("开始序列化result对象: {}", result);
        byte[] bytes = codec.encode(result);
        log.info("序列化后的内容: {}", new String(bytes));

        Result decode = codec.decode(bytes);

        Assert.isTrue(result.equals(decode), "经过转换后的对象与原对象不同");
        decode.setCode(1);
        ((Person) decode.getData()).getAddress().setDetail("xxxxxxxx");
        Assert.isTrue(!result.equals(decode), "Result类的Equal方法实现有误");
    }

    @Test(expected = DecodeException.class)
    public void testDecodeException() throws DecodeException {
        TypicalObjectMapperCodec<Result> codec = new TypicalObjectMapperCodec<>(Result.class);
        codec.decode("abc".getBytes());
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class Result<T> {
        private int code;
        private String msg;
        private T data;
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class Person {
        private String name;
        private int age;
        private Address address;
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class Address {
        private String detail;
        private String postcode;
    }
}
