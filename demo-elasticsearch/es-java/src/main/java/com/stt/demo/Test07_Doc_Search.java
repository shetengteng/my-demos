package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class Test07_Doc_Search {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 创建请求对象
        GetRequest request = new GetRequest();
        // 配置主键查询
        request.index("user").id("1001");
        // 发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 查看结果
        System.out.println("_index:" + response.getIndex());
        System.out.println("_type:" + response.getType());
        System.out.println("_id:" + response.getId());
        System.out.println("_source:" + response.getSource());

        // 关闭客户端
        client.close();
    }
}
