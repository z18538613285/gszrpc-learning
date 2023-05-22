package io.gushizhao.rpc.proxy.api.future;


import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.protocol.response.RpcResponse;
import io.gushizhao.rpc.proxy.api.callback.AsyncRPCCallback;
import io.gushizhao.rpc.proxy.api.common.ClientThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 10:09
 */
public class RPCFuture extends CompletableFuture<Object> {

    public static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);

    private Sync sync;

    private RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private long startTime;

    private long responseTimeThreshold = 5000;

    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcProtocol<RpcRequest> requestRpcProtocol) {
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.responseRpcProtocol != null) {
            return this.responseRpcProtocol.getBody().getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.responseRpcProtocol != null) {
                return this.responseRpcProtocol.getBody().getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception Request id: " +
                    this.requestRpcProtocol.getHeader().getRequestId() +
                                        ". Request class name: " +
                    this.requestRpcProtocol.getBody().getClassName() +
                                        ". Request method: " +
                    this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void done(RpcProtocol<RpcResponse> responseRpcProtocol) {
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);
        // 新增的调用 invokeCallbacks 方法
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " +
                    responseRpcProtocol.getHeader().getRequestId() + ". Response Time = " +
                    responseTime + "ms");
        }
    }

    // 主要用于异步执行回调方法
    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse res = this.responseRpcProtocol.getBody();
        ClientThreadPool.submit(() -> {
            if (!res.isError()) {
                callback.onSuccess(res.getResult());
            } else {
                callback.onException(new RuntimeException("Response error", new Throwable(res.getError())));
            }
        });
    }

    // 主要用于外部服务添加回调接口实例对象到 pendingCallback 集合中
    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    // 主要用于依次执行 pendingCallback 集合中回调接口的方法
    public void invokeCallbacks() {
        lock.lock();
        try {
            for (AsyncRPCCallback rpcCallback : pendingCallbacks) {
                runCallback(rpcCallback);
            }
        } finally {
            lock.unlock();
        }
    }



    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        // future status
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int acquire) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int release) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}
