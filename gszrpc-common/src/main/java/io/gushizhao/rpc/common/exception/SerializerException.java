package io.gushizhao.rpc.common.exception;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:31
 */
public class SerializerException extends RuntimeException{
    private static final long serializationUID = -684685135485135L;

    public SerializerException(final Throwable cause) {
        super(cause);
    }

    public SerializerException(final String message) {
        super(message);
    }

    public SerializerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
