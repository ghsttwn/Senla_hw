package config;

import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;
import T8.T1.annotations.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationManager {
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    private static final Map<String, Properties> propertiesCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> beanCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> singletonCache = new ConcurrentHashMap<>();

    public static void configure(Object target) {
        if (target == null) {
            return;
        }

        Class<?> clazz = target.getClass();
        System.out.println("=== CONFIGURING: " + clazz.getSimpleName() + " ===");

        List<Field> allFields = getAllFields(clazz);

        for (Field field : allFields) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                try {
                    configureField(target, field);
                } catch (Exception e) {
                    System.err.println("Error configuring field " + field.getName() +
                            " in class " + clazz.getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    public static void injectDependencies(Object target) {
        if (target == null) {
            return;
        }

        configure(target);
        injectThroughFields(target);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> clazz) {
        if (singletonCache.containsKey(clazz)) {
            return (T) singletonCache.get(clazz);
        }

        if (beanCache.containsKey(clazz)) {
            return (T) beanCache.get(clazz);
        }

        try {
            T instance;

            Constructor<?> injectConstructor = findInjectConstructor(clazz);

            if (injectConstructor != null) {
                Class<?>[] paramTypes = injectConstructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = getOrCreateBean(paramTypes[i]);
                }

                instance = (T) injectConstructor.newInstance(params);
            } else {
                instance = clazz.getDeclaredConstructor().newInstance();
            }

            injectThroughFields(instance);
            configure(instance);

            if (isSingleton(clazz)) {
                singletonCache.put(clazz, instance);
            } else {
                beanCache.put(clazz, instance);
            }

            System.out.println("Created instance of " + clazz.getName());
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private static void configureField(Object target, Field field) {
        ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);

        String configFileName = annotation.configFileName();
        if (configFileName.isEmpty()) {
            configFileName = DEFAULT_CONFIG_FILE;
        }

        String propertyName = annotation.propertyName();
        if (propertyName.isEmpty()) {
            propertyName = generateDefaultPropertyName(target.getClass(), field);
        }

        Properties properties = loadProperties(configFileName);
        String value = properties.getProperty(propertyName);

        if (value != null) {
            setFieldValue(target, field, value, annotation.type());
        } else {
            System.out.println("Warning: Property '" + propertyName + "' not found in " + configFileName);
            // Попробуем найти свойство в другом формате (нижний регистр с точками)
            String alternativeName = propertyName.toLowerCase().replace("_", ".");
            value = properties.getProperty(alternativeName);
            if (value != null) {
                setFieldValue(target, field, value, annotation.type());
            }
        }
    }

    private static void setFieldValue(Object target, Field field, String value, PropertyType propertyType) {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            Object convertedValue = convertValue(value, fieldType, propertyType);

            if (convertedValue != null) {
                field.set(target, convertedValue);

                // Правильное отображение значения
                String displayValue;
                if (convertedValue.getClass().isArray()) {
                    if (convertedValue instanceof String[]) {
                        displayValue = Arrays.toString((String[]) convertedValue);
                    } else if (convertedValue instanceof int[]) {
                        displayValue = Arrays.toString((int[]) convertedValue);
                    } else if (convertedValue instanceof double[]) {
                        displayValue = Arrays.toString((double[]) convertedValue);
                    } else if (convertedValue instanceof boolean[]) {
                        displayValue = Arrays.toString((boolean[]) convertedValue);
                    } else {
                        displayValue = "Array of " + convertedValue.getClass().getComponentType().getSimpleName();
                    }
                } else {
                    displayValue = convertedValue.toString();
                }

                System.out.println("Configured field: " + field.getName() + " = " + displayValue);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value", e);
        }
    }

    private static Object convertValue(String value, Class<?> targetType, PropertyType propertyType) {
        if (value == null || value.trim().isEmpty()) {
            return getDefaultValue(targetType);
        }

        value = value.trim();

        if (propertyType != PropertyType.AUTO) {
            return convertByPropertyType(value, propertyType, targetType);
        }

        return convertByFieldType(value, targetType);
    }

    private static Object convertByPropertyType(String value, PropertyType propertyType, Class<?> targetType) {
        try {
            switch (propertyType) {
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
                    return parseStringArray(value);

                case INTEGER_ARRAY:
                    return parseIntArray(value);

                case DOUBLE_ARRAY:
                    return parseDoubleArray(value);

                case BOOLEAN_ARRAY:
                    return parseBooleanArray(value);

                case LIST:
                    return parseList(value);

                case ARRAY:
                    if (!targetType.isArray()) {
                        throw new IllegalArgumentException("Target type must be an array: " + targetType);
                    }
                    return parseArray(value, targetType);

                default:
                    return convertByFieldType(value, targetType);
            }
        } catch (Exception e) {
            System.err.println("Error converting value '" + value + "' to type " +
                    propertyType + ": " + e.getMessage());
            return getDefaultValue(targetType);
        }
    }

    private static Object convertByFieldType(String value, Class<?> targetType) {
        try {
            if (targetType == String.class) {
                return value;
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(value);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(value);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return parseBoolean(value);
            } else if (targetType == List.class) {
                return parseList(value);
            } else if (targetType.isArray()) {
                return parseArray(value, targetType);
            } else if (targetType.isEnum()) {
                return convertToEnum(value, targetType);
            }

            try {
                return targetType.getConstructor(String.class).newInstance(value);
            } catch (Exception e) {
                return value;
            }
        } catch (Exception e) {
            System.err.println("Error auto-converting value '" + value +
                    "' to type " + targetType + ": " + e.getMessage());
            return getDefaultValue(targetType);
        }
    }

    private static String[] parseStringArray(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new String[0];
        }

        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split("\\s*,\\s*");
        return Arrays.stream(parts)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static int[] parseIntArray(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new int[0];
        }

        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split("\\s*,\\s*");
        return Arrays.stream(parts)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static double[] parseDoubleArray(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new double[0];
        }

        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split("\\s*,\\s*");
        return Arrays.stream(parts)
                .filter(s -> !s.isEmpty())
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    private static boolean[] parseBooleanArray(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new boolean[0];
        }

        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split("\\s*,\\s*");
        boolean[] result = new boolean[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parseBoolean(parts[i]);
        }
        return result;
    }

    private static boolean parseBoolean(String value) {
        if (value == null) return false;

        value = value.trim().toLowerCase();
        if (value.equals("true") || value.equals("1") ||
                value.equals("yes") || value.equals("да")) {
            return true;
        }
        if (value.equals("false") || value.equals("0") ||
                value.equals("no") || value.equals("нет")) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private static List<String> parseList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }

        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split("\\s*,\\s*");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private static Object parseArray(String value, Class<?> targetType) {
        if (!targetType.isArray()) {
            throw new IllegalArgumentException("Target type must be an array: " + targetType);
        }

        Class<?> componentType = targetType.getComponentType();
        if (value == null || value.trim().isEmpty()) {
            return Array.newInstance(componentType, 0);
        }

        value = value.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split("\\s*,\\s*");
        Object array = Array.newInstance(componentType, parts.length);

        for (int i = 0; i < parts.length; i++) {
            String element = parts[i].trim();
            if (!element.isEmpty()) {
                Array.set(array, i, convertArrayElement(element, componentType));
            }
        }

        return array;
    }

    private static Object convertArrayElement(String value, Class<?> componentType) {
        if (componentType == String.class) {
            return value;
        } else if (componentType == int.class || componentType == Integer.class) {
            return Integer.parseInt(value);
        } else if (componentType == long.class || componentType == Long.class) {
            return Long.parseLong(value);
        } else if (componentType == double.class || componentType == Double.class) {
            return Double.parseDouble(value);
        } else if (componentType == float.class || componentType == Float.class) {
            return Float.parseFloat(value);
        } else if (componentType == boolean.class || componentType == Boolean.class) {
            return parseBoolean(value);
        } else if (componentType.isEnum()) {
            return convertToEnum(value, componentType);
        } else {
            try {
                return componentType.getConstructor(String.class).newInstance(value);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported array element type: " + componentType);
            }
        }
    }

    private static Object convertToEnum(String value, Class<?> enumType) {
        try {
            @SuppressWarnings("unchecked")
            Class<Enum> enumClass = (Class<Enum>) enumType;

            // Сначала пробуем точное совпадение
            try {
                return Enum.valueOf(enumClass, value.toUpperCase());
            } catch (IllegalArgumentException e1) {
                // Затем пробуем без учета регистра
                for (Object enumConstant : enumType.getEnumConstants()) {
                    if (enumConstant.toString().equalsIgnoreCase(value)) {
                        return enumConstant;
                    }
                }
                // Если не нашли, возвращаем первое значение
                return enumType.getEnumConstants()[0];
            }
        } catch (Exception e) {
            System.err.println("Error converting to enum: " + value + " for type " + enumType.getSimpleName());
            return enumType.getEnumConstants()[0];
        }
    }

    private static Object getDefaultValue(Class<?> targetType) {
        if (targetType.isPrimitive()) {
            if (targetType == int.class) return 0;
            if (targetType == long.class) return 0L;
            if (targetType == double.class) return 0.0;
            if (targetType == float.class) return 0.0f;
            if (targetType == boolean.class) return false;
            if (targetType == char.class) return '\0';
            if (targetType == byte.class) return (byte) 0;
            if (targetType == short.class) return (short) 0;
        }

        if (targetType.isArray()) {
            return Array.newInstance(targetType.getComponentType(), 0);
        }

        return null;
    }

    private static Properties loadProperties(String fileName) {
        return propertiesCache.computeIfAbsent(fileName, key -> {
            Properties properties = new Properties();
            InputStream input = null;
            InputStreamReader reader = null;

            try {
                input = ConfigurationManager.class.getClassLoader()
                        .getResourceAsStream(fileName);

                if (input != null) {
                    System.out.println("Loading config file: " + fileName);

                    // Читаем поток в память для обработки BOM
                    byte[] bytes = input.readAllBytes();

                    // Проверяем наличие BOM (UTF-8 BOM = EF BB BF)
                    int offset = 0;
                    if (bytes.length >= 3 &&
                            (bytes[0] & 0xFF) == 0xEF &&
                            (bytes[1] & 0xFF) == 0xBB &&
                            (bytes[2] & 0xFF) == 0xBF) {
                        offset = 3; // Пропускаем BOM
                        System.out.println("  Found UTF-8 BOM, skipping...");
                    }

                    // Преобразуем байты в строку с UTF-8 кодировкой
                    String content = new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8);

                    // Загружаем свойства из строки
                    properties.load(new StringReader(content));

                    System.out.println("  Successfully loaded " + properties.size() + " properties");

                    // Отладочный вывод ключевых свойств
                    System.out.println("  Key properties:");
                    String[] importantProps = {
                            "hotel.name", "supported.room.types", "ui.date.format",
                            "room.default.type", "guest.default.name"
                    };
                    for (String prop : importantProps) {
                        String value = properties.getProperty(prop);
                        if (value != null) {
                            System.out.println("    " + prop + " = " + value);
                        }
                    }

                } else {
                    System.out.println("Config file not found: " + fileName + ", using default properties");
                    properties = createDefaultProperties();
                }

            } catch (IOException e) {
                System.out.println("Error loading config file: " + fileName + ": " + e.getMessage());
                properties = createDefaultProperties();
            } finally {
                // Закрываем ресурсы
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // Игнорируем ошибку закрытия
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        // Игнорируем ошибку закрытия
                    }
                }
            }

            return properties;
        });
    }

    private static Properties createDefaultProperties() {
        Properties props = new Properties();

        // Hotel Configuration
        props.setProperty("hotel.name", "Гранд Отель");
        props.setProperty("room.status.change.enabled", "true");
        props.setProperty("room.history.size", "3");
        props.setProperty("hotel.default.rooms", "10");
        props.setProperty("hotel.default.services", "5");
        props.setProperty("csv.delimiter", ",");
        props.setProperty("export.enabled", "true");
        props.setProperty("backup.path", "./backups/");
        props.setProperty("supported.room.types", "Стандарт,Люкс,Президентский");
        props.setProperty("max.guests.per.room", "4");
        props.setProperty("room.min.price", "1000.0");
        props.setProperty("room.max.price", "50000.0");
        props.setProperty("default.checkin.time", "14:00");
        props.setProperty("default.checkout.time", "12:00");

        // UI Configuration
        props.setProperty("ui.refresh.rate", "1000");
        props.setProperty("ui.date.format", "dd.MM.yyyy");
        props.setProperty("ui.show.warnings", "true");
        props.setProperty("ui.auto.save", "true");
        props.setProperty("ui.auto.save.interval", "300");
        props.setProperty("ui.max.input.attempts", "3");

        // Guest Configuration
        props.setProperty("guest.default.name", "Имя не указано");
        props.setProperty("guest.passport.format", "XXXXXXXXXX");
        props.setProperty("guest.phone.format", "+7-XXX-XXX-XXXX");
        props.setProperty("guest.email", "guest@example.com");
        props.setProperty("guest.auto.generate.id", "true");
        props.setProperty("guest.max.name.length", "100");
        props.setProperty("guest.passport.regex", "\\d{10}");
        props.setProperty("guest.phone.regex", "^\\+?[\\d\\s\\-\\(\\)]+$");
        props.setProperty("guest.email.regex", "^[A-Za-z0-9+_.-]+@(.+)$");
        props.setProperty("guest.min.age", "18");
        props.setProperty("guest.max.age", "120");
        props.setProperty("guest.default.nationality", "Россия");
        props.setProperty("guest.validation.enabled", "true");
        props.setProperty("guest.require.email", "false");
        props.setProperty("guest.require.birth.date", "false");

        // Room Configuration
        props.setProperty("room.default.price", "2500.0");
        props.setProperty("room.default.capacity", "2");
        props.setProperty("room.default.stars", "3");
        props.setProperty("room.auto.generate.id", "true");
        props.setProperty("room.default.type", "Стандарт");
        props.setProperty("room.max.additional.services", "10");
        props.setProperty("room.validation.enabled", "true");
        props.setProperty("room.max.history.size", "100");
        props.setProperty("room.min.number", "1");
        props.setProperty("room.max.number", "9999");
        props.setProperty("room.auto.clean.services.on.checkout", "true");

        return props;
    }

    private static String generateDefaultPropertyName(Class<?> clazz, Field field) {
        // Два формата: с точками и с подчеркиваниями
        String className = clazz.getSimpleName().toLowerCase();
        String fieldName = field.getName().toLowerCase();

        // Пробуем оба формата
        return className + "." + fieldName;
    }

    private static Constructor<?> findInjectConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }

        return null;
    }

    private static void injectThroughFields(Object target) {
        Class<?> clazz = target.getClass();
        List<Field> allFields = getAllFields(clazz);

        for (Field field : allFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                injectField(target, field);
            }
        }
    }

    private static void injectField(Object target, Field field) {
        try {
            field.setAccessible(true);

            if (field.get(target) != null) {
                return;
            }

            Class<?> fieldType = field.getType();
            Object dependency = getOrCreateBean(fieldType);

            if (dependency != null) {
                field.set(target, dependency);
                System.out.println("Injected dependency: " + fieldType.getSimpleName() +
                        " into " + target.getClass().getSimpleName());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject field", e);
        }
    }

    private static Object getOrCreateBean(Class<?> beanClass) {
        if (isPrimitiveOrWrapper(beanClass) || beanClass.isArray() || beanClass == List.class) {
            return null;
        }

        if (singletonCache.containsKey(beanClass)) {
            return singletonCache.get(beanClass);
        }

        if (beanCache.containsKey(beanClass)) {
            return beanCache.get(beanClass);
        }

        return createInstance(beanClass);
    }

    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class;
    }

    private static boolean isSingleton(Class<?> clazz) {
        // NavigationManager - синглтон
        return clazz.getName().contains("NavigationManager");
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static void clearCache() {
        propertiesCache.clear();
        beanCache.clear();
        singletonCache.clear();
        System.out.println("Configuration cache cleared");
    }

    public static void reloadConfiguration() {
        propertiesCache.clear();
        System.out.println("Configuration reloaded");
    }

    public static void printConfigurationReport() {
        System.out.println("\n=== CONFIGURATION REPORT ===");
        System.out.println("Singleton instances: " + singletonCache.size());
        System.out.println("Regular bean instances: " + beanCache.size());
        System.out.println("Cached property files: " + propertiesCache.size());

        System.out.println("\nLoaded properties:");
        for (Map.Entry<String, Properties> entry : propertiesCache.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().size() + " properties");
        }

        System.out.println("\n=== END OF REPORT ===");
    }

    public static String getProperty(String key) {
        Properties props = loadProperties(DEFAULT_CONFIG_FILE);
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        Properties props = loadProperties(DEFAULT_CONFIG_FILE);
        return props.getProperty(key, defaultValue);
    }
}