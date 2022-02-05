package com.stt.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

public class Test05_Doc_Create {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 请求对象
        IndexRequest request = new IndexRequest();
        // 配置文档id
        request.index("user").id("1001");

        // 构建文档对象
        String userJson = createUserJson();

        // 添加文档数据到请求中，格式JSON
        request.source(userJson, XContentType.JSON);

        // 发送请求
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        // 打印结果
        System.out.println("index:" + response.getIndex());
        System.out.println("id:" + response.getId());
        System.out.println("result:" + response.getResult());

        // 关闭客户端
        client.close();
    }

    private static String createUserJson() throws JsonProcessingException {
        User user = new User();
        user.setName("zhangsan");
        user.setAge(30);
        user.setSex("男");
        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        return userJson;
    }
}
