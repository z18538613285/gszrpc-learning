package io.gushizhao.rpc.common.exception;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:31
 */
public class RegistryException extends RuntimeException{
    private static final long serializationUID = -684685135485135L;

    public RegistryException(final Throwable cause) {
        super(cause);
    }

    public RegistryException(final String message) {
        super(message);
    }

    public RegistryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
