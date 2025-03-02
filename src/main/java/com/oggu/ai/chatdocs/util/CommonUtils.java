package com.oggu.ai.chatdocs.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Author : bhask
 * Created : 02-26-2025
 */
public class CommonUtils {

    /**
     * Converts the content of a MultipartFile to a String.
     *
     * @param file The MultipartFile to be converted
     * @return The content of the file as a String
     * @throws IOException If an I/O error occurs while reading the file
     */
    public static String convertMultipartFileToString(MultipartFile file) {
        // Using Scanner to read the content of the file as a String
        try (Scanner scanner = new Scanner(file.getInputStream(), StandardCharsets.UTF_8)) {
            // Use Scanner to read the file and join the lines
            StringBuilder fileContent = new StringBuilder();
            while (scanner.hasNextLine()) {
                fileContent.append(scanner.nextLine()).append(System.lineSeparator());
            }
            return fileContent.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
