package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

public class Test06_Doc_Update {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 修改文档 请求对象
        UpdateRequest request = new UpdateRequest();

        // 配置修改参数
        request.index("user").id("1001");

        // 配置请求体，对数据进行修改
        request.doc(XContentType.JSON, "sex", "女");

        // 发送请求
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        System.out.println("_index:" + response.getIndex());
        System.out.println("_id:" + response.getId());
        System.out.println("_result:" + response.getResult());

        // 关闭客户端
        client.close();
    }
}
