package net.bowen.engine.readers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextReader {
    public static String readFile(String path) {
        try {
        List<String> lines;
            lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
            return String.join("\n", lines);
        } catch (IOException e) {
            throw new RuntimeException("File \"" + path + "\" not found", e);
        }
    }
}
