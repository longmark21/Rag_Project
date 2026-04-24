package com.example.rag.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileParserTest {

    private final FileParser fileParser = new FileParser();

    @Test
    void testParseTxtFile(@TempDir Path tempDir) throws IOException {
        File txtFile = tempDir.resolve("test.txt").toFile();
        String content = "这是一段测试文本。\n第二行内容。\n第三行内容。";
        try (FileOutputStream fos = new FileOutputStream(txtFile)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }

        String result = fileParser.parseFile(txtFile);

        assertNotNull(result);
        assertTrue(result.contains("这是一段测试文本"), "结果应包含中文文本，实际结果: " + result);
        assertTrue(result.contains("第二行内容"), "结果应包含第二行内容，实际结果: " + result);
    }

    @Test
    void testParseTxtFileWithGbkEncoding(@TempDir Path tempDir) throws IOException {
        File txtFile = tempDir.resolve("test_gbk.txt").toFile();
        String content = "GBK编码测试文本";
        try (FileOutputStream fos = new FileOutputStream(txtFile)) {
            fos.write(content.getBytes("GBK"));
        }

        String result = fileParser.parseFile(txtFile);

        assertNotNull(result);
        assertTrue(result.contains("GBK编码测试文本"), "结果应包含GBK编码文本，实际结果: " + result);
    }

    @Test
    void testParseNonExistentFile(@TempDir Path tempDir) {
        File nonExistentFile = tempDir.resolve("not_exist.txt").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            fileParser.parseFile(nonExistentFile);
        });

        assertTrue(exception.getMessage().contains("文件不存在"));
    }

    @Test
    void testParseUnsupportedFileType(@TempDir Path tempDir) throws IOException {
        File unsupportedFile = tempDir.resolve("test.xyz").toFile();
        unsupportedFile.createNewFile();

        IOException exception = assertThrows(IOException.class, () -> {
            fileParser.parseFile(unsupportedFile);
        });

        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }

    @Test
    void testParseEmptyTxtFile(@TempDir Path tempDir) throws IOException {
        File emptyFile = tempDir.resolve("empty.txt").toFile();
        emptyFile.createNewFile();

        String result = fileParser.parseFile(emptyFile);

        assertNotNull(result);
        assertEquals("", result);
    }
}
