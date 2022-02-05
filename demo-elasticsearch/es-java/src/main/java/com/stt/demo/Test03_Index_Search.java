package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;

import java.io.IOException;

public class Test03_Index_Search {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
        // 【查询索引】 请求
        GetIndexRequest request = new GetIndexRequest("user");
        // 发送请求，获取响应，如果不存在则会抛出异常
        GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

        System.out.println("aliases:" + response.getAliases());
        System.out.println("mappings:" + response.getMappings());
        System.out.println("settings:" + response.getSettings());

        // 关闭客户端
        client.close();
    }
}
