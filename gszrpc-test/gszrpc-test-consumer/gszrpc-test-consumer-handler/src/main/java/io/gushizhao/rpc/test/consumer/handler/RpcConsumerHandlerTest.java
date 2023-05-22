package io.gushizhao.rpc.test.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import io.gushizhao.rpc.consumer.common.RpcConsumer;

import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.proxy.api.callback.AsyncRPCCallback;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/25 17:08
 */
public class RpcConsumerHandlerTest {

    private final static Logger logger = LoggerFactory.getLogger(RpcConsumerHandlerTest.class);

    //public static void main(String[] args) throws Exception{
    //    RpcConsumer consumer = RpcConsumer.getInstance();
    //    RPCFuture rpcFuture = consumer.sendRequest(getRpcRequestProtocol());
    //
    //    rpcFuture.addCallback(new AsyncRPCCallback() {
    //        @Override
    //        public void onSuccess(Object result) {
    //            logger.info(("从服务消费者获取到的数据===>>>" + result));
    //        }
    //
    //        @Override
    //        public void onException(Exception e) {
    //            System.out.println(("抛出了异常===>>>" + e));
    //        }
    //    });
    //    Thread.sleep(2000);
    //    consumer.close();
    //}

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        // 模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
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
