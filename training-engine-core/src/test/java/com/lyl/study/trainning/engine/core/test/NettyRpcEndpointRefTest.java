package com.lyl.study.trainning.engine.core.test;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;
import com.lyl.study.trainning.engine.core.test.netty.TestNettyRpcClientEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @author liyilin
 */
@Slf4j
public class NettyRpcEndpointRefTest {
    @Test
    public void test1() throws Exception {
        TestNettyRpcClientEndpoint rpcClientEndpoint = new TestNettyRpcClientEndpoint("test-client", new RpcAddress("127.0.0.1", 8080));
        rpcClientEndpoint.start();

        while (rpcClientEndpoint.isOpen() && !rpcClientEndpoint.isActive()) {
            Thread.sleep(200L);
        }

        RpcEndpointRef rpcEndpointRef = rpcClientEndpoint.getSelfRef();
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setRequestId("1");
        requestMessage.setContent("hello world");
        Future<HashMap> future = rpcEndpointRef.ask(requestMessage, HashMap.class);
        HashMap hashMap = future.get();
        assertEquals(2, hashMap.size());
        assertEquals(hashMap.get("code"), 0);
        assertEquals(hashMap.get("msg"), "success");
        rpcClientEndpoint.stop();

        while (rpcClientEndpoint.isOpen()) {
            Thread.sleep(200L);
        }
    }


}
