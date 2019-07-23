package com.lyl.study.training.engine.core.proxy;

import com.lyl.study.training.engine.core.bean.Result;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker终端点
 *
 * @author liyilin
 */
public class WebClientWorkerProxy implements WorkerProxy {
    private final WebClient webClient;

    public WebClientWorkerProxy(String host, int port) {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .baseUrl("http://" + host + ":" + port)
                .build();
    }

    @Override
    public Mono<Result<?>> submitTask(long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        return webClient.post().uri("/post")
                .body(Mono.just(params), Map.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<?>>() {
                });
    }
}
