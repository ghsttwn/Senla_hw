package T8.T1.annotations;

public enum PropertyType {
    AUTO,           // Автоматическое преобразование
    STRING,         // Оставить как строку
    INTEGER,        // Преобразовать в Integer
    LONG,           // Преобразовать в Long
    DOUBLE,         // Преобразовать в Double
    BOOLEAN,        // Преобразовать в Boolean
    LIST,           // Преобразовать в List<String>
    ARRAY,          // Преобразовать в массив (автоматически определяет тип)
    STRING_ARRAY,   // Преобразовать в String[]
    INTEGER_ARRAY,  // Преобразовать в Integer[] или int[]
    LONG_ARRAY,     // Преобразовать в Long[] или long[]
    DOUBLE_ARRAY,   // Преобразовать в Double[] или double[]
    BOOLEAN_ARRAY,  // Преобразовать в Boolean[] или boolean[]
    FLOAT_ARRAY,    // Преобразовать в Float[] или float[]
    DATE,           // Преобразовать в LocalDate
    CUSTOM          // Пользовательский тип
}