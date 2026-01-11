package T8.T1.model;

import java.util.Objects;
import java.io.Serializable;
import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;
import java.time.LocalDate;

public class Guest implements Comparable<Guest>, Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    @ConfigProperty(propertyName = "guest.default.id", type = PropertyType.LONG)
    private Long id;

    @ConfigProperty(propertyName = "guest.default.name", type = PropertyType.STRING)
    private String name;

    @ConfigProperty(propertyName = "guest.passport.format", type = PropertyType.STRING)
    private String passportNumber;

    @ConfigProperty(propertyName = "guest.phone.format", type = PropertyType.STRING)
    private String phoneNumber;

    @ConfigProperty(propertyName = "guest.email", type = PropertyType.STRING)
    private String email;

    @ConfigProperty(propertyName = "guest.birth.date", type = PropertyType.STRING)
    private LocalDate birthDate;

    @ConfigProperty(propertyName = "guest.nationality", type = PropertyType.STRING)
    private String nationality;

    @ConfigProperty(propertyName = "guest.auto.generate.id", type = PropertyType.BOOLEAN)
    private static boolean autoGenerateId = true;

    @ConfigProperty(propertyName = "guest.max.name.length", type = PropertyType.INTEGER)
    private static int maxNameLength = 100;

    @ConfigProperty(propertyName = "guest.passport.regex", type = PropertyType.STRING)
    private static String passportRegex = "\\d{10}";

    @ConfigProperty(propertyName = "guest.phone.regex", type = PropertyType.STRING)
    private static String phoneRegex = "^\\+?[\\d\\s\\-\\(\\)]+$";

    @ConfigProperty(propertyName = "guest.email.regex", type = PropertyType.STRING)
    private static String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";

    @ConfigProperty(propertyName = "guest.min.age", type = PropertyType.INTEGER)
    private static int minAge = 18;

    @ConfigProperty(propertyName = "guest.max.age", type = PropertyType.INTEGER)
    private static int maxAge = 120;

    @ConfigProperty(propertyName = "guest.default.nationality", type = PropertyType.STRING)
    private static String defaultNationality = "Россия";

    @ConfigProperty(propertyName = "guest.validation.enabled", type = PropertyType.BOOLEAN)
    private static boolean validationEnabled = true;

    @ConfigProperty(propertyName = "guest.require.email", type = PropertyType.BOOLEAN)
    private static boolean requireEmail = false;

    @ConfigProperty(propertyName = "guest.require.birth.date", type = PropertyType.BOOLEAN)
    private static boolean requireBirthDate = false;

    public Guest() {
        this.nationality = defaultNationality;
    }

    public Guest(String name, String passportNumber, String phoneNumber) {
        setName(name);
        setPassportNumber(passportNumber);
        setPhoneNumber(phoneNumber);
        this.nationality = defaultNationality;
    }

    public Guest(Long id, String name, String passportNumber, String phoneNumber) {
        this(name, passportNumber, phoneNumber);
        setId(id);
    }

    public Guest(String name, String passportNumber, String phoneNumber, String email, LocalDate birthDate, String nationality) {
        this(name, passportNumber, phoneNumber);
        setEmail(email);
        setBirthDate(birthDate);
        setNationality(nationality);
    }

    public Long getId() { return id; }
    public void setId(Long id) {
        if (autoGenerateId && id == null) {
            this.id = generateAutoId();
        } else {
            this.id = id;
        }
    }

    private Long generateAutoId() {
        return System.currentTimeMillis() % 1000000 + 2000000;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (validationEnabled) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Имя гостя не может быть пустым");
            }
            if (name.length() > maxNameLength) {
                throw new IllegalArgumentException("Имя гостя не может превышать " + maxNameLength + " символов");
            }
        }
        this.name = name;
    }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) {
        if (validationEnabled) {
            if (passportNumber == null || passportNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Номер паспорта не может быть пустым");
            }
            if (!passportNumber.matches(passportRegex)) {
                throw new IllegalArgumentException("Номер паспорта должен соответствовать формату: " + passportRegex);
            }
        }
        this.passportNumber = passportNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        if (validationEnabled) {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Телефон не может быть пустым");
            }
            if (!phoneNumber.matches(phoneRegex)) {
                throw new IllegalArgumentException("Телефон должен соответствовать формату: " + phoneRegex);
            }
        }
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (validationEnabled && requireEmail && (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException("Email обязателен для заполнения");
        }
        if (email != null && !email.trim().isEmpty() && validationEnabled && !email.matches(emailRegex)) {
            throw new IllegalArgumentException("Email должен соответствовать формату: " + emailRegex);
        }
        this.email = email;
    }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) {
        if (validationEnabled && requireBirthDate && birthDate == null) {
            throw new IllegalArgumentException("Дата рождения обязательна для заполнения");
        }
        if (birthDate != null && validationEnabled) {
            int age = calculateAge(birthDate);
            if (age < minAge) {
                throw new IllegalArgumentException("Гость должен быть не младше " + minAge + " лет");
            }
            if (age > maxAge) {
                throw new IllegalArgumentException("Возраст гостя не может превышать " + maxAge + " лет");
            }
        }
        this.birthDate = birthDate;
    }

    private int calculateAge(LocalDate birthDate) {
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) {
        this.nationality = nationality != null ? nationality : defaultNationality;
    }

    public int getAge() {
        if (birthDate == null) return 0;
        return calculateAge(birthDate);
    }

    public boolean isAdult() {
        return getAge() >= 18;
    }

    @Override
    public int compareTo(Guest other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(passportNumber, guest.passportNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passportNumber);
    }

    @Override
    public String toString() {
        return String.format("Guest{id=%d, name='%s', passport='%s', phone='%s', email='%s', age=%d, nationality='%s'}",
                id, name, passportNumber, phoneNumber, email, getAge(), nationality);
    }

    // Дополнительные методы
    public String getContactInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Телефон: ").append(phoneNumber);
        if (email != null && !email.trim().isEmpty()) {
            sb.append(", Email: ").append(email);
        }
        return sb.toString();
    }

    public String getFormattedPassport() {
        if (passportNumber == null || passportNumber.length() != 10) {
            return passportNumber;
        }
        // Формат: XX XX XXXXXX
        return passportNumber.substring(0, 2) + " " +
                passportNumber.substring(2, 4) + " " +
                passportNumber.substring(4);
    }

    // Статические геттеры и сеттеры для конфигурации
    public static boolean isAutoGenerateId() { return autoGenerateId; }
    public static void setAutoGenerateId(boolean autoGenerateId) {
        Guest.autoGenerateId = autoGenerateId;
    }

    public static int getMaxNameLength() { return maxNameLength; }
    public static void setMaxNameLength(int maxNameLength) {
        Guest.maxNameLength = maxNameLength;
    }

    public static String getPassportRegex() { return passportRegex; }
    public static void setPassportRegex(String passportRegex) {
        Guest.passportRegex = passportRegex;
    }

    public static String getPhoneRegex() { return phoneRegex; }
    public static void setPhoneRegex(String phoneRegex) {
        Guest.phoneRegex = phoneRegex;
    }

    public static String getEmailRegex() { return emailRegex; }
    public static void setEmailRegex(String emailRegex) {
        Guest.emailRegex = emailRegex;
    }

    public static int getMinAge() { return minAge; }
    public static void setMinAge(int minAge) {
        Guest.minAge = minAge;
    }

    public static int getMaxAge() { return maxAge; }
    public static void setMaxAge(int maxAge) {
        Guest.maxAge = maxAge;
    }

    public static String getDefaultNationality() { return defaultNationality; }
    public static void setDefaultNationality(String defaultNationality) {
        Guest.defaultNationality = defaultNationality;
    }

    public static boolean isValidationEnabled() { return validationEnabled; }
    public static void setValidationEnabled(boolean validationEnabled) {
        Guest.validationEnabled = validationEnabled;
    }

    public static boolean isRequireEmail() { return requireEmail; }
    public static void setRequireEmail(boolean requireEmail) {
        Guest.requireEmail = requireEmail;
    }

    public static boolean isRequireBirthDate() { return requireBirthDate; }
    public static void setRequireBirthDate(boolean requireBirthDate) {
        Guest.requireBirthDate = requireBirthDate;
    }
}