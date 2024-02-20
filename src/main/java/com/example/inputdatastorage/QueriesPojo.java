package com.example.inputdatastorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class QueriesPojo {

    private String graphId;
    private String query;
    private String component;
    private String annotationType;
    private final Logger logger = LoggerFactory.getLogger(QueriesPojo.class);
    private List<String> allowedAnnotationTypes = new ArrayList<>() {{
        add("AnnotationOfInstance");
        add("AnnotationOfSpotInstance");
        add("AnnotationOfRelation");
        add("AnnotationOfAnswerSPARQL");
        add("AnnotationOfAnswerJSON");
        add("AnnotationOfQuestionTranslation");
        add("AnnotationOfQuestionLanguage");
        add("AnnotationOfClass");
        add("AnnotationOfTextRepresentation");
    }};

    public QueriesPojo(String item) throws IOException {
        this.initObject(item);
    }

    protected void initObject(String item) throws IOException {
        this.query = item;
        setUpProperties(item);
    }

    protected void setUpProperties(String item) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(item));
        String line = reader.readLine();
        while(line != null) {
            getAnnotationTypeFromItem(line);
            getGraphIdFromItem(line);
            line = reader.readLine();
        }
        if(this.graphId == null || this.annotationType == null)
            throw new RuntimeException("Error while creating object from pass query with graphID = " + graphId + " and annotationType = " + annotationType);
        else
            logger.info("Found graphId: {} and annotationType: {}", this.graphId, this.annotationType);
    }

    protected void getGraphIdFromItem(String line) {
        if(line.startsWith("FROM <")) {
            this.graphId = line.replace("FROM <", "").replace(">", "");
            this.graphId.trim();
            this.graphId = this.graphId.replace("}","");
        }
    }

    protected void getAnnotationTypeFromItem(String line) {
        if(line.contains("AnnotationOf")) {
            String modifiedLine = line.substring(line.indexOf("qa:AnnotationOf")); // Current String equals "AnnotationOfSOMEWHAT ...." -> Need to remove the last part
            String annotationType = modifiedLine.replace(modifiedLine.substring(modifiedLine.indexOf(" "), modifiedLine.length()), "").replace("qa:","");
            logger.info("Found annotationType: {}", annotationType);
            if(this.allowedAnnotationTypes.contains(annotationType)) // Pretend it gets overwritten by any other, different annotation type // solely for our current (!) purpose
                this.annotationType = annotationType;
        }
    }

    public String getGraphId() {
        return graphId;
    }

    public String getQuery() {
        return query;
    }

    public String getComponent() {
        return component;
    }

    public String getAnnotationType() {
        return annotationType;
    }
}
