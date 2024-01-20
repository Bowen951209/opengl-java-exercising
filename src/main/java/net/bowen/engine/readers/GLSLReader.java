package net.bowen.engine.readers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GLSLReader {
    private final List<String> data;
    public GLSLReader(Path path) {

        try {
            data = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("File \"" + path.getFileName() + "\" not found", e);
        }
    }

    public String getString() {
        return String.join("\n",  data);
    }
}
