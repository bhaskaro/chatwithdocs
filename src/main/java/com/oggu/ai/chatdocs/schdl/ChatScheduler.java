package com.oggu.ai.chatdocs.schdl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Author : bhask
 * Created : 02-21-2025
 */
@Component
public class ChatScheduler {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private OllamaChatModel chatModel;

    @Autowired
    private DataSource dataSource;

    //    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void callModel() {
        String query = "Hi, how are you ?";
        logger.info("Calling chatModel with query : {}", query);
        String response = this.chatModel.call(query);
        logger.info("Response : {}", response);
    }

    //    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void checkDatasource() throws SQLException {
        System.out.println("dataSource.getConnection : " + dataSource.getConnection());
    }
}
