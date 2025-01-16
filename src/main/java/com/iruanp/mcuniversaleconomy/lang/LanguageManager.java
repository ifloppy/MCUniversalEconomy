package com.iruanp.mcuniversaleconomy.lang;

import com.iruanp.mcuniversaleconomy.config.YamlHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, String> fallbackMessages = new HashMap<>();
    private final File dataFolder;
    private final String defaultLanguage;
    private static final String FALLBACK_LANGUAGE = "en_US";

    public LanguageManager(File dataFolder, String defaultLanguage) {
        this.dataFolder = dataFolder;
        this.defaultLanguage = defaultLanguage;
        loadLanguage();
    }

    private void loadLanguage() {
        File langFolder = new File(dataFolder, "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Load fallback language (en_US) first
        loadLanguageFile(FALLBACK_LANGUAGE, fallbackMessages);

        // Then load the configured language if different from fallback
        if (!defaultLanguage.equals(FALLBACK_LANGUAGE)) {
            loadLanguageFile(defaultLanguage, messages);
        } else {
            messages.putAll(fallbackMessages);
        }
    }

    private void loadLanguageFile(String language, Map<String, String> targetMap) {
        File langFile = new File(dataFolder, "lang/" + language + ".yml");
        if (!langFile.exists()) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("lang/" + language + ".yml")) {
                if (is != null) {
                    Files.copy(is, langFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            YamlHandler yamlHandler = new YamlHandler();
            yamlHandler.load(langFile.toPath());
            Yaml yaml = new Yaml();
            try (InputStream is = Files.newInputStream(langFile.toPath())) {
                Map<String, Object> data = yaml.load(is);
                if (data != null && data.containsKey("messages")) {
                    Object messagesObj = data.get("messages");
                    if (messagesObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> messagesMap = (Map<String, Object>) messagesObj;
                        flattenMessages("", messagesMap, targetMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void flattenMessages(String prefix, Map<String, Object> map, Map<String, String> targetMap) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();
                flattenMessages(key, nestedMap, targetMap);
            } else {
                targetMap.put(key, entry.getValue().toString());
            }
        }
    }

    public String getMessage(String key, Object... args) {
        String message = messages.getOrDefault(key, fallbackMessages.getOrDefault(key, key));
        return String.format(message, args);
    }

    public void reload() {
        messages.clear();
        fallbackMessages.clear();
        loadLanguage();
    }
} 