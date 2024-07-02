import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Represents a dependency with groupId, artifactId, and version.
class Dependency {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    // Validates that none of the fields are null or empty.
    public static boolean isValid(String groupId, String artifactId, String version) {
        return groupId != null && !groupId.isEmpty() &&
                artifactId != null && !artifactId.isEmpty() &&
                version != null && !version.isEmpty();
    }
}

// Interface for reading dependencies from a file.
interface DependencyReader {
    List<Dependency> readDependencies(String filePath) throws IOException;
}

// Custom exception for errors during dependency reading.
class DependencyReadException extends IOException {
    public DependencyReadException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Implementation of DependencyReader to read dependencies from a text file.
class TextFileDependencyReader implements DependencyReader {
    private static final Logger logger = Logger.getLogger(TextFileDependencyReader.class.getName());

    @Override
    public List<Dependency> readDependencies(String filePath) throws DependencyReadException {
        List<Dependency> dependencies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String groupId = null;
            String artifactId = null;
            String version = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                logger.info("Processing line: " + line);
                if (line.contains("groupId")) {
                    groupId = line.split("=")[1].trim().replace(";", "");
                    logger.info("Parsed groupId: " + groupId);
                } else if (line.contains("artifactId")) {
                    artifactId = line.split("=")[1].trim().replace(";", "");
                    logger.info("Parsed artifactId: " + artifactId);
                } else if (line.contains("version")) {
                    version = line.split("=")[1].trim().replace(";", "");
                    logger.info("Parsed version: " + version);
                    if (Dependency.isValid(groupId, artifactId, version)) {
                        dependencies.add(new Dependency(groupId, artifactId, version));
                        logger.info("Added dependency: " + groupId + ", " + artifactId + ", " + version);
                        groupId = null;
                        artifactId = null;
                        version = null;
                    } else {
                        logger.warning("Invalid dependency found and skipped: " + groupId + ", " + artifactId + ", " + version);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading dependencies from file", e);
            throw new DependencyReadException("Error reading dependencies from file", e);
        }
        return dependencies;
    }
}

// Interface for writing dependencies to a file.
interface DependencyWriter {
    void writeDependencies(List<Dependency> dependencies, String filePath) throws IOException;
}

// Implementation of DependencyWriter to write dependencies to an XML file.
class XMLDependencyWriter implements DependencyWriter {
    private static final Logger logger = Logger.getLogger(XMLDependencyWriter.class.getName());

    @Override
    public void writeDependencies(List<Dependency> dependencies, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("<dependencies>\n");
            for (Dependency dependency : dependencies) {
                writer.write("    <dependency>\n");
                writer.write("        <groupId>" + dependency.getGroupId() + "</groupId>\n");
                writer.write("        <artifactId>" + dependency.getArtifactId() + "</artifactId>\n");
                writer.write("        <version>" + dependency.getVersion() + "</version>\n");
                writer.write("    </dependency>\n");
            }
            writer.write("</dependencies>");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing dependencies to XML file", e);
            throw e;
        }
    }
}

// Main class to read dependencies from a text file and write them to an XML file.
public class GenerateXML {
    private static final Logger logger = Logger.getLogger(GenerateXML.class.getName());

    public static void main(String[] args) {
        String inputFilePath = "C:/Users/Tharanesh/Desktop/dependencies.txt";
        String outputFilePath = "C:/Users/Tharanesh/Desktop/dependencies.xml";

        DependencyReader reader = new TextFileDependencyReader();
        DependencyWriter writer = new XMLDependencyWriter();

        try {
            List<Dependency> dependencies = reader.readDependencies(inputFilePath);
            if (dependencies.isEmpty()) {
                logger.severe("No dependencies found in the input file.");
            } else {
                writer.writeDependencies(dependencies, outputFilePath);
                logger.info("Dependencies written to XML file successfully");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process dependencies", e);
        }
    }
}