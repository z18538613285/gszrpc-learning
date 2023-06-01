package io.gushizhao.rpc.disuse.api.connection;

import io.netty.channel.Channel;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/25 16:40
 */
public class ConnectionInfo implements Serializable {
    private static final long serialVersionUID = -468686565448453L;

    private Channel channel;
    private long connectionTime;
    private long lastUseTime;

    private AtomicInteger useCount = new AtomicInteger(0);

    public ConnectionInfo() {
    }

    public ConnectionInfo(Channel channel) {
        this.channel = channel;
        long currentTimeStamp = System.currentTimeMillis();
        this.connectionTime = currentTimeStamp;
        this.lastUseTime = currentTimeStamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionInfo info = (ConnectionInfo) o;
        return Objects.equals(channel, info.channel);
    }

    public int getUseCount() {
        return useCount.get();
    }

    public int incrementUseCount() {
        return this.useCount.incrementAndGet();
    }




    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public void setConnectionTime(long connectionTime) {
        this.connectionTime = connectionTime;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(long lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public void setUseCount(AtomicInteger useCount) {
        this.useCount = useCount;
    }
}
