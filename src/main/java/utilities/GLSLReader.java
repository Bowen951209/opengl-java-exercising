package utilities;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GLSLReader {
    private List<String> data;
    GLSLReader(Path path) {

        try {
            data = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString() {
        return String.join("\n",  data);
    }
}
