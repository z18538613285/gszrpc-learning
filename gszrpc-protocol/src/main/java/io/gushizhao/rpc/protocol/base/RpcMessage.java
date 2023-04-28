package io.gushizhao.rpc.protocol.base;

import java.io.Serializable;

/**
 * @Author huzhichao
 * @Description 基础消息类
 * @Date 2023/4/23 14:57
 */
public class RpcMessage implements Serializable {

    /**
     * 是否单向发送
     */
    private boolean oneway;

    /**
     * 是否异步调用
     */
    private boolean async;

    public boolean getOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public boolean getAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
