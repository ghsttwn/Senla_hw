package Task_6.T2;

import Task_6.T2.model.Identifiable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import Task_6.T2.exceptions.ImportExportException;
import java.time.LocalDate;
import java.util.*;


public class CSVImportExport<T extends Identifiable> {
    private final Class<T> clazz;
    private final String delimiter;

    public CSVImportExport(Class<T> clazz) {
        this(clazz, ",");
    }

    public CSVImportExport(Class<T> clazz, String delimiter) {
        this.clazz = clazz;
        this.delimiter = delimiter;
    }

    public void exportToFile(List<T> data, String filename) throws ImportExportException {
        if (data == null) {
            throw new ImportExportException("Данные для экспорта не могут быть null");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<String> headers = getFieldNames();
            writer.println(String.join(delimiter, headers));

            for (T item : data) {
                List<String> values = getFieldValues(item);
                writer.println(String.join(delimiter, values));
            }
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
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line == null) {
                throw new ImportExportException("Файл '" + filename + "' пуст");
            }

            List<String> headers = Arrays.asList(line.split(delimiter));
            Map<String, Field> fieldMap = getFieldMap();

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = line.split(delimiter, -1);
                    if (values.length != headers.size()) {
                        throw new ImportExportException(
                                "Несоответствие количества колонок в строке " + lineNumber +
                                        ". Ожидалось: " + headers.size() + ", получено: " + values.length
                        );
                    }

                    T item = createItemFromValues(headers, values, fieldMap);
                    result.add(item);
                } catch (Exception e) {
                    // Исправлено: создаем исключение без лишней скобки
                    throw new ImportExportException("Ошибка обработки строки " + lineNumber + " в файле '" + filename + "'");
                }
            }
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
                values.add(value != null ? value.toString() : "");
            } catch (IllegalAccessException e) {
                values.add("");
            }
        }
        return values;
    }

    private Map<String, Field> getFieldMap() {
        return Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, field -> {
                    field.setAccessible(true);
                    return field;
                }));
    }

    private T createItemFromValues(List<String> headers, String[] values, Map<String, Field> fieldMap) {
        try {
            T item = clazz.getDeclaredConstructor().newInstance();
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
            }
            // RoomStatus и другие сложные типы пропускаем
        } catch (NumberFormatException e) {
            throw new RuntimeException("Неверный числовой формат для поля " + field.getName() + ": " + value, e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка установки значения поля " + field.getName() + ": " + value, e);
        }
    }
}