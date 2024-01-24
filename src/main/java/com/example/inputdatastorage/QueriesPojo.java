package com.example.inputdatastorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class QueriesPojo {

    private String graphId;
    private String query;
    private String component;
    private final Logger logger = LoggerFactory.getLogger(QueriesPojo.class);

    public QueriesPojo(String item) {
        this.initObject(item);
    }

    protected void initObject(String item) {
        this.query = item;
        try {
            this.graphId = getGraphFromItem(item);
        } catch(Exception e) {
            logger.error("Error while creating QueryPojo object with stack-trace: {}", e);
        }
    }

    protected String getGraphFromItem(String item) throws IOException {
        logger.info("ITEM: {}", item);
        BufferedReader reader = new BufferedReader(new StringReader(item));
        String line = reader.readLine();
        while(!line.startsWith("GRAPH <")) {
            line = reader.readLine();
        }
        if(line.startsWith("GRAPH <"))
            return line.replace("GRAPH <", "").replace("> {", "");
        return null;
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
}
