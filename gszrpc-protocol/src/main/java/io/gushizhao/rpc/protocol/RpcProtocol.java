package io.gushizhao.rpc.protocol;

import io.gushizhao.rpc.protocol.header.RpcHeader;

import java.io.Serializable;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:22
 */
public class RpcProtocol<T> implements Serializable {
    private static final long serialVersionUID = 292789485166173277L;

    /**
     * 消息头
     */
    private RpcHeader header;
    /**
     * 消息体
     */
    private T body;

    public RpcHeader getHeader() {
        return header;
    }

    public void setHeader(RpcHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
