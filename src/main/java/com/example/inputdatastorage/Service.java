package com.example.inputdatastorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
public class Service {

    Service() {

    }

    private Map<String,String> componentName2PathToLogfile;
    private String dirToQanaryComponents = "";
    private final Logger logger = LoggerFactory.getLogger(Service.class);
    private final String QANARY_DIRECTORY = "Qanary-question-answering-components/";

    public List<String> fetchDirectoryNames() throws FileNotFoundException {
        File folder = new File(QANARY_DIRECTORY);
        File[] files = folder.listFiles();
        List<String> files_ =  Arrays.stream(files).filter(file -> file.isDirectory()).map(File::getName).toList();
        return files_.stream().filter(file -> file.startsWith("qanary")).map(file -> file.concat("/")).toList();
    }

    public void fetchDirectoryNames(String dirToQanaryComponents) throws FileNotFoundException {
        this.dirToQanaryComponents = dirToQanaryComponents;
        File folder = ResourceUtils.getFile(dirToQanaryComponents);
        File[] files = folder.listFiles();
        List<String> files_ = Arrays.stream(files).filter(file -> file.isDirectory()).map(File::getName).toList();
    }

    public void fetchComponentNames() throws Exception {
        List<String> componentDirectories = fetchDirectoryNames();
        componentDirectories.forEach(componentDirectory -> {
            logger.info("Component dir: {}", componentDirectory);
            String componentName = fetchComponentName(componentDirectory);
            if(componentName != null)
                this.componentName2PathToLogfile.put(componentName, componentDirectory);
        });
    }

    public String fetchComponentName(String componentDirectory) {
        String pathToResources = QANARY_DIRECTORY + componentDirectory + "src/main/resources";
        Path pathToApplicationPropertyFile = findApplicationPropertyFile(pathToResources);
        if(pathToApplicationPropertyFile == null)
            return null;
        logger.info("Full file path: {}", pathToApplicationPropertyFile);
        try {
            String fileContent = new String(Files.readAllBytes(pathToApplicationPropertyFile));
            logger.info("File content: {}", fileContent);
        } catch(IOException e) {
            return null;
        }
        return  null;
    }

    public Path findApplicationPropertyFile(String pathToResources) {
        Optional<Path> path;
        try (Stream<Path> walkStream = Files.walk(Paths.get(pathToResources))) {
            path = walkStream.filter(p -> p.toFile().isFile()).filter(file -> file.endsWith("application.properties")).findFirst();
        }
        catch(Exception e) {
            logger.error("Error while searching app props");
            return null;
        }
        return path.orElse(null);
    }





}
