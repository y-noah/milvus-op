package org.example.milvusop.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.highlevel.dml.QuerySimpleParam;
import io.milvus.param.highlevel.dml.response.QueryResponse;
import org.example.milvusop.constants.FaceArchive;
import org.example.milvusop.entity.MilvusArchive;
import org.example.milvusop.utils.MilvusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MilvusController {
    @Autowired
    private MilvusServiceClient milvusServiceClient;

    @Autowired
    private MilvusUtil milvusUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("insert")
    void insert(@RequestBody List<MilvusArchive> data) {
        System.out.println("data:" + data);
        Map<Integer, List<MilvusArchive>> map =
                data.stream()
                        .filter(item -> item.getArchiveFeature() != null)
                        .collect(Collectors.groupingBy(MilvusArchive::getOrgId));

        System.out.println("map" + map);
        map.forEach((orgId, list) -> {
            //插入数据
            List<InsertParam.Field> fields = new ArrayList<>();
            List<Long> archiveIds = Lists.newArrayList();
            List<Integer> orgIds = Lists.newArrayList();
            List<List<Float>> floatVectors = Lists.newArrayList();
            for (MilvusArchive dto : list) {
                archiveIds.add(dto.getArchiveId());
                orgIds.add(dto.getOrgId());
                //虹软特征值转Float向量
                floatVectors.add(milvusUtil.arcsoftToFloat(dto.getArchiveFeature()));
            }
            //档案ID
            fields.add(new InsertParam.Field(FaceArchive.Field.ARCHIVE_ID,archiveIds));
            //小区id
            fields.add(new InsertParam.Field(FaceArchive.Field.ORG_ID,orgIds));
            //特征值
            fields.add(new InsertParam.Field(FaceArchive.Field.ARCHIVE_FEATURE,floatVectors));
            //插入
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(FaceArchive.COLLECTION_NAME)
                    .withPartitionName(FaceArchive.getPartitionName(orgId))
                    .withFields(fields)
                    .build();
            R<MutationResult> insert = milvusServiceClient.insert(insertParam);
            System.out.println("insert:" + insert);
        });
    }

    @GetMapping("deleteEntity")
    void deleteEntity() {
        R<MutationResult> response = milvusServiceClient.delete(
                DeleteParam.newBuilder()
                        //集合名称
                        .withCollectionName(FaceArchive.COLLECTION_NAME)
                        //分区名称
                        .withPartitionName("shards_9")
                        //条件
                        .withExpr("archive_id == 1")
                        .build()
        );
        System.out.println("response:" + response);
    }


    // 相似度查询
    @PostMapping("/search")
    void searchTallestSimilarity(@RequestBody Map<String, Object> request, @RequestParam(required = false) Integer orgId) {
        // 提取并解析 archiveFeature
        List<Float> featureVector = parseFeatureVector(request.get("archiveFeature"));

        List<List<Float>> searchVectors = new ArrayList<>();
        searchVectors.add(featureVector);

        // 构建搜索参数
        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName("face_archive")
                .withMetricType(MetricType.IP) // 余弦相似度 or 内积
                .withTopK(1)
                .withVectors(searchVectors)
                .withVectorFieldName("archive_feature")
                .withParams("{\"nprobe\": 512}");

        if (orgId != null) {
            builder.withExpr("org_id == " + orgId);
            builder.withPartitionNames(List.of("partition_" + orgId));
        }

        R<SearchResults> search = milvusServiceClient.search(builder.build());
        System.out.println("search:" + search);
    }

    private List<Float> parseFeatureVector(Object featureObject) {
        if (featureObject == null) {
            throw new IllegalArgumentException("archiveFeature 不能为空");
        }
        try {
            return objectMapper.convertValue(featureObject, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("无法解析 archiveFeature，请提供正确的 JSON 数组", e);
        }
    }

    // 根据id查询
    @GetMapping("/qry")
    void qryMilvis() {

        // 创建查询参数
        QuerySimpleParam builder = QuerySimpleParam.newBuilder()
                .withCollectionName("face_archive")
                .withOutputFields(Arrays.asList(new String[]{"archive_id", "org_id", "archive_feature"}))
                .withFilter("org_id > 0")
                .withLimit(100L)
                .build();

        // 执行查询操作
        R<QueryResponse> query = milvusServiceClient.query(builder);

        // 获取查询结果
        QueryResponse data = query.getData();

        // 使用Gson将查询结果转换为JSON格式
        Gson gson = new Gson();

        // 打印JSON格式的查询结果
        System.out.println("data:" + gson.toJson(data));

        // 关闭客户端连接
        milvusServiceClient.close();

    }

}
