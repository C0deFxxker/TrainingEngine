package com.lyl.study.trainning.engine.core.test.mock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author liyilin
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;
}
