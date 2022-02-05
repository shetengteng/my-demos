package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class Test08_Doc_Delete {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
        // 构建请求
        DeleteRequest request = new DeleteRequest().index("user").id("1001");
        // 发送请求
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        // 返回结果
        System.out.println(response.toString());

        // 关闭客户端
        client.close();
    }
}
