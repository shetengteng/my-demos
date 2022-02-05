package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;

public class Test15_Doc_Query_Filter {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 创建查询请求对象
        SearchRequest request = new SearchRequest();
        request.indices("student");

        // 构建查询请求体
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());

        // 查询字段过滤
        String[] excludes = {};
        String[] includes = {"name", "age"};
        builder.fetchSource(includes, excludes);

        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 查询后匹配
        System.out.println("took:" + response.getTook());
        System.out.println("time out:" + response.isTimedOut());

        SearchHits hits = response.getHits();

        System.out.println("total:" + hits.getTotalHits());
        System.out.println("max score:" + hits.getMaxScore());
        System.out.println("-----hits------");
        for (SearchHit hit : hits) {
            // 输出每条查询的记录
            System.out.println(hit.getSourceAsString());
        }

        // 关闭客户端
        client.close();
    }
}
