package com.stt.demo.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class CustomProducerTransactionTest {
    public static void main(String[] args) {
        // 创建kafka生产者配置对象
        Properties properties = new Properties();

        // 给kafka配置对象添加配置信息
        // 配置kafka链接信息
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux101:9092");
        // key,value 序列化配置
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 设置事务 id（必须），事务 id 任意起名，每个producer一个固定的 transactionId,相同的transactionId的producer只有一个生效
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my_transaction_id_0");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // ENABLE_IDEMPOTENCE_CONFIG 为true，则 acks 默认 all
        properties.put(ProducerConfig.ACKS_CONFIG,"all");

        // 创建生产者对象
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);

        // 初始化事务
        kafkaProducer.initTransactions();
        // 开启事务
        kafkaProducer.beginTransaction();

        try {
            String topic = "first";
            // 调用send方法发送消息
            for (int i = 0; i < 5; i++) {
                String value = "transaction";
                kafkaProducer.send(new ProducerRecord<>(topic,"transaction", value));
            }
//            int i = 1/0;
            Thread.sleep(10*1000);
            // 提交事务
            kafkaProducer.commitTransaction();
        } catch (Exception e) {
            kafkaProducer.abortTransaction();
        } finally {
            // 关闭资源
            kafkaProducer.close();
        }
    }
}
