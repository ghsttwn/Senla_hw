package config;

import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class ConfiguredObjectFactory {
    private final AnnotationConfigurator configurator;

    public ConfiguredObjectFactory() {
        this.configurator = new AnnotationConfigurator();
    }

    public <T> T createConfiguredInstance(Class<T> clazz) throws ConfigurationException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            configurator.configure(instance);
            return instance;
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Не удалось создать сконфигурированный экземпляр класса " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public <T> T createConfiguredInstance(Class<T> clazz, String configFile) throws ConfigurationException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            configurator.configure(instance, configFile);
            return instance;
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Не удалось создать сконфигурированный экземпляр класса " + clazz.getSimpleName() +
                            " с файлом конфигурации '" + configFile + "': " + e.getMessage(), e);
        }
    }

    public List<String> getConfigurationReport(Class<?> clazz) {
        List<String> report = new ArrayList<>();
        report.add("=== ОТЧЕТ КОНФИГУРАЦИИ ДЛЯ КЛАССА: " + clazz.getSimpleName() + " ===");

        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
                report.add("Поле: " + field.getName());
                report.add("  Тип поля: " + field.getType().getSimpleName());
                report.add("  Файл конфигурации: " +
                        (annotation.configFileName().isEmpty() ? "config.properties" : annotation.configFileName()));
                report.add("  Имя свойства: " +
                        (annotation.propertyName().isEmpty() ?
                                clazz.getSimpleName().toUpperCase() + "." + field.getName().toUpperCase() :
                                annotation.propertyName()));
                report.add("  Тип конвертации: " + annotation.type());
                report.add("");
            }
        }

        if (report.size() == 1) {
            report.add("  В классе нет аннотированных полей @ConfigProperty");
        }

        return report;
    }

    public Map<String, Object> getDefaultConfiguration(Class<?> clazz) {
        Map<String, Object> defaults = new HashMap<>();

        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
                String propertyName = annotation.propertyName().isEmpty() ?
                        clazz.getSimpleName().toUpperCase() + "." + field.getName().toUpperCase() :
                        annotation.propertyName();

                Object defaultValue = getDefaultValueForType(field.getType());
                defaults.put(propertyName, defaultValue);
            }
        }

        return defaults;
    }

    private Object getDefaultValueForType(Class<?> type) {
        if (type == String.class) return "";
        if (type == int.class || type == Integer.class) return 0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == double.class || type == Double.class) return 0.0;
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == String[].class) return new String[0];
        if (type == int[].class) return new int[0];
        if (type == double[].class) return new double[0];
        if (type == boolean[].class) return new boolean[0];
        if (List.class.isAssignableFrom(type)) return new ArrayList<>();
        return null;
    }

    public void generateConfigurationTemplate(Class<?> clazz, String fileName) throws ConfigurationException {
        Map<String, Object> defaults = getDefaultConfiguration(clazz);

        StringBuilder template = new StringBuilder();
        template.append("# Конфигурационный файл для ").append(clazz.getSimpleName()).append("\n");
        template.append("# Сгенерировано: ").append(new Date()).append("\n\n");

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            template.append("# ").append(entry.getKey()).append("\n");
            template.append(entry.getKey()).append("=");

            Object value = entry.getValue();
            if (value instanceof String[]) {
                template.append("значение1, значение2, значение3");
            } else if (value instanceof int[] || value instanceof Integer[]) {
                template.append("100, 200, 300");
            } else if (value instanceof double[] || value instanceof Double[]) {
                template.append("10.5, 20.5, 30.5");
            } else if (value instanceof boolean[] || value instanceof Boolean[]) {
                template.append("true, false, true");
            } else if (value instanceof List) {
                template.append("элемент1, элемент2, элемент3");
            } else {
                template.append(value);
            }

            template.append("\n\n");
        }

        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(fileName),
                    template.toString().getBytes()
            );
            System.out.println("Шаблон конфигурационного файла создан: " + fileName);
        } catch (IOException e) {
            throw new ConfigurationException(
                    "Не удалось создать шаблон конфигурационного файла '" + fileName + "': " + e.getMessage(), e);
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}