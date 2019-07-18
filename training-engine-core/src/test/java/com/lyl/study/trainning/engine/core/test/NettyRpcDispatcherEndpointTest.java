package com.lyl.study.trainning.engine.core.test;

import com.lyl.study.trainning.engine.core.exception.RpcException;
import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;
import com.lyl.study.trainning.engine.core.rpc.dispatch.DefaultRpcDispatcher;
import com.lyl.study.trainning.engine.core.rpc.dispatch.RpcDispatchRequest;
import com.lyl.study.trainning.engine.core.rpc.dispatch.RpcDispatcher;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcClientEndpoint;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcDispatcherEndpoint;
import com.lyl.study.trainning.engine.core.test.netty.TestNettyRpcClientEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author liyilin
 */
@Slf4j
public class NettyRpcDispatcherEndpointTest {
    @Test
    public void test() throws InterruptedException, ExecutionException {
        RpcDispatcher rpcDispatcher = new DefaultRpcDispatcher();

        Calculator calculator = new Calculator();
        Method[] methods = calculator.getClass().getDeclaredMethods();
        for (Method method : methods) {
            log.info("向RpcDispatcher注册方法: {}", method.toString());
            rpcDispatcher.registerService(method.toString(), calculator, method);
        }

        NettyRpcDispatcherEndpoint nettyRpcDispatcherEndpoint
                = new NettyRpcDispatcherEndpoint("test-server", 8080, rpcDispatcher);

        nettyRpcDispatcherEndpoint.start();
        nettyRpcDispatcherEndpoint.awaitStart();

        NettyRpcClientEndpoint nettyRpcClientEndpoint = new TestNettyRpcClientEndpoint(
                "test-client", new RpcAddress("127.0.0.1", 8080));
        nettyRpcClientEndpoint.start();
        nettyRpcClientEndpoint.awaitStart();

        // 调用add
        RpcEndpointRef selfRef = nettyRpcClientEndpoint.getSelfRef();
        RpcDispatchRequest request = new RpcDispatchRequest();
        request.setServiceName("public int com.lyl.study.trainning.engine.core.test.NettyRpcDispatcherEndpointTest$Calculator.add(int,int)");
        request.setArguments(new Object[]{1, 2});
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setRequestId("1");
        requestMessage.setContent(request);
        log.info("Rpc调用: add(1,2)");
        Future<?> future = selfRef.ask(requestMessage, Integer.class);
        log.info("Rpc调用结果: {}", future.get());

        // 调用sqrt
        selfRef = nettyRpcClientEndpoint.getSelfRef();
        request = new RpcDispatchRequest();
        request.setServiceName("public int com.lyl.study.trainning.engine.core.test.NettyRpcDispatcherEndpointTest$Calculator.sqrt(int)");
        request.setArguments(new Object[]{2});
        requestMessage = new RequestMessage();
        requestMessage.setRequestId("1");
        requestMessage.setContent(request);
        log.info("Rpc调用: sqrt(2)");
        future = selfRef.ask(requestMessage, Integer.class);
        log.info("Rpc调用结果: {}", future.get());

        // 调用result
        selfRef = nettyRpcClientEndpoint.getSelfRef();
        request = new RpcDispatchRequest();
        request.setServiceName("public java.util.Map com.lyl.study.trainning.engine.core.test.NettyRpcDispatcherEndpointTest$Calculator.result()");
//        request.setArguments(new Object[]{2});
        requestMessage = new RequestMessage();
        requestMessage.setRequestId("1");
        requestMessage.setContent(request);
        log.info("Rpc调用: result()");
        future = selfRef.ask(requestMessage, Map.class);
        log.info("Rpc调用结果: {}", future.get());

        // 调用hello
        selfRef = nettyRpcClientEndpoint.getSelfRef();
        request = new RpcDispatchRequest();
        request.setServiceName("public void com.lyl.study.trainning.engine.core.test.NettyRpcDispatcherEndpointTest$Calculator.hello()");
//        request.setArguments(new Object[]{2});
        requestMessage = new RequestMessage();
        requestMessage.setRequestId("1");
        requestMessage.setContent(request);
        log.info("Rpc调用: result()");
        future = selfRef.ask(requestMessage, Void.class);
        log.info("Rpc调用结果: {}", future.get());

        // 调用error
        selfRef = nettyRpcClientEndpoint.getSelfRef();
        request = new RpcDispatchRequest();
        request.setServiceName("public void com.lyl.study.trainning.engine.core.test.NettyRpcDispatcherEndpointTest$Calculator.error() throws java.lang.Exception");
//        request.setArguments(new Object[]{2});
        requestMessage = new RequestMessage();
        requestMessage.setRequestId("1");
        requestMessage.setContent(request);
        log.info("Rpc调用: error()");
        future = selfRef.ask(requestMessage, Void.class);
        try {
            log.info("Rpc调用结果: {}", future.get());
            assert false : "这里必须抛错";
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
            assert e.getCause() instanceof RpcException : "异常类型不正确";
        }

        nettyRpcDispatcherEndpoint.stop();
        while (nettyRpcDispatcherEndpoint.isOpen()) {
            Thread.sleep(200L);
        }
    }

    static class Calculator {
        public int add(int a, int b) {
            return a + b;
        }

        public int sqrt(int a) {
            return a * a;
        }

        public Map<String, Object> result() {
            Map<String, Object> map = new HashMap<>(2);
            map.put("code", 0);
            map.put("msg", "success");
            return map;
        }

        public void hello() {
            log.info("hello");
        }

        public void error() throws Exception {
            throw new Exception("抛了个异常");
        }
    }
}
