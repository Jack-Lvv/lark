package com.cqupt.lark.rag.service.impl;

import com.cqupt.lark.rag.model.QaRecord;
import com.cqupt.lark.rag.repository.RAGRepository;
import com.cqupt.lark.rag.service.RagService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagServiceImpl implements RagService {

    private final MilvusClientV2 milvusClient;
    private final EmbeddingModel embeddingModel;
    private final RAGRepository ragRepository;
    private final Integer topK;
    private final Integer dimension;

    public RagServiceImpl(MilvusClientV2 milvusClient,
                          @Value("${open-ai.doubao-embedding.base-url}") String baseUrl,
                          @Value("${open-ai.doubao-embedding.api-key}") String apiKey,
                          @Value("${open-ai.doubao-embedding.model-name}") String modelName,
                          RAGRepository ragRepository,
                          @Value("${app.config.vector-topK}") Integer topK,
    @Value("${app.config.vector-dimension}") Integer dimension) {
        this.milvusClient = milvusClient;
        this.ragRepository = ragRepository;
        this.topK = topK;
        this.dimension = dimension;
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .dimensions(dimension)
                .build();
    }

    @Override
    public void initMilvusCollection() {
        // 3.1 Create schema
        CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();

        // 3.2 Add fields to schema

        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)  // 设置为自动生成ID
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("vector")
                .dataType(DataType.FloatVector)
                .dimension(dimension)
                .build());


        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(IndexParam.builder()
                .fieldName("vector")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                .build());

        milvusClient.createCollection(CreateCollectionReq.builder()
                .collectionName("collection")
                .collectionSchema(schema)
                .indexParams(indexParams)
                .build());

        // 6. Load the collection
        LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                .collectionName("collection")
                .build();

        milvusClient.loadCollection(loadCollectionReq);
    }

    @Override
    public void saveQaRecord(QaRecord record) {
        // 1. 使用LangChain4j生成向量
        float[] vector = embeddingModel.embed(record.toStringForVector()).content().vector();

        // 2. 存储向量到Milvus
        List<JsonObject> data = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (float v : vector) {
            jsonArray.add(v);
        }
        jsonObject.add("vector", jsonArray);

        data.add(jsonObject);
        InsertReq insertReq = InsertReq.builder()
                .collectionName("collection")
                .data(data)
                .build();

        InsertResp insertResp = milvusClient.insert(insertReq);
        String vectorId = insertResp.getPrimaryKeys().get(0).toString();
        System.out.println(insertResp);

        // 3. 存储元数据到MySQL
        record.setVectorId(vectorId);
        ragRepository.save(record);
    }

    @Override
    public List<String> searchSimilarTexts(QaRecord query) {
        // 1. 生成查询向量
        float[] queryVector = embeddingModel.embed(query.toStringForVector()).content().vector();

        SearchReq searchReq = SearchReq.builder()
                .collectionName("collection")
                .data(Collections.singletonList(new FloatVec(queryVector)))
                .topK(topK)
                .build();

        SearchResp searchResp = milvusClient.search(searchReq);

        List<SearchResp.SearchResult> searchResults = searchResp.getSearchResults().get(0);

        String[] ids = new String[searchResults.size()];
        int index = 0;
        for (SearchResp.SearchResult result : searchResults) {
                ids[index] = result.getId().toString();
                index++;
        }

        // 3. 从MySQL获取元数据
        return ragRepository.findByVectorIdIn(ids)
                .stream().map(QaRecord::getAnswer)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCollection() {
        milvusClient.dropCollection(
                DropCollectionReq.builder()
                        .collectionName("collection")
                        .build()
        );
    }


}
