package org.example.milvusop.controller;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;
import org.example.milvusop.constants.FaceArchive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MilvusOperateController {

    @Autowired
    private MilvusServiceClient milvusServiceClient;

    @GetMapping("loadCollection")
    // 加载到内存
    void loadCollection() {
        R<RpcStatus> response = milvusServiceClient.loadCollection(LoadCollectionParam.newBuilder()
                //集合名称
                .withCollectionName(FaceArchive.COLLECTION_NAME)
                .build());

        System.out.println("response:" + response);
    }


    @GetMapping("releaseCollection")
    // 从内存释放
    void releaseCollection() {
        R<RpcStatus> response = milvusServiceClient.releaseCollection(ReleaseCollectionParam.newBuilder()
                .withCollectionName(FaceArchive.COLLECTION_NAME)
                .build());
        System.out.println("response:" + response);
    }
}
