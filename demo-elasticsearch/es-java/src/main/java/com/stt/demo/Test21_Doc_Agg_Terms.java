package com.stt.demo;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public class Test21_Doc_Agg_Terms {
    public static void main(String[] args) throws IOException {
        // 创建客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 创建查询请求对象
        SearchRequest request = new SearchRequest();
        request.indices("student");

        // 构建查询请求体
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .aggregation(AggregationBuilders.terms("age_groupby").field("age"));

        builder.size(0);
        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 查询后匹配
        System.out.println("took:" + response.getTook());
        System.out.println("time out:" + response.isTimedOut());

        for (Aggregation agg : response.getAggregations()) {
            System.out.println(agg.getName());
            for (Terms.Bucket bucket : ((ParsedTerms) agg).getBuckets()) {
                System.out.println(bucket.getKey()+" doc_count:"+bucket.getDocCount());
            }
        }

        // 关闭客户端
        client.close();
    }
}