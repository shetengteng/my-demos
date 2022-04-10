package com.stt.demo.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class CustomProducerThroughputParamTest {

    // 提高kafka吞吐量测试

    public static void main(String[] args) {
        // 创建kafka生产者配置对象
        Properties properties = new Properties();

        // 给kafka配置对象添加配置信息
        // 配置kafka链接信息
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux101:9092");
        // key,value 序列化配置
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // RecordAccumulator：缓冲区大小，默认 32M：buffer.memory
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // batch.size 批次大小 默认16k
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        // linger.ms：等待时间，默认 0ms
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);

        // compression.type：压缩，默认 none，可配置值 gzip、snappy、lz4 和 zstd
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // 创建生产者对象
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);

        String topic = "first";

        // 调用send方法发送消息
        for (int i = 0; i < 5; i++) {
            String value = "hello " + i;
            kafkaProducer.send(new ProducerRecord<>(topic, value));
        }

        // 关闭资源
        kafkaProducer.close();
    }
}