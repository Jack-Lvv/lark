package com.cqupt.lark.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    @Value("${milvus.uri}")
    private String uri;
    @Value("${milvus.token}")
    private String token;

    @Bean
    public MilvusClientV2 milvusClient() {
        return new MilvusClientV2(
                ConnectConfig.builder()
                        .uri(uri)
                        .token(token)
                        .build()
        );
    }
}
