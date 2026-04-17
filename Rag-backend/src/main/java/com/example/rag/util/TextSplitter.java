package com.example.rag.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TextSplitter {

    private static final int MIN_CHUNK_SIZE = 500;
    private static final int MAX_CHUNK_SIZE = 800;
    private static final int OVERLAP_SIZE = 100;

    public List<String> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        text = cleanText(text);
        
        if (text.length() <= MAX_CHUNK_SIZE) {
            return Arrays.asList(text);
        }

        return splitByParagraphs(text);
    }

    private String cleanText(String text) {
        return text
            .replaceAll("\\s+", " ")
            .replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "")
            .trim();
    }

    private List<String> splitByParagraphs(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n|\\r\\n\\s*\\r\\n");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmedPara = paragraph.trim();
            if (trimmedPara.isEmpty()) {
                continue;
            }

            if (currentChunk.length() + trimmedPara.length() <= MAX_CHUNK_SIZE) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmedPara);
            } else {
                if (currentChunk.length() >= MIN_CHUNK_SIZE) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder(trimmedPara);
                } else {
                    if (currentChunk.length() > 0) {
                        currentChunk.append(" ");
                    }
                    currentChunk.append(trimmedPara);
                    
                    while (currentChunk.length() >= MAX_CHUNK_SIZE) {
                        String chunk = currentChunk.substring(0, MAX_CHUNK_SIZE);
                        chunks.add(chunk.trim());
                        
                        String remaining = currentChunk.substring(MAX_CHUNK_SIZE);
                        int overlapStart = Math.max(0, remaining.length() - OVERLAP_SIZE);
                        currentChunk = new StringBuilder(remaining.substring(overlapStart));
                    }
                }
            }
        }

        if (currentChunk.length() >= MIN_CHUNK_SIZE) {
            chunks.add(currentChunk.toString());
        } else if (!chunks.isEmpty() && currentChunk.length() > 0) {
            int lastIndex = chunks.size() - 1;
            String lastChunk = chunks.get(lastIndex);
            if (lastChunk.length() + currentChunk.length() <= MAX_CHUNK_SIZE) {
                chunks.set(lastIndex, lastChunk + "\n\n" + currentChunk.toString());
            } else {
                chunks.add(currentChunk.toString());
            }
        } else if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        log.info("文本分块完成: 总块数={}", chunks.size());
        return chunks;
    }

    public List<String> splitWithOverlap(String text, int chunkSize, int overlap) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        text = cleanText(text);
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int step = chunkSize - overlap;

        for (int i = 0; i < length; i += step) {
            int end = Math.min(i + chunkSize, length);
            String chunk = text.substring(i, end).trim();
            
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            
            if (end >= length) {
                break;
            }
        }

        log.info("文本分块完成(带重叠): 总块数={}, 块大小={}, 重叠大小={}", chunks.size(), chunkSize, overlap);
        return chunks;
    }
}
