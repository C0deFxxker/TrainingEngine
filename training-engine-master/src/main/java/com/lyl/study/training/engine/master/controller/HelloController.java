package com.lyl.study.training.engine.master.controller;

import com.lyl.study.training.engine.core.bean.Result;
import com.lyl.study.training.engine.core.proxy.WebClientWorkerProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * TODO 填写注释
 *
 * @author liyilin
 */
@Slf4j
@RestController
public class HelloController {
    @GetMapping("/index")
    public Mono<?> index() {
        WebClientWorkerProxy webClientWorkerProxy = new WebClientWorkerProxy("127.0.0.1", 8080);
        return webClientWorkerProxy.submitTask(10)
                .map(result -> {
                    log.info("收到的信息: {}", result);
                    return result;
                });
    }

    @PostMapping("/post")
    public Mono<?> post(@RequestBody Map<String, Object> params) {
        return Mono.just(new Result<Map<String, Object>>(0, "success", params));
    }
}
