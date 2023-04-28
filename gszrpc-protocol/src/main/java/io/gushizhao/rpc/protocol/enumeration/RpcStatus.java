package io.gushizhao.rpc.protocol.enumeration;

public enum RpcStatus {
    SUCESS(0),
    FAIL(1);

    private final int code;

    RpcStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
