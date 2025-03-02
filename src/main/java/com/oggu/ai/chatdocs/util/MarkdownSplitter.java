package com.oggu.ai.chatdocs.util;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : bhask
 * Created : 02-26-2025
 */
public class MarkdownSplitter {

    public static void main(String[] args) {
        // Example markdown data (story)
        String markdownText = "### Character 1: Evelyn\n" +
                "- **Role**: The brave, adventurous young woman.\n" +
                "- **Traits**: Loves solving mysteries and discovering hidden things.\n" +
                "- **Goal**: To find the hidden treasure and unravel the secrets of the mysterious journal.\n\n" +
                "### Chapter 1: The Hidden Map\n" +
                "Evelyn found an ancient journal with a mysterious treasure map. She shared it with her friends...\n\n" +
                "### Chapter 2: Into the Unknown\n" +
                "The group set out on their journey...\n\n" +
                "### Event 1: Finding the Hidden Cave\n" +
                "Evelyn and her friends found the hidden cave marked on the treasure map...\n\n" +
                "### Event 2: Solving the Door Puzzle\n" +
                "The group encountered a massive stone door with a complex locking mechanism...\n\n" +
                "### Character 2: Max\n" +
                "- **Role**: Tech-savvy programmer and hacker.\n" +
                "- **Traits**: Witty, intelligent, always ready with a sarcastic comment.\n" +
                "- **Goal**: To use his hacking skills to help the team overcome technological obstacles.";

        // Split markdown text into chunks
        List<Document> chunks = splitMarkdownIntoChunks(markdownText);

        // Output chunks to verify
        for (Document chunk : chunks) {
            System.out.println("---- CHUNK ----");
            System.out.println(chunk);
            System.out.println("---------------");
        }
    }

    // Function to split the markdown text into chunks based on headings and events
    public static List<Document> splitMarkdownIntoChunks(String markdownText) {
        List<Document> chunks = new ArrayList<>();

        // Regex pattern to match headings (e.g., Chapter, Event, or Character)
        Pattern pattern = Pattern.compile("###\\s(.+?)\\s*(?=\n|$)");
        Matcher matcher = pattern.matcher(markdownText);

        // Keep track of the last position where we found a heading
        int lastPosition = 0;

        while (matcher.find()) {
            // Get the title of the heading
            String heading = matcher.group(1).trim();

            // The chunk is from the last matched position to the current matched heading
            String chunk = markdownText.substring(lastPosition, matcher.end()).trim();

            // Add the chunk to the list
            chunks.add(new Document(chunk));

            // Update the last position to the end of the current match
            lastPosition = matcher.end();
        }

        // Add the remaining part of the markdown text as the last chunk
        if (lastPosition < markdownText.length()) {
            chunks.add(new Document(markdownText.substring(lastPosition).trim()));
        }

        return chunks;
    }


    public static List<Document> splitMarkdownIntoChunks(MultipartFile file) {
        return splitMarkdownIntoChunks(CommonUtils.convertMultipartFileToString(file));
    }
}

