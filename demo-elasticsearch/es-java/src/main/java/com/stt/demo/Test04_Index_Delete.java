package com.stt.demo;

import org.apache.http.HttpHost;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class Test04_Index_Delete {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 创建请求
        DeleteIndexRequest request = new DeleteIndexRequest("user");
        // 发送请求
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        // 返回结果
        System.out.println("操作返回：" + response.isAcknowledged());

        // 关闭客户端
        client.close();
    }
}
