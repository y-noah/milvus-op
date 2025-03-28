package org.example.milvusop.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    @Value("${milvus.config.host}")
    private String ipAddr;
    @Value("${milvus.config.port}")
    private Integer port;

    @Bean
    public MilvusServiceClient milvusServiceClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(ipAddr)
                .withPort(port)
                .build();
        return new MilvusServiceClient(connectParam);
    }
}
