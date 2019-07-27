package com.lyl.study.training.engine.worker;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 执行器信息
 *
 * @author liyilin
 */
@Data
@ToString
public class Executor implements Serializable {
    private long taskId;
    private long stepId;
}
