package io.gushizhao.rpc.fusing.api;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.spi.annotation.SPI;

@SPI(RpcConstants.DEFAULT_FUSING_INVOKER)
public interface FusingInvoker {


    /**
     * 是否会触发熔断操作，规则如下：
     * 1.断路器默认处于“关闭”状态，当错误个数或错误率到达阈值，就会触发断路器“开启”。
     * 2.断路器开启后进入熔断时间，到达熔断时间终点后重置熔断时间，进入“半开启”状态。
     * 3.在半开启状态下，如果服务能力恢复，则断路器关闭熔断状态。进而进入正常的服务状态。
     * 4.在半开启状态下，如果服务能力未能恢复，则断路器再次触发服务熔断，进入熔断时间。
     * @return 是否要触发熔断，true：触发熔断，false：不触发熔断
     */
    boolean invokeFusingStrategy();

    /**
     * 处理请求的次数
     */
    void incrementCount();


    /**
     * fix08
     * 大流量场景下，如果熔断状态处于半开启状态时，可能会导致大量请求穿透访问后端服务的问题。
     * 如果探测真实服务是否恢复的线程还未返回结果时，又有其他线程来调用服务方法，此时服务状态为半开启状态，就会执行invokeHalfOpenFusingStrategy()方法，
     * 由于探测真实服务是否恢复的线程还未返回结果，所以，满足currentFailureCounter.get()小于或者等于0的条件，此时又会将熔断状态设置为关闭。
     * 后续就会有大量线程穿透熔断逻辑直接访问真实服务。此时，真实服务是否已经恢复仍未可知。
     *
     * 解决：
     *
     */


    /**
     * 访问成功
     */
    void markSuccess();

    /**
     * 访问失败
     */
    void markFailed();

    /**
     * 在milliSeconds毫秒内错误数量或者错误百分比达到totalFailure，则触发熔断操作
     * @param totalFailure 在milliSeconds毫秒内触发熔断操作的上限值
     * @param milliSeconds 毫秒数
     */
    default void init(double totalFailure, int milliSeconds){}
}
