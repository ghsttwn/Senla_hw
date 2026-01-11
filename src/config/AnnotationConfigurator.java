package config;

import T8.T1.annotations.PropertyType;
import T8.T1.annotations.ConfigProperty;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationConfigurator {

    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    public <T> T configure(T configObject) throws ConfigurationException {
        return configure(configObject, DEFAULT_CONFIG_FILE);
    }

    public <T> T configure(T configObject, String defaultConfigFile) throws ConfigurationException {
        Class<?> configClass = configObject.getClass();
        System.out.println("=== КОНФИГУРАЦИЯ КЛАССА: " + configClass.getSimpleName() + " ===");

        boolean hasChanges = false;

        for (Field field : getAllFields(configClass)) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                try {
                    boolean changed = configureField(configObject, field, defaultConfigFile);
                    hasChanges = hasChanges || changed;
                } catch (Exception e) {
                    throw new ConfigurationException(
                            "Ошибка конфигурации поля '" + field.getName() + "' в классе '" +
                                    configClass.getSimpleName() + "': " + e.getMessage(), e);
                }
            }
        }

        if (hasChanges) {
            System.out.println("Класс " + configClass.getSimpleName() + " полностью сконфигурирован.");
        } else {
            System.out.println("Класс " + configClass.getSimpleName() + " не изменен.");
        }

        return configObject;
    }

    private boolean configureField(Object configObject, Field field, String defaultConfigFile)
            throws IllegalAccessException, IOException {
        ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
        String configFileName = annotation.configFileName().isEmpty() ?
                defaultConfigFile : annotation.configFileName();

        String propertyName = annotation.propertyName().isEmpty() ?
                generateDefaultPropertyName(configObject.getClass(), field.getName()) :
                annotation.propertyName();

        Properties properties = loadProperties(configFileName);
        String propertyValue = properties.getProperty(propertyName);

        if (propertyValue == null) {
            System.out.println("Свойство '" + propertyName + "' не найдено в файле '" +
                    configFileName + "'. Используется значение по умолчанию.");
            return false;
        }

        Object convertedValue = convertValue(propertyValue, field, annotation.type());
        field.setAccessible(true);
        Object currentValue = field.get(configObject);

        if (!Objects.equals(currentValue, convertedValue)) {
            field.set(configObject, convertedValue);
            System.out.println("Поле '" + field.getName() + "' изменено: '" +
                    currentValue + "' -> '" + convertedValue + "'");
            return true;
        }

        return false;
    }

    private Object convertValue(String value, Field field, PropertyType annotationType) {
        Class<?> fieldType = field.getType();
        PropertyType targetType = annotationType == PropertyType.AUTO ?
                determinePropertyType(field) : annotationType;

        try {
            return convertValueByType(value.trim(), targetType, field);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Не удалось преобразовать значение '" + value + "' в тип " +
                            targetType + " для поля " + field.getName() + ": " + e.getMessage());
        }
    }

    private Object convertValueByType(String value, PropertyType type, Field field) {
        if (value.isEmpty()) {
            return getDefaultValue(field.getType());
        }

        switch (type) {
            case STRING:
                return value;

            case INTEGER:
                return Integer.parseInt(value);

            case LONG:
                return Long.parseLong(value);

            case DOUBLE:
                return Double.parseDouble(value);

            case BOOLEAN:
                return parseBoolean(value);

            case STRING_ARRAY:
                return parseArray(value, String.class);

            case INTEGER_ARRAY:
                return parseArray(value, Integer.class);

            case DOUBLE_ARRAY:
                return parseArray(value, Double.class);

            case BOOLEAN_ARRAY:
                return parseArray(value, Boolean.class);

            default:
                throw new IllegalArgumentException("Неподдерживаемый тип: " + type);
        }
    }

    private PropertyType determinePropertyType(Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType == String.class) return PropertyType.STRING;
        if (fieldType == int.class || fieldType == Integer.class) return PropertyType.INTEGER;
        if (fieldType == long.class || fieldType == Long.class) return PropertyType.LONG;
        if (fieldType == double.class || fieldType == Double.class) return PropertyType.DOUBLE;
        if (fieldType == boolean.class || fieldType == Boolean.class) return PropertyType.BOOLEAN;

        if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();
            if (componentType == String.class) return PropertyType.STRING_ARRAY;
            if (componentType == int.class) return PropertyType.INTEGER_ARRAY;
            if (componentType == double.class) return PropertyType.DOUBLE_ARRAY;
            if (componentType == boolean.class) return PropertyType.BOOLEAN_ARRAY;
        }

        if (Collection.class.isAssignableFrom(fieldType)) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Class<?> elementType = (Class<?>) genericType.getActualTypeArguments()[0];
            if (elementType == String.class) return PropertyType.STRING_ARRAY;
            if (elementType == Integer.class) return PropertyType.INTEGER_ARRAY;
            if (elementType == Double.class) return PropertyType.DOUBLE_ARRAY;
            if (elementType == Boolean.class) return PropertyType.BOOLEAN_ARRAY;
        }

        return PropertyType.STRING;
    }

    private Object parseArray(String value, Class<?> elementType) {
        String[] parts = value.split("\\s*,\\s*");

        if (elementType == String.class) {
            return parts;
        } else if (elementType == Integer.class) {
            return Arrays.stream(parts)
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
        } else if (elementType == Double.class) {
            return Arrays.stream(parts)
                    .map(Double::parseDouble)
                    .toArray(Double[]::new);
        } else if (elementType == Boolean.class) {
            return Arrays.stream(parts)
                    .map(this::parseBoolean)
                    .toArray(Boolean[]::new);
        }

        throw new IllegalArgumentException("Неподдерживаемый тип элемента массива: " + elementType);
    }

    private boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true") || value.equals("1")) return true;
        if (value.equalsIgnoreCase("false") || value.equals("0")) return false;
        throw new IllegalArgumentException("Недопустимое булево значение: " + value);
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == String.class) return "";
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == boolean.class) return false;
        if (type.isArray()) return Array.newInstance(type.getComponentType(), 0);
        return null;
    }

    private String generateDefaultPropertyName(Class<?> clazz, String fieldName) {
        return clazz.getSimpleName().toUpperCase() + "." + fieldName.toUpperCase();
    }

    private Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.out.println("Конфигурационный файл '" + fileName + "' не найден.");
            }
        }
        return properties;
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