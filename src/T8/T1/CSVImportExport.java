package T8.T1;

import T8.T1.exceptions.ImportExportException;
import T8.T1.model.Identifiable;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import T8.T1.exceptions.ImportExportException;
import T8.T1.model.Identifiable;
import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;
import T8.T1.model.RoomStatus;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CSVImportExport<T extends Identifiable> {
    private final Class<T> clazz;

    @ConfigProperty(propertyName = "csv.delimiter", type = PropertyType.STRING)
    private String delimiter;

    @ConfigProperty(propertyName = "csv.encoding", type = PropertyType.STRING)
    private String encoding;

    @ConfigProperty(propertyName = "csv.include.headers", type = PropertyType.BOOLEAN)
    private boolean includeHeaders;

    @ConfigProperty(propertyName = "csv.date.format", type = PropertyType.STRING)
    private String dateFormat;

    @ConfigProperty(propertyName = "csv.max.line.length", type = PropertyType.INTEGER)
    private int maxLineLength;

    @ConfigProperty(propertyName = "csv.auto.trim", type = PropertyType.BOOLEAN)
    private boolean autoTrim;

    public CSVImportExport(Class<T> clazz) {
        this.clazz = clazz;
        // Конфигурация загрузится через аннотации
        this.delimiter = ",";
        this.encoding = "UTF-8";
        this.includeHeaders = true;
        this.dateFormat = "yyyy-MM-dd";
        this.maxLineLength = 10000;
        this.autoTrim = true;
    }

    public CSVImportExport(Class<T> clazz, String delimiter) {
        this(clazz);
        this.delimiter = delimiter;
    }

    public void exportToFile(List<T> data, String filename) throws ImportExportException {
        if (data == null) {
            throw new ImportExportException("Данные для экспорта не могут быть null");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), encoding))) {

            if (includeHeaders) {
                List<String> headers = getFieldNames();
                writer.println(String.join(delimiter, headers));
            }

            for (T item : data) {
                List<String> values = getFieldValues(item);
                String line = String.join(delimiter, values);
                if (line.length() > maxLineLength) {
                    throw new ImportExportException("Строка превышает максимальную длину: " + maxLineLength);
                }
                writer.println(line);
            }

            System.out.println("Экспортировано " + data.size() + " записей в файл: " + filename);
        } catch (IOException e) {
            throw new ImportExportException("экспорте в файл", filename, e);
        } catch (Exception e) {
            throw new ImportExportException("экспорте данных", filename, e);
        }
    }

    public List<T> importFromFile(String filename) throws ImportExportException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        List<T> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), encoding))) {

            String line = reader.readLine();
            if (line == null) {
                throw new ImportExportException("Файл '" + filename + "' пуст");
            }

            boolean hasHeaders = includeHeaders;
            List<String> headers;

            if (hasHeaders) {
                headers = Arrays.asList(line.split(delimiter, -1));
                if (autoTrim) {
                    headers = headers.stream().map(String::trim).collect(Collectors.toList());
                }
            } else {
                // Если заголовков нет, используем имена полей класса
                headers = getFieldNames();
                // Первая строка - данные
                result.add(createItemFromLine(line, headers));
            }

            Map<String, Field> fieldMap = getFieldMap();

            int lineNumber = hasHeaders ? 1 : 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                if (line.length() > maxLineLength) {
                    throw new ImportExportException("Строка " + lineNumber +
                            " превышает максимальную длину: " + maxLineLength);
                }

                try {
                    T item = createItemFromLine(line, headers);
                    result.add(item);
                } catch (Exception e) {
                    throw new ImportExportException("Ошибка обработки строки " + lineNumber +
                            " в файле '" + filename + "': " + e.getMessage());
                }
            }

            System.out.println("Импортировано " + result.size() + " записей из файла: " + filename);
        } catch (FileNotFoundException e) {
            throw new ImportExportException("Файл '" + filename + "' не найден");
        } catch (IOException e) {
            throw new ImportExportException("чтении файла", filename, e);
        } catch (Exception e) {
            throw new ImportExportException("импорте из файла", filename, e);
        }

        return result;
    }

    private List<String> getFieldNames() {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private List<String> getFieldValues(T item) {
        List<String> values = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(item);
                String stringValue = convertToString(value);
                values.add(stringValue != null ? stringValue : "");
            } catch (IllegalAccessException e) {
                values.add("");
            }
        }
        return values;
    }

    private String convertToString(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof LocalDate) {
            LocalDate date = (LocalDate) value;
            return date.toString(); // Можно использовать dateFormat
        }

        if (value instanceof RoomStatus) {
            return ((RoomStatus) value).getDescription();
        }

        return value.toString();
    }

    private Map<String, Field> getFieldMap() {
        return Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, field -> {
                    field.setAccessible(true);
                    return field;
                }));
    }

    private T createItemFromLine(String line, List<String> headers) {
        try {
            String[] values = line.split(delimiter, -1);
            if (autoTrim) {
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
            }

            if (values.length != headers.size()) {
                throw new RuntimeException("Несоответствие количества колонок. Ожидалось: " +
                        headers.size() + ", получено: " + values.length);
            }

            T item = clazz.getDeclaredConstructor().newInstance();
            Map<String, Field> fieldMap = getFieldMap();

            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                String value = values[i];
                Field field = fieldMap.get(header);

                if (field != null && !value.isEmpty()) {
                    setFieldValue(item, field, value);
                }
            }
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания объекта из CSV: " + e.getMessage(), e);
        }
    }

    private void setFieldValue(T item, Field field, String value) {
        try {
            Class<?> type = field.getType();

            if (type == String.class) {
                field.set(item, value);
            } else if (type == int.class || type == Integer.class) {
                field.set(item, Integer.parseInt(value));
            } else if (type == long.class || type == Long.class) {
                field.set(item, Long.parseLong(value));
            } else if (type == double.class || type == Double.class) {
                field.set(item, Double.parseDouble(value));
            } else if (type == boolean.class || type == Boolean.class) {
                field.set(item, Boolean.parseBoolean(value));
            } else if (type == LocalDate.class) {
                field.set(item, LocalDate.parse(value));
            } else if (type == RoomStatus.class) {
                // Попробуем найти по описанию
                for (RoomStatus status : RoomStatus.values()) {
                    if (status.getDescription().equals(value)) {
                        field.set(item, status);
                        return;
                    }
                }
                // Если не нашли по описанию, попробуем по имени enum
                field.set(item, RoomStatus.valueOf(value.toUpperCase()));
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Неверный числовой формат для поля " + field.getName() + ": " + value, e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка установки значения поля " + field.getName() + ": " + value, e);
        }
    }

    // Геттеры и сеттеры для конфигурационных параметров
    public String getDelimiter() { return delimiter; }
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }

    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }

    public boolean isIncludeHeaders() { return includeHeaders; }
    public void setIncludeHeaders(boolean includeHeaders) { this.includeHeaders = includeHeaders; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public int getMaxLineLength() { return maxLineLength; }
    public void setMaxLineLength(int maxLineLength) { this.maxLineLength = maxLineLength; }

    public boolean isAutoTrim() { return autoTrim; }
    public void setAutoTrim(boolean autoTrim) { this.autoTrim = autoTrim; }

    public Class<T> getClazz() { return clazz; }
}