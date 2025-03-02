package com.oggu.ai.chatdocs.controller;

import com.oggu.ai.chatdocs.entity.KBDocument;
import com.oggu.ai.chatdocs.repository.KBDocumentRepository;
import com.oggu.ai.chatdocs.util.DataLoader;
import com.oggu.ai.chatdocs.util.MarkdownSplitter;
import com.oggu.ai.chatdocs.util.PDFTextExtractor;
import com.oggu.ai.chatdocs.util.TextSplitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Author : bhask
 * Created : 02-23-2025
 */
@Controller
public class FileUploadController {

    private static final Logger logger = LogManager.getLogger();

    private final List<String> uploadedFiles = new ArrayList<>();
    @Autowired
    private PDFTextExtractor pdfTextExtractor;
    @Autowired
    private KBDocumentRepository kbDocumentRepository;
    @Autowired
    private DataLoader dataLoader;

    // Handle the file upload request
    @PostMapping("/file/upload")
    public ResponseEntity<Map<String, Object>> handleFileUpload(@RequestParam("files") MultipartFile[] files, Model model) throws IOException {

        logger.info("Uploaded file length {} and file name   : {}", files.length, files[0].getOriginalFilename());

        for (MultipartFile file : files) {

            String fileName = file.getOriginalFilename();
            List<Document> documents = null;

            if (fileName == null || (!fileName.toLowerCase().endsWith(".pdf") &&
                    !fileName.toLowerCase().endsWith(".txt") &&
                    !fileName.toLowerCase().endsWith(".md"))) {

                // Create a Map to hold the response data
                Map<String, Object> response = new HashMap<>();
                response.put("status", 400);
                response.put("error", "File should be of pdf/txt/md type. Bad request type found.");

                logger.error("uploaded file type {} is not compatible, returning.", fileName);
                // Return a Bad Request response with the Map
                return ResponseEntity.badRequest().body(response);
            }

            if (fileName.toLowerCase().endsWith(".pdf")) {
                documents = pdfTextExtractor.extractText(file);
            } else if (fileName.toLowerCase().endsWith(".txt")) {
                documents = TextSplitter.splitText(file);
            } else if (fileName.toLowerCase().endsWith(".md")) {
                documents = MarkdownSplitter.splitMarkdownIntoChunks(file);
            }

            if (documents != null && !documents.isEmpty()) {
                for (int i = 0; i < documents.size(); i++) {

                    if (logger.isDebugEnabled()) {
                        String tempText = (documents.get(i).getText().length() > 10 ? documents.get(i).getText().substring(0, 10) : documents.get(i).getText());
                        logger.info(" {} inserting ------> {}  document line {}", fileName, i, tempText);
                    }

                    KBDocument pdfDocument = new KBDocument();

                    pdfDocument.setFileName(fileName);
                    pdfDocument.setFileChunksNumber(i + 1);
                    pdfDocument.setData(documents.get(i).getText());

                    kbDocumentRepository.save(pdfDocument);

                    logger.info("Processed PDF document successfully for : {}, inserted {} pages of file.", file.getOriginalFilename(), documents.size());
                }

                logger.info("initializing the data to vector after adding the file {}", fileName);
                dataLoader.populateVectorColumn(fileName);
            }



            uploadedFiles.add(file.getOriginalFilename());  // Add the uploaded file name to the list
        }

        // Create the response map
        Map<String, Object> response = new HashMap<>();
        response.put("message", "File uploaded successfully");
        response.put("uploadedFiles", uploadedFiles);  // Add the uploaded files list

        // Return the response as JSON
        return ResponseEntity.ok(response);  // This will return the JSON response with status 200
    }

    @PostMapping("/file/delete")
    public ResponseEntity<Map<String, Object>> handleFileDelete(@RequestParam("fileName") String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Logic to delete the file (e.g., delete from disk or database)
            List<KBDocument> byFileName = kbDocumentRepository.findByFileName(fileName);

            if (!byFileName.isEmpty()) {
                kbDocumentRepository.deleteAll(byFileName);

                logger.info("initializing the data to vector after deleting the file : {}", fileName);
                dataLoader.populateVectorColumn(fileName);

                uploadedFiles.remove(fileName);
                response.put("message", "File deleted successfully");
                response.put("uploadedFiles", uploadedFiles);

                return ResponseEntity.ok(response);
            } else {
                response.put("message", "File not found or failed to delete");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "Error deleting file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/file/list")
    public ResponseEntity<Map<String, Object>> getUploadedFiles() {

        Map<String, Object> response = new HashMap<>();
        response.put("uploadedFiles", uploadedFiles);
        return ResponseEntity.ok(response);  // Return the list of files as JSON
    }

}

