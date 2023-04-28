package io.gushizhao.rpc.protocol.response;

import io.gushizhao.rpc.protocol.base.RpcMessage;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:03
 */
public class RpcResponse extends RpcMessage {
    private static final long serialVersionUID = 425335064405584525L;

    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
