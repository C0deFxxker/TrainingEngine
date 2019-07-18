package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Netty实现一请求一响应阻塞的上下文
 *
 * @author liyilin
 */
@Slf4j
class NettyAskContext {
    private static final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, NettyResponse> responseMessages = new ConcurrentHashMap<>();
    private static final long RESPONSE_LOCK_TIMEOUT = 200L;

    public static Future<Object> askForResponse(String requestId, long timeout) {
        Lock lock = new ReentrantLock();
        locks.put(requestId, lock);
        if (log.isTraceEnabled()) {
            log.trace("开始等待请求响应");
        }

        // TODO 目前是线程阻塞模式获取响应信息，需要用Reactor模式优化性能
        FutureTask<Object> future = new FutureTask<>(() -> {
            if (responseMessages.get(requestId) != null) {
                if (log.isTraceEnabled()) {
                    log.trace("请求已得到响应");
                }

                return resolveResult(requestId);
            }

//            lock.wait(timeout);
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() - t < timeout && responseMessages.get(requestId) == null) {
                Thread.sleep(200L);
            }

            if (log.isTraceEnabled()) {
                log.trace("请求已得到响应");
            }

            lock.lock();
            try {
                return resolveResult(requestId);
            } finally {
                lock.unlock();
            }
        });

        new Thread(future).start();
        return future;
    }

    private static Object resolveResult(String requestId) throws Exception {
        if (responseMessages.containsKey(requestId)) {
            NettyResponse nettyResponse = NettyAskContext.responseMessages.get(requestId);
            locks.remove(requestId);
            responseMessages.remove(requestId);

            if (nettyResponse.isSuccess()) {
                return nettyResponse.getContent();
            } else {
                throw new RpcException(nettyResponse.getContent().toString());
            }
        } else {
            locks.remove(requestId);
            responseMessages.remove(requestId);
            throw new TimeoutException("RequestID=" + requestId + "的请求超时");
        }
    }

    public static boolean responseForAsk(NettyResponse nettyResponse) {
        String requestId = nettyResponse.getRequestId();
        Lock lock = locks.get(requestId);
        if (lock != null) {
            boolean flag = false;
            try {
                flag = lock.tryLock(RESPONSE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("获取响应锁异常", e);
            }
            if (flag) {
                try {
                    if (locks.containsKey(requestId)) {
                        responseMessages.put(requestId, nettyResponse);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return false;
    }
}
