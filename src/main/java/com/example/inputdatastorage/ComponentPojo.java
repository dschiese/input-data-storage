package com.example.inputdatastorage;

import java.nio.file.Path;

public class ComponentPojo {

    public ComponentPojo(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    private String name;
    private Path path;

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
