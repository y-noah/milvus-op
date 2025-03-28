package org.example.milvusop.controller;

import com.google.gson.Gson;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.R;
import io.milvus.param.highlevel.dml.QuerySimpleParam;
import io.milvus.param.highlevel.dml.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class MilvusController {
    @Autowired
    private MilvusServiceClient milvusServiceClient;

    @GetMapping("/qry")
    void qryMilvis() {

        // 创建查询参数
        QuerySimpleParam builder = QuerySimpleParam.newBuilder()
                .withCollectionName("test")
                .withOutputFields(Arrays.asList(new String[]{"id", "embedding", "name"}))
                .withFilter("id > 0")
                .withLimit(1L)
                .build();

        // 执行查询操作
        R<QueryResponse> query = milvusServiceClient.query(builder);

        // 获取查询结果
        QueryResponse data = query.getData();

        // 使用Gson将查询结果转换为JSON格式
        Gson gson = new Gson();

        // 打印JSON格式的查询结果
        System.out.println(gson.toJson(data));

        // 关闭客户端连接
        milvusServiceClient.close();

    }

}
