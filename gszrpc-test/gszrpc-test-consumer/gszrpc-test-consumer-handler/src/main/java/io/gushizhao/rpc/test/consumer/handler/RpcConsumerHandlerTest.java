package io.gushizhao.rpc.test.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import io.gushizhao.rpc.consumer.common.RpcConsumer;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/25 17:08
 */
public class RpcConsumerHandlerTest {

    public static void main(String[] args) throws Exception{
        RpcConsumer consumer = RpcConsumer.getInstance();
        consumer.sendRequest(getRpcRequestProtocol());
        Thread.sleep(2000);
        consumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        // 模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        RpcRequest request = new RpcRequest();
        request.setClassName("io.gushizhao.rpc.test.api.DemoService");
        request.setGroup("gushizhao");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"gushizhao"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }
}
