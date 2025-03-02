package com.oggu.ai.chatdocs.controller;

import com.oggu.ai.chatdocs.util.DataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Author : bhask
 * Created : 02-21-2025
 */
@RestController
public class ChatController {

    private static final Logger logger = LogManager.getLogger();

    private final String prompt = """
            Your task is to answer the questions. Use the information from the DOCUMENTS section to provide accurate answers.
            If unsure or if the answer isn't found in the DOCUMENTS section, simply state that you don't know the answer.
            
            Respond to the following QUESTION in strict HTML format. Include only HTML elements, such as `<h1>`, `<p>`, `<b>`, `<i>`, `<ul>`, `<li>`, etc., as appropriate for the content. Ensure the output is valid HTML and suitable for embedding directly into a webpage.
            
            QUESTION:
            {input}
            
            DOCUMENTS:
            {documents}
            
            """;

    private final String prompt2 = """
            Your task is to answer the following question based on the information provided in the DOCUMENTS section.
            
            Only use the content from the DOCUMENTS section to provide your answer.
            If the answer is not explicitly mentioned or is unclear, respond with "I don't know the answer."
            Important:
            
            Respond strictly in valid HTML format only.
            Your response should be fully structured using appropriate HTML tags. Do not include any text outside of the HTML tags.
            Use the following HTML elements where appropriate:
            <h1> or <h2> for headings.
            <p> for paragraphs.
            <ul> and <ol> for lists (unordered and ordered lists, respectively).
            <b> for bold text and <i> for italics to highlight important points.
            <br> for line breaks if needed to separate distinct sections or content.
            Ensure that your HTML is clean, valid, and well-formed with all tags properly opened and closed.
            
            QUESTION:
            {input}
            
            DOCUMENTS:
            {documents}
            """;

    private final OllamaChatModel chatModel;

    @Autowired
    private DataLoader dataLoader;

    @Autowired
    public ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map<String, String> generate(@RequestParam(name = "message", defaultValue = "Tell me a joke") String message) {

        logger.info("Reveiced message : {}", message);
        return Map.of("generation", this.chatModel.call(message));
    }

    @PostMapping(value = "/ai/chat")
    public Map<String, String> aiChat(@RequestBody String question) {

        logger.info("Received post chat question : {}", question);
        logger.info("prompt used  : {}", prompt2);

        PromptTemplate template = new PromptTemplate(prompt2);
        Map<String, Object> promptsParameters = new HashMap<>();
        promptsParameters.put("input", question);
        promptsParameters.put("documents", dataLoader.findSimilarData(question));

        logger.debug("promptsParameters ----------- > {}", promptsParameters);

        String response = chatModel
                .call(template.create(promptsParameters))
                .getResult()
                .getOutput()
                .getText();

        logger.info("response : -----> {}", response);
        return Map.of("message", response);
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));

        return this.chatModel.stream(prompt);
    }

    @GetMapping("/chat")
    public ModelAndView getAll(Model model) {

        return new ModelAndView("chat");
    }


}
