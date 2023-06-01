package io.gushizhao.rpc.common.exception;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:31
 */
public class RefuseException extends RuntimeException{
    private static final long serializationUID = -684685135485135L;

    public RefuseException(final Throwable cause) {
        super(cause);
    }

    public RefuseException(final String message) {
        super(message);
    }

    public RefuseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
