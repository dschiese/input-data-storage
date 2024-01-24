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

    public QueriesPojo(String item) throws IOException {
        this.initObject(item);
    }

    protected void initObject(String item) throws IOException {
        this.query = item;
        this.graphId = getGraphFromItem(item);
    }

    protected String getGraphFromItem(String item) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(item));
        String line = reader.readLine();
        while(line != null && !line.startsWith("FROM <")) {
            line = reader.readLine();
        }
        if(line != null && line.startsWith("FROM <"))
            return line.replace("FROM <", "").replace("> {", "");
        throw new RuntimeException("Couldn't find a FROM statement in the passed query");
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
