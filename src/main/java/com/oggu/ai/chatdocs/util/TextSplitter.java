package com.oggu.ai.chatdocs.util;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : bhask
 * Created : 02-26-2025
 */
public class TextSplitter {

    public static void main(String[] args) throws IOException {
        // Load the text file (replace with your actual file path)
        File file = new File("your-text-document.txt");
        StringBuilder textContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textContent.append(line).append("\n");
            }
        }

        // Split the text based on the detected split type
        List<Document> chunks = splitText(textContent.toString());

        // Output the chunks
        int chunkNum = 1;
        for (Document chunk : chunks) {
            System.out.println("---- Chunk " + chunkNum + " ----");
            System.out.println(chunk);
            System.out.println("---------------");
            chunkNum++;
        }
    }

    // Default method to split the text based on detected split type
    public static List<Document> splitText(String text) {
        // Automatically detect the split type based on the content
        String splitType = detectSplitType(text);

        // Split the text based on the detected split type
        List<String> splitText = splitTextBasedOnType(text, splitType);
        List<Document> documents = new ArrayList<>(splitText.size());

        splitText.forEach(x -> documents.add(new Document(x)));


        return documents;
    }

    // Detect the split type based on content
    public static String detectSplitType(String text) {
        // Check for common split types based on regular expressions or patterns

        // Detect "Page" markers (e.g., "--- Page 1 ---")
        if (text.matches(".*---\\s*Page\\s*\\d+\\s*---.*")) {
            return "page";
        }

        // Detect "Chapter" markers (e.g., "Chapter 1", "Chapter 2", ...)
        if (text.matches(".*Chapter\\s+\\d+.*")) {
            return "chapter";
        }

        // Detect Paragraphs by checking for blank lines
        if (text.matches(".*\\n\\n.*")) {
            return "paragraph";
        }

        // If no specific markers are found, default to lines per page
        return "lines";
    }

    // Main function to decide the splitting logic based on the splitType
    public static List<String> splitTextBasedOnType(String text, String splitType) {
        switch (splitType.toLowerCase()) {
            case "page":
                return splitTextByPages(text);
            case "chapter":
                return splitTextByChapters(text);
            case "lines":
                return splitTextByLines(text, 40); // Example: 40 lines per chunk
            case "paragraph":
                return splitTextByParagraphs(text);
            default:
                System.out.println("Invalid split type.");
                return new ArrayList<>();
        }
    }

    // Function to split the text by page markers (e.g., "--- Page 1 ---")
    public static List<String> splitTextByPages(String text) {
        List<String> chunks = new ArrayList<>();

        // Regex pattern to match page markers (e.g., "--- Page 1 ---")
        Pattern pattern = Pattern.compile("---\\s*Page\\s*\\d+\\s*---");
        Matcher matcher = pattern.matcher(text);

        // Add a start position before the first match
        int lastPosition = 0;

        while (matcher.find()) {
            // Capture the text between the last position and the current match
            String chunk = text.substring(lastPosition, matcher.start()).trim();

            // If there's content, add it to the chunks
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Update the last position to the end of the current match
            lastPosition = matcher.end();
        }

        // Add the remaining content after the last page marker
        if (lastPosition < text.length()) {
            chunks.add(text.substring(lastPosition).trim());
        }

        return chunks;
    }

    // Function to split the text by chapter markers (e.g., "Chapter 1", "Chapter 2", ...)
    public static List<String> splitTextByChapters(String text) {
        List<String> chunks = new ArrayList<>();

        // Regex pattern to match chapter headings (e.g., "Chapter 1", "Chapter 2", ...)
        Pattern pattern = Pattern.compile("Chapter\\s+\\d+");
        Matcher matcher = pattern.matcher(text);

        // Add a start position before the first match
        int lastPosition = 0;

        while (matcher.find()) {
            // Capture the text between the last position and the current match
            String chunk = text.substring(lastPosition, matcher.start()).trim();

            // If there's content, add it to the chunks
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Update the last position to the end of the current match
            lastPosition = matcher.end();
        }

        // Add the remaining content after the last chapter marker
        if (lastPosition < text.length()) {
            chunks.add(text.substring(lastPosition).trim());
        }

        return chunks;
    }

    // Function to split the text into chunks based on lines per page
    public static List<String> splitTextByLines(String text, int linesPerPage) {
        List<String> chunks = new ArrayList<>();
        String[] lines = text.split("\n"); // Split the text into lines

        StringBuilder chunk = new StringBuilder();
        int lineCount = 0;

        for (String line : lines) {
            chunk.append(line).append("\n");
            lineCount++;

            // If we've reached the defined number of lines per page, create a new chunk
            if (lineCount == linesPerPage) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();  // Reset for the next chunk
                lineCount = 0;  // Reset line count
            }
        }

        // Add the remaining content if any lines are left
        if (!chunk.isEmpty()) {
            chunks.add(chunk.toString().trim());
        }

        return chunks;
    }

    // Function to split the text by paragraphs (using blank lines as delimiters)
    public static List<String> splitTextByParagraphs(String text) {
        List<String> chunks = new ArrayList<>();

        // Split the text by two or more newline characters (to separate paragraphs)
        String[] paragraphs = text.split("\n{2,}");

        // Add each paragraph as a chunk
        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                chunks.add(paragraph.trim());
            }
        }

        return chunks;
    }

    public static List<Document> splitText(MultipartFile file) {

        return splitText(CommonUtils.convertMultipartFileToString(file));
    }

}
