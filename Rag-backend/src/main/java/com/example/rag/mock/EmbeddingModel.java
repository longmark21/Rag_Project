package com.example.rag.mock;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟 EmbeddingModel 类，用于文本向量化
 */
public class EmbeddingModel {

    /**
     * 模拟 embed 方法
     */
    public float[] embed(String text) {
        // 模拟生成一个 1536 维的向量
        float[] embedding = new float[1536];
        for (int i = 0; i < 1536; i++) {
            embedding[i] = (float) Math.random();
        }
        return embedding;
    }

    /**
     * 模拟 call 方法
     */
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts = request.getTexts();
        List<EmbeddingResult> results = new ArrayList<>();

        for (String text : texts) {
            float[] embedding = embed(text);
            List<Float> floatList = new ArrayList<>();
            for (float f : embedding) {
                floatList.add(f);
            }
            results.add(new EmbeddingResult(floatList));
        }

        return new EmbeddingResponse(results);
    }

    /**
     * 模拟 EmbeddingRequest 类
     */
    public static class EmbeddingRequest {
        private List<String> texts;
        private Object options;

        public EmbeddingRequest(List<String> texts, Object options) {
            this.texts = texts;
            this.options = options;
        }

        public List<String> getTexts() {
            return texts;
        }
    }

    /**
     * 模拟 EmbeddingResponse 类
     */
    public static class EmbeddingResponse {
        private List<EmbeddingResult> results;

        public EmbeddingResponse(List<EmbeddingResult> results) {
            this.results = results;
        }

        public List<EmbeddingResult> getResults() {
            return results;
        }
    }

    /**
     * 模拟 EmbeddingResult 类
     */
    public static class EmbeddingResult {
        private List<Float> output;

        public EmbeddingResult(List<Float> output) {
            this.output = output;
        }

        public List<Float> getOutput() {
            return output;
        }
    }
}
