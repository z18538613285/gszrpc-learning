package io.gushizhao.rpc.consumer.spring.context;

import org.springframework.context.ApplicationContext;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/31 15:18
 */
public class RpcConsumerSpringContext {

    /**
     * Spring ApplicationContext
     */
    private ApplicationContext context;

    private RpcConsumerSpringContext(){

    }

    private static class Holder{
        private static final RpcConsumerSpringContext INSTANCE = new RpcConsumerSpringContext();
    }

    public static RpcConsumerSpringContext getInstance(){
        return Holder.INSTANCE;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

}
