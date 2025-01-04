package com.iruanp.mcuniversaleconomy.config;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class YamlHandler {
    private final Yaml yaml;
    private Map<String, Object> data;

    public YamlHandler() {
        this.yaml = new Yaml();
    }

    public void load(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            this.data = yaml.load(inputStream);
        }
    }

    public void save(Path path, Map<String, Object> data) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            yaml.dump(data, writer);
        }
    }

    public String getString(String path, String defaultValue) {
        Object value = getNestedValue(path);
        return value != null ? value.toString() : defaultValue;
    }

    public int getInt(String path, int defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    public double getDouble(String path, double defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    private Object getNestedValue(String path) {
        if (data == null) return null;
        
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        
        for (int i = 0; i < parts.length - 1; i++) {
            Object value = current.get(parts[i]);
            if (!(value instanceof Map)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            current = map;
        }
        
        return current.get(parts[parts.length - 1]);
    }
} 