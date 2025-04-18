package org.example.milvusop.controller;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetIndexBuildProgressResponse;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.GetIndexBuildProgressParam;
import io.milvus.param.partition.CreatePartitionParam;
import org.example.milvusop.constants.FaceArchive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MilvusCreatedController {

    @Autowired
    private MilvusServiceClient milvusServiceClient;

    @GetMapping("/created")
    void createMilvus() {

        // 设置列
        FieldType archiveId = FieldType.newBuilder()
                .withName(FaceArchive.Field.ARCHIVE_ID)
                .withDescription("主键id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        FieldType orgId = FieldType.newBuilder()
                .withName(FaceArchive.Field.ORG_ID)
                .withDescription("组织id")
                .withDataType(DataType.Int32)
                .build();
        FieldType archiveFeature = FieldType.newBuilder()
                .withName(FaceArchive.Field.ARCHIVE_FEATURE)
                .withDescription("档案特征值")
                .withDataType(DataType.FloatVector)
                .withDimension(FaceArchive.FEATURE_DIM)
                .build();

        // 判断该集合名是否存在
        R<Boolean> r = milvusServiceClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(FaceArchive.COLLECTION_NAME)
                        .build());

        System.out.println("r:" + r);

        if (!r.getData()) {
            // 写入
            CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                    .withCollectionName(FaceArchive.COLLECTION_NAME)
                    .withDescription("档案集合")
                    .withShardsNum(FaceArchive.SHARDS_NUM)
                    .addFieldType(archiveId)
                    .addFieldType(orgId)
                    .addFieldType(archiveFeature)
                    .build();
            R<RpcStatus> response = milvusServiceClient.createCollection(createCollectionReq);

            System.out.println("response:" + response);
        } else {
            System.out.println("集合已存在");
        }

    }


    @GetMapping("/partition")
    void createPartition (){

        for (int i = 0; i < FaceArchive.PARTITION_NUM; i++) {
            R<RpcStatus> response = milvusServiceClient.createPartition(CreatePartitionParam.newBuilder()
                    .withCollectionName(FaceArchive.COLLECTION_NAME) //集合名称
                    .withPartitionName(FaceArchive.PARTITION_PREFIX + i) //分区名称
                    .build());

            System.out.println("response" + i + ":" + response);
        }

    }


    @GetMapping("index")
    void createIndex() {
        R<RpcStatus> response = milvusServiceClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(FaceArchive.COLLECTION_NAME)
                .withFieldName(FaceArchive.Field.ARCHIVE_FEATURE)
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.IP)
                //nlist 建议值为 4 × sqrt(n)，其中 n 指 segment 最多包含的 entity 条数。
                .withExtraParam("{\"nlist\":16384}")
                .withSyncMode(Boolean.FALSE)
                .build());
        System.out.println("response:" + response);
        R<GetIndexBuildProgressResponse> idnexResp = milvusServiceClient.getIndexBuildProgress(
                GetIndexBuildProgressParam.newBuilder()
                        .withCollectionName(FaceArchive.COLLECTION_NAME)
                        .build());
        System.out.println("idnexResp:" + idnexResp);
    }

}
