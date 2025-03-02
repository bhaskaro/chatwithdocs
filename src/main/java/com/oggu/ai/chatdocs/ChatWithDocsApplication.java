package com.oggu.ai.chatdocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatWithDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatWithDocsApplication.class, args);
    }

}
