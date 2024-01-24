package com.example.inputdatastorage;

import org.apache.jena.rdf.model.*;
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

    public Service() throws Exception {
        fetchComponentNames();
        model.setNsPrefix("ex", EXAMPLE_NAMESPACE);
        this.hasInputQuery = ResourceFactory.createProperty(EXAMPLE_NAMESPACE, "hasInputQuery");
        this.usedComponent = ResourceFactory.createProperty(EXAMPLE_NAMESPACE, "usedComponent");
        this.hasAnnotationType = ResourceFactory.createProperty(EXAMPLE_NAMESPACE, "hasAnnotationType");
    }

    private Map<String, String> componentName2PathToLogfile = new HashMap<>();
    private String dirToQanaryComponents = "";
    private final Logger logger = LoggerFactory.getLogger(Service.class);
    private final String QANARY_DIRECTORY = "Qanary-question-answering-components/";

    // PROPERTIES
    private Property usedComponent;
    private Property hasInputQuery;
    private Property hasAnnotationType;
    // NAMESPACES
    private final String EXAMPLE_NAMESPACE = "http://example#";


    private BufferedReader reader;
    @Value("${virtuoso.triplestore.endpoint}")
    private String VIRTUOSO_TRIPLESTORE_ENDPOINT;
    @Value("${virtuoso.triplestore.username}")
    private String VIRTUOSO_TRIPLESTORE_USERNAME;
    @Value("${virtuoso.triplestore.password}")
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
                insertDataToTriplestore(value, key);
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

    public void insertDataToTriplestore(String path, String component) {
        List<String> queries = selectQueriesFromFile(path);
        queries.forEach(item -> {
            try {
            insertDataToTriplestore(new QueriesPojo(item), component);
        } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });
    }

    public List<String> selectQueriesFromFile(String path) {
        List<String> queries = new ArrayList<>();
        String temp = "";
        try {
            reader = new BufferedReader(new FileReader(path + "log.txt"));
            String line = reader.readLine(); // read first line
            while (line != null) {
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

    public void insertDataToTriplestore(QueriesPojo queryPojo, String component) {
        List<Statement> statements = createStatementsFromQueryPojo(queryPojo, component); // resolve
        model.add(statements);
        VirtModel virtModel = VirtModel.openDatabaseModel(queryPojo.getGraphId(), VIRTUOSO_TRIPLESTORE_ENDPOINT, VIRTUOSO_TRIPLESTORE_USERNAME, VIRTUOSO_TRIPLESTORE_PASSWORD);
        virtModel.add(model);
        virtModel.close();
        model.remove(statements);
    }

    /* DEPRECATED
    public List<Statement> createStatementsFromQueryPojo(QueriesPojo queries, String component) {
        List<Statement> statements = new ArrayList<>();
        Resource graphId = ResourceFactory.createResource(queries.getGraphId());
        Resource annotationType = ResourceFactory.createResource("qa:" + queries.getAnnotationType());
        statements.add(ResourceFactory.createStatement(
                graphId,
                this.usedComponent,
                ResourceFactory.createResource("urn:qanary:" + component)
        ));
        statements.add(ResourceFactory.createStatement(
                graphId,
                this.hasInputQuery,
                ResourceFactory.createStringLiteral(queries.getQuery())
        ));
        statements.add(ResourceFactory.createStatement(
           graphId,
           this.hasAnnotationType,
           annotationType
        ));
        logger.debug("Statements: {}", statements);
        return statements;
    }
     */

    public List<Statement> createStatementsFromQueryPojo(QueriesPojo queriesPojo, String component) {
        List<Statement> statements = new ArrayList<>();
        Resource componentResource = ResourceFactory.createResource("urn:qanary:" + component);
        Literal query = ResourceFactory.createStringLiteral(queriesPojo.getQuery());
        Resource annType = ResourceFactory.createResource("qa:" + queriesPojo.getAnnotationType());
        statements.add(ResourceFactory.createStatement(
                componentResource,
                this.hasInputQuery,
                query
        ));
        statements.add(ResourceFactory.createStatement(
                componentResource,
                this.hasAnnotationType,
                annType
        ));
        logger.debug("Statements: {}", statements);
        return statements;
    }


}
