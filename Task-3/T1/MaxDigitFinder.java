package T1;

public class MaxDigitFinder {
    public static void main(String[] args) {
        // Генерация трехзначного числа с использованием указанной конструкции
        int number = 100 + (new java.util.Random()).nextInt(900);
        System.out.println("Сгенерированное число: " + number);

        int maxDigit = findMaxDigit(number);
        System.out.println("Наибольшая цифра: " + maxDigit);
    }

    public static int findMaxDigit(int number) {
        int digit1 = number / 100;
        int digit2 = (number / 10) % 10;
        int digit3 = number % 10;

        return Math.max(digit1, Math.max(digit2, digit3));
    }
}