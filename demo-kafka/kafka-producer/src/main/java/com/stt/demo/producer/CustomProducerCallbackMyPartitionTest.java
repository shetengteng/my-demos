package com.stt.demo.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Objects;
import java.util.Properties;

public class CustomProducerCallbackMyPartitionTest {

    // 测试kafka的分区策略：使用key的hash为依据的partition

    public static void main(String[] args) throws InterruptedException {
        // 创建kafka生产者配置对象
        Properties properties = new Properties();

        // 给kafka配置对象添加配置信息
        // 配置kafka链接信息
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux101:9092");
        // key,value 序列化配置
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 添加自定义分区器
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());

        // 创建生产者对象
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);

        String topic = "first";

        // 调用send方法发送消息
        for (int i = 0; i < 5; i++) {
            String value = "stt " + i;
            kafkaProducer.send(new ProducerRecord<>(topic, "a", value), new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (Objects.isNull(e)) {
                        System.out.println("topic:" + recordMetadata.topic() + " partition:" + recordMetadata.partition());
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
        // 关闭资源
        kafkaProducer.close();
    }
}