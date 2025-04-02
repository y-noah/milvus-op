package org.example.milvusop.entity;

import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
@Data
public class MilvusArchive {
    private Long archiveId;
    private Integer orgId;
    private String archiveFeature;
}
