package com.lyl.study.trainning.engine.core.test.mock;

import lombok.*;

/**
 * @author liyilin
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RequestWrapper {
    private String requestId;
    private int status;
    private Object content;
}
