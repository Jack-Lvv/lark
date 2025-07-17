package com.cqupt.lark.vector.service.impl;

import com.cqupt.lark.vector.model.entity.QaRecord;
import com.cqupt.lark.vector.repository.VectorRepository;
import com.cqupt.lark.vector.service.VectorService;
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

import java.util.*;

@Service
public class VectorServiceImpl implements VectorService {

    private final MilvusClientV2 milvusClient;
    private final EmbeddingModel embeddingModel;
    private final VectorRepository vectorRepository;
    private final Integer topK;
    private final Integer dimension;

    public VectorServiceImpl(MilvusClientV2 milvusClient,
                             @Value("${open-ai.doubao-embedding.base-url}") String baseUrl,
                             @Value("${open-ai.doubao-embedding.api-key}") String apiKey,
                             @Value("${open-ai.doubao-embedding.model-name}") String modelName,
                             VectorRepository vectorRepository,
                             @Value("${app.config.vector-topK}") Integer topK,
                             @Value("${app.config.vector-dimension}") Integer dimension) {
        this.milvusClient = milvusClient;
        this.vectorRepository = vectorRepository;
        this.topK = topK;
        this.dimension = dimension;
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .dimensions(dimension)
                .build();
        //initMilvusCollection();
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
        vectorRepository.save(record);
    }

    @Override
    public List<QaRecord> searchSimilarTexts(QaRecord query) {
        // 1. 生成查询向量
        float[] queryVector = embeddingModel.embed(query.toStringForVector()).content().vector();

        Map<String,Object> extraParams = new HashMap<>();
        extraParams.put("radius", 0.8);
        extraParams.put("range_filter", 1);

        SearchReq searchReq = SearchReq.builder()
                .collectionName("collection")
                .data(Collections.singletonList(new FloatVec(queryVector)))
                .topK(topK)
                .searchParams(extraParams)
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
        return vectorRepository.findByVectorIdIn(ids);
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
