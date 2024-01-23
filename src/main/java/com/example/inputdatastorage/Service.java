package com.example.inputdatastorage;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ResourceUtils;
import virtuoso.jena.driver.VirtModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
@Configuration
@EnableScheduling
public class Service {

    Service() throws Exception {
        fetchComponentNames();
        logger.info("Current Map: {}", this.componentName2PathToLogfile);
    }

    private Map<String, String> componentName2PathToLogfile = new HashMap<>();
    private String dirToQanaryComponents = "";
    private final Logger logger = LoggerFactory.getLogger(Service.class);
    private final String QANARY_DIRECTORY = "Qanary-question-answering-components/";
    private BufferedReader reader;
 //   @Value("${virtuoso.triplestore.endpoint}")
    private String VIRTUOSO_TRIPLESTORE_ENDPOINT;
  //  @Value("${virtuoso.triplestore.username}")
    private String VIRTUOSO_TRIPLESTORE_USERNAME;
  //  @Value("${virtuoso.triplestore.password}")
    private String VIRTUOSO_TRIPLESTORE_PASSWORD;
    private Model model = ModelFactory.createDefaultModel();

    public List<String> fetchDirectoryNames() throws FileNotFoundException {
        File folder = new File(QANARY_DIRECTORY);
        File[] files = folder.listFiles();
        List<String> files_ = Arrays.stream(files).filter(file -> file.isDirectory()).map(File::getName).toList();
        return files_.stream().filter(file -> file.startsWith("qanary")).map(file -> file.concat("/")).toList();
    }

    public void fetchDirectoryNames(String dirToQanaryComponents) throws FileNotFoundException {
        this.dirToQanaryComponents = dirToQanaryComponents;
        File folder = ResourceUtils.getFile(dirToQanaryComponents);
        File[] files = folder.listFiles();
        List<String> files_ = Arrays.stream(files).filter(file -> file.isDirectory()).map(File::getName).toList();
    }

    public Map<String, String> fetchComponentNames() throws Exception {
        List<String> componentDirectories = fetchDirectoryNames();
        componentDirectories.forEach(componentDirectory -> {
            String componentName = fetchComponentName(componentDirectory);
            if (componentName != null)
                this.componentName2PathToLogfile.put(componentName, QANARY_DIRECTORY + componentDirectory);
        });
        return componentName2PathToLogfile;
    }

    public String fetchComponentName(String componentDirectory) {
        String pathToResources = QANARY_DIRECTORY + componentDirectory + "src/main/resources";
        Path pathToApplicationPropertyFile = findApplicationPropertyFile(pathToResources);
        if (pathToApplicationPropertyFile == null)
            return null;
        try {
            reader = new BufferedReader(new FileReader(String.valueOf(pathToApplicationPropertyFile)));
            String line = reader.readLine();

            while (line != null) {
                if (line.contains("spring.application.name=")) {
                    String name = line.replace("spring.application.name=", "");
                    return name;
                } else
                    line = reader.readLine();
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public Path findApplicationPropertyFile(String pathToResources) {
        Optional<Path> path;
        try (Stream<Path> walkStream = Files.walk(Paths.get(pathToResources))) {
            path = walkStream.filter(p -> p.toFile().isFile()).filter(file -> file.endsWith("application.properties")).findFirst();
        } catch (Exception e) {
            return null;
        }
        return path.orElse(null);
    }

    @Scheduled(fixedRate = 5000)
    public void checkFiles() {
        logger.info("Scheduled job");
        this.componentName2PathToLogfile.forEach((key, value) -> {
            boolean isFileEmpty = checkFileContent(value);
            if (!isFileEmpty) {
                logger.info("File is not empty, path: {}", value);
                insertDataToTriplestore(value);
            }
        });
    }

    public boolean checkFileContent(String path) {
        try {
            reader = new BufferedReader(new FileReader(path + "log.txt"));
            String line = reader.readLine(); // read first line
            if(line == null)
                return true;
            else
                return line.isEmpty();
        } catch (IOException e) {

        }
        return true;
    }

    public void insertDataToTriplestore(String path) {
        List<String> queries = selectQueriesFromFile(path);
        queries.forEach(item -> insertDataToTriplestore(new QueriesPojo(item)));
    }

    public List<String> selectQueriesFromFile(String path) {
        List<String> queries = new ArrayList<>();
        String temp = "";
        try {
            reader = new BufferedReader(new FileReader(path + "log.txt"));
            String line = reader.readLine(); // read first line
            while (line != null) {
                logger.info(line);
                if (line.startsWith("-------------------------------------")) {
                    queries.add(temp);
                    temp = "";
                } else {
                    temp = temp.concat(line + "\n");
                }
                line = reader.readLine();
            }
            deleteFileContent(path);
            return queries;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFileContent(String path) {
        try {
            FileWriter fileWriter = new FileWriter(path + "log.txt");
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveQueriesToTriplestore() {
        String query = "";
        String graph = "";
    }


    public void insertDataToTriplestore(QueriesPojo queryPojo) {
        List<Statement> statements = createStatementsFromQueryPojo(queryPojo); // resolve
        model.add(statements);
        VirtModel virtModel = VirtModel.openDatabaseModel("urn:qanary:" + "GRAPH_ID", VIRTUOSO_TRIPLESTORE_ENDPOINT, VIRTUOSO_TRIPLESTORE_USERNAME, VIRTUOSO_TRIPLESTORE_PASSWORD);
        virtModel.add(model);
        virtModel.close();
        model.remove(statements);
    }

    public List<Statement> createStatementsFromQueryPojo(QueriesPojo queries) {
        return null;
    }


}
