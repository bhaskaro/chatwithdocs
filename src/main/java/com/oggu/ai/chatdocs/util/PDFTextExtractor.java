package com.oggu.ai.chatdocs.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Author : bhask
 * Created : 02-24-2025
 */
@Component
public class PDFTextExtractor {

    private static final Logger logger = LogManager.getLogger();

    public List<Document> extractText(MultipartFile file) {

        //load pdbfs
        Resource pdfResource = convertMultipartFileToResource(file);

        return extractText(pdfResource);
    }

    public List<Document> extractText(Resource pdfResource) {

        //load pdbfs
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder().withPagesPerDocument(1).build();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);

        TokenTextSplitter textSplitter = new TokenTextSplitter();

        return textSplitter.apply(reader.get());
    }

    public Resource convertMultipartFileToResource(MultipartFile file) {

        // Convert the MultipartFile to a byte array
        byte[] fileBytes = null;
        try {
            fileBytes = file.getBytes();

            // Return as a ByteArrayResource
            return new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
