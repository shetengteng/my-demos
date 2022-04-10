package com.stt.demo.producer;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * 1. 实现接口 Partitioner
 * 2. 实现 3 个方法:partition,close,configure
 * 3. 编写 partition 方法,返回分区号
 */
public class MyPartitioner implements Partitioner {

    /**
     * 返回信息对应的分区
     *
     * @param topic      主题
     * @param key        消息的 key
     * @param keyBytes   消息的 key 序列化后的字节数组
     * @param value      消息的 value
     * @param valueBytes 消息的 value 序列化后的字节数组
     * @param cluster    集群元数据可以查看分区信息
     * @return
     */
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        String msgValue = value.toString();
        if (msgValue.contains("stt")) {
            return 0;
        }
        return 1;
    }

    // 关闭资源
    @Override
    public void close() {

    }

    // 配置方法
    @Override
    public void configure(Map<String, ?> configs) {

    }


}
