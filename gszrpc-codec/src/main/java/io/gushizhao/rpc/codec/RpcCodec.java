package io.gushizhao.rpc.codec;

import io.gushizhao.rpc.serialization.api.Serialization;
import io.gushizhao.rpc.serialization.jdk.JdkSerialization;

public interface RpcCodec {

    default Serialization getJdkSerialization() {
        return new JdkSerialization();
    }
}
