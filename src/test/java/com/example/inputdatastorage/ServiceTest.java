package com.example.inputdatastorage;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ServiceTest {

    @Autowired
    private Service service;

    private final Logger logger = LoggerFactory.getLogger(ServiceTest.class);

    @Test
    public void insertDataToTriplestoreTest() throws IOException {
        String path = "Qanary-question-answering-components/qanary-component-KG2KG-TranslateAnnotationsOfInstance/";
        BufferedReader reader = new BufferedReader(new FileReader(path + "log.txt"));
        String line = reader.readLine();
        assertNotNull(line);
        while(line != null) {
            logger.info(line);
            assertNotNull(line);
            line = reader.readLine();
        }
    }



}
