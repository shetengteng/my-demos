package com.stt.demo.consumer.base.rebalance.range;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class CustomProducer {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 配置kafka链接信息
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux101:9092");
        // key,value 序列化配置
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 创建生产者对象
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);
        String topic = "first";
        // 调用send方法发送消息
        for (int i = 0; i < 500; i++) {
            kafkaProducer.send(new ProducerRecord<>(topic, i % 7, "", "hello " + i), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception == null) {
                        System.out.println("topic: " + metadata.topic() + " partition: " + metadata.partition());
                    }
                }
            });
        }
        // 关闭资源
        kafkaProducer.close();
    }
}
