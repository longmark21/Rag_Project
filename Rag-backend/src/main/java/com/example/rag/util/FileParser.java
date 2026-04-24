package com.example.rag.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Component
public class FileParser {

    public String parseFile(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在或不是有效文件：" + file.getAbsolutePath());
        }

        String fileName = file.getName().toLowerCase();
        log.info("开始解析文件：文件名={}, 大小={}MB", fileName, file.length() / 1024.0 / 1024.0);
        
        try {
            String text;
            if (fileName.endsWith(".pdf")) {
                text = parsePDF(file);
            } else if (fileName.endsWith(".doc")) {
                text = parseDoc(file);
            } else if (fileName.endsWith(".docx")) {
                text = parseDocx(file);
            } else if (fileName.endsWith(".txt")) {
                text = parseTxt(file);
            } else {
                throw new UnsupportedOperationException("不支持的文件类型：" + fileName);
            }

            text = cleanText(text);
            log.info("文件解析成功：文件名={}, 清洗后字符数={}", fileName, text.length());
            return text;
        } catch (Exception e) {
            log.error("文件解析失败：文件名={}", fileName, e);
            throw new IOException("文件解析失败：" + e.getMessage(), e);
        }
    }

    private String parsePDF(File file) throws IOException {
        log.info("PDF 解析开始: 文件名={}", file.getName());
        try (PDDocument document = Loader.loadPDF(file)) {
            if (document.isEncrypted()) {
                log.warn("PDF 文件已加密，尝试解密: 文件名={}", file.getName());
                throw new IOException("PDF 文件已加密，无法解析：" + file.getName());
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(false);

            int pageCount = document.getNumberOfPages();
            log.info("PDF 页数: {}", pageCount);

            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                text.append(pageText);
                if (i < pageCount) {
                    text.append("\n\n");
                }
            }

            String result = text.toString();
            log.info("PDF 解析完成: 文件名={}, 页数={}, 字符数={}", file.getName(), pageCount, result.length());
            return result;
        } catch (IOException e) {
            log.error("PDF 文件解析失败: 文件名={}", file.getName(), e);
            throw new IOException("PDF 文件解析失败：" + e.getMessage(), e);
        }
    }

    private String parseDoc(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            
            String text = extractor.getText();
            log.info("DOC 解析完成：原始字符数={}", text.length());
            return text;
        } catch (Exception e) {
            log.error("DOC 文件解析失败", e);
            throw new IOException("DOC 文件解析失败：" + e.getMessage(), e);
        }
    }

    private String parseDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder text = new StringBuilder();
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String paragraphText = paragraph.getText().trim();
                if (!paragraphText.isEmpty()) {
                    text.append(paragraphText).append("\n\n");
                }
            }
            
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder rowText = new StringBuilder();
                    row.getTableCells().forEach(cell -> {
                        String cellText = cell.getText().trim();
                        if (!cellText.isEmpty()) {
                            if (rowText.length() > 0) {
                                rowText.append(" | ");
                            }
                            rowText.append(cellText);
                        }
                    });
                    if (rowText.length() > 0) {
                        text.append(rowText).append("\n");
                    }
                }
                text.append("\n");
            }
            
            log.info("DOCX 解析完成：段落数={}, 表格数={}, 原始字符数={}", 
                paragraphs.size(), tables.size(), text.length());
            return text.toString();
        } catch (Exception e) {
            log.error("DOCX 文件解析失败", e);
            throw new IOException("DOCX 文件解析失败：" + e.getMessage(), e);
        }
    }

    private String parseTxt(File file) throws IOException {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            
            Charset[] encodings = {
                StandardCharsets.UTF_8,
                Charset.forName("GBK"),
                Charset.forName("GB2312"),
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII
            };
            
            String text = null;
            Charset detectedCharset = StandardCharsets.UTF_8;
            
            for (Charset encoding : encodings) {
                try {
                    String decoded = new String(fileContent, encoding);
                    if (!decoded.contains("") || encoding == StandardCharsets.ISO_8859_1) {
                        text = decoded;
                        detectedCharset = encoding;
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            if (text == null) {
                text = new String(fileContent, StandardCharsets.UTF_8);
            }
            
            log.info("TXT 解析完成：检测编码={}, 原始字符数={}", detectedCharset.name(), text.length());
            return text;
        } catch (Exception e) {
            log.error("TXT 文件解析失败", e);
            throw new IOException("TXT 文件解析失败：" + e.getMessage(), e);
        }
    }

    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        text = text
            .replaceAll("[\\r\\n]+", "\n")
            .replaceAll("[\\s\\u00A0]+", " ")
            .replaceAll("\\t+", " ")
            .replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\u200B-\\u200D\\uFEFF]", "")
            .replaceAll("[\\u3000\\uFFFC\\uFFFD]", "")
            .replaceAll("\\s+", " ")
            .replaceAll(" +", " ")
            .replaceAll(" \\.+", ".")
            .replaceAll("\\n\\s*\\n\\s*\\n+", "\n\n")
            .trim();

        text = text.replaceAll("([。！？；：，、])\n", "$1\n\n");
        text = text.replaceAll("([a-zA-Z])([\\u4e00-\\u9fa5])", "$1 $2");
        text = text.replaceAll("([\\u4e00-\\u9fa5])([a-zA-Z])", "$1 $2");
        text = text.replaceAll("([0-9])([\\u4e00-\\u9fa5])", "$1 $2");
        text = text.replaceAll("([\\u4e00-\\u9fa5])([0-9])", "$1 $2");

        return text;
    }
}
