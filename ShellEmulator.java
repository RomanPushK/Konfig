package Konfig;

import java.util.*;

public class ShellEmulator {
    public static void main(String[] args) {
        new ShellEmulator().start();
    }

    private final Scanner scanner = new Scanner(System.in);

    private void start() {
        String username = System.getProperty("user.name", "user");
        String hostname = getHostName();
        String prompt = username + "@" + hostname + ":~$ ";

        System.out.println("Эмулятор командной оболочки (вариант 19)");
        System.out.println("Введите 'exit' для выхода.\n");

        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            List<String> tokens = parseInput(input);
            String command = tokens.getFirst();
            List<String> arguments = tokens.subList(1, tokens.size());

            if (command.equals("exit")) {
                System.out.println("Выход из эмулятора...");
                break;
            }

            switch (command) {
                case "ls" -> handleStubCommand("ls", arguments);
                case "cd" -> handleStubCommand("cd", arguments);
                default -> System.out.println("Ошибка: неизвестная команда '" + command + "'");
            }
        }
    }

    private List<String> parseInput(String input) {
        // Простой парсер, разделяет по пробелам, игнорируя лишние
        String[] parts = input.split("\\s+");
        return new ArrayList<>(Arrays.asList(parts));
    }

    private void handleStubCommand(String command, List<String> args) {
        System.out.println("Выполняется команда: " + command);
        if (args.isEmpty()) {
            System.out.println("Аргументы: (нет)");
        } else {
            System.out.println("Аргументы: " + String.join(", ", args));
        }
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }
}
