package com.oggu.ai.chatdocs.util;

import com.oggu.ai.chatdocs.entity.KBDocument;
import com.oggu.ai.chatdocs.repository.KBDocumentRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class DataLoader {

    private static final Logger logger = LogManager.getLogger();

    @Value("classpath:Software developer profiles.pdf")
    private Resource pdfResource;

    @Autowired
    private PDFTextExtractor pdfTextExtractor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KBDocumentRepository kbDocumentRepository;

    @PostConstruct
    public void addVectorColum() {

        String tableName = "kb_documents";
        String columnName = "vector";

        // Query to check if the column exists in the table
        String sql = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME  = ? AND COLUMN_NAME  = ?";

        // Check if the column exists
        Integer columnCount = jdbcTemplate.queryForObject(sql, new Object[]{tableName.toUpperCase(), columnName.toUpperCase()}, Integer.class);

        if (columnCount < 1) {
            logger.info("Vector column doesnt exist, adding it now.");
            jdbcTemplate.execute("ALTER TABLE kb_documents ADD (kb_documents_vector VECTOR)");
        } else {
            logger.info("Vector column already present, not adding it now.");
        }

        //initialize with some test data.
//        initTempData();
//        populateVectorColumn(fileName);
    }

    public void populateVectorColumn(String fileName) {

        //cleanup old records.
//        kbDocumentRepository.deleteAll();
//        jdbcTemplate.execute("update kb_documents set kb_documents_vector = NULL");

        StringBuilder sb = new StringBuilder();
        sb.append("update kb_documents \n");
        sb.append("set kb_documents_vector = vector_embedding(\n");
        sb.append("    all_minilm_l12_v2 \n");
        sb.append("    using concat(' ', DBMS_LOB.SUBSTR(DATA, 1000, 1)) as data\n");
        sb.append(") where file_name = '").append(fileName).append("'");

        logger.info("populating vector with the data, please wait...");
        jdbcTemplate.execute(sb.toString());
        logger.info("populated vector with the data.");
    }


    //TODO only for testing,can be removed later.
    public void initTempData() {

        //load pdbfs
        List<Document> documents = pdfTextExtractor.extractText(pdfResource);

        String fileName = pdfResource.getFilename();
        kbDocumentRepository.deleteAll();

        for (int i = 0; i < documents.size(); i++) {
            logger.info(" {} inserting ------> {}  document line {}", fileName, i, documents.get(i).getText().substring(0, 10));

            KBDocument pdfDocument = new KBDocument();

            pdfDocument.setFileName(fileName);
            pdfDocument.setFileChunksNumber(i + 1);
            pdfDocument.setData(documents.get(i).getText());

            pdfDocument = kbDocumentRepository.save(pdfDocument);

            logger.info("Saved pdfDocument : {}", pdfDocument);
        }

        // int totalRowCount = jdbcClient.sql("select count(*) from pdf_documents").query(Integer.class).single();
        logger.info("Inserted into pdf_documents {}", kbDocumentRepository.count());
    }

    public String findSimilarData(String question) {

        StringBuilder output = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT vector_distance(kb_documents_vector, (vector_embedding(all_minilm_l12_v2 using ? as data))) as distance,\n");
        sb.append("       FILE_CHUNKS_NUMBER as FILE_CHUNKS_NUMBER,\n");
        sb.append("       file_name as file_name,\n");
        sb.append("       data as data\n");
        sb.append("FROM   kb_documents\n");
        sb.append("order by 1\n");
        sb.append("fetch approximate first 5 rows only");

        logger.info("Searching vector with query : {}", sb);

        try {
            Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();

            CallableStatement cstmt = conn.prepareCall(sb.toString());
            cstmt.setString(1, question);
            logger.debug("cstmt --- > : {}", cstmt);

            ResultSet rs = cstmt.executeQuery();

            while (rs.next()) {
                //read the OUT parameter now
                double distance = rs.getDouble("DISTANCE");
                int chunkNumber = rs.getInt("FILE_CHUNKS_NUMBER");
                String file_name = rs.getString("file_name");

//                if (distance < 0.7)
                output.append(rs.getString("DATA")).append(" ");

                logger.info("filename, chunkNumber and Distance ------ > {} \t -- {} \t -- \t {}", file_name, chunkNumber, distance);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return output.toString();
    }
}
