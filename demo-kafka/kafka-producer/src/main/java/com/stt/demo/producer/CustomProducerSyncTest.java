package com.stt.demo.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class CustomProducerSyncTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 创建kafka生产者配置对象
        Properties properties = new Properties();

        // 给kafka配置对象添加配置信息
        // 配置kafka链接信息
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux101:9092");
        // key,value 序列化配置
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 创建生产者对象
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);

        String topic = "first";

        // 调用send方法发送消息
        for (int i = 0; i < 5; i++) {
            String value = "hello " + i;
            RecordMetadata recordMetadata = kafkaProducer.send(new ProducerRecord<>(topic, value)).get();
            System.out.println("topic:" + recordMetadata.topic() + " partition:" + recordMetadata.partition());
        }

        // 关闭资源
        kafkaProducer.close();
    }
}
