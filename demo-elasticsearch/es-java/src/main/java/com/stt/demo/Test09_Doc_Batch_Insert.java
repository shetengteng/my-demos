package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

public class Test09_Doc_Batch_Insert {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
        // 创建请求
        BulkRequest request = new BulkRequest();
        request.add(new IndexRequest().index("user").id("1002").source(XContentType.JSON, "name", "zhangsan", "age", "30"));
        request.add(new IndexRequest().index("user").id("1003").source(XContentType.JSON, "name", "lisi", "age", "40"));
        request.add(new IndexRequest().index("user").id("1004").source(XContentType.JSON, "name", "wangwu", "age", "50"));
        // 发送请求
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println("took:" + response.getTook());
        BulkItemResponse[] items = response.getItems();
        for (BulkItemResponse item : items) {
            System.out.println("_id:" + item.getId());
        }
        // 关闭客户端
        client.close();
    }

}
