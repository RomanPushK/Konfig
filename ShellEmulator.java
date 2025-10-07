package Konfig;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

public class ShellEmulator {
    private final Scanner scanner = new Scanner(System.in);
    private String username;
    private String hostname;
    private String vfsPath = "(не задан)";
    private String scriptPath = "(не задан)";

    public static void main(String[] args) {
        new ShellEmulator().start(args);
    }

    private void start(String[] args) {
        username = System.getProperty("user.name", "user");
        hostname = getHostName();

        if (args.length == 0) {
            askUserForPaths();
        } else {
            parseArguments(args);
        }

        printDebugInfo();

        if (!scriptPath.equals("(не задан)") && !scriptPath.isBlank()) {
            executeStartupScript(scriptPath);
        }

        runInteractiveMode();
    }

    private void askUserForPaths() {
        System.out.println("=== Настройка эмулятора ===");
        System.out.print("Введите путь к виртуальной файловой системе (VFS): ");
        vfsPath = scanner.nextLine().trim();
        if (vfsPath.isEmpty()) vfsPath = "(не задан)";

        System.out.print("Введите путь к стартовому скрипту (можно пропустить): ");
        scriptPath = scanner.nextLine().trim();
        if (scriptPath.isEmpty()) scriptPath = "(не задан)";
        System.out.println();
    }

    private void parseArguments(String[] args) {
        if (args.length >= 1) vfsPath = args[0];
        if (args.length >= 2) scriptPath = args[1];
    }

    private void printDebugInfo() {
        System.out.println("=== Конфигурация эмулятора ===");
        System.out.println("Пользователь: " + username);
        System.out.println("Имя хоста: " + hostname);
        System.out.println("Путь к VFS: " + vfsPath);
        System.out.println("Путь к стартовому скрипту: " + scriptPath);
        System.out.println("===============================\n");
    }

    private void executeStartupScript(String path) {
        System.out.println("=== Выполнение стартового скрипта: " + path + " ===");

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Ошибка: файл скрипта не найден или не является файлом.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                System.out.println(username + "@" + hostname + ":~$ " + line);
                processCommand(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении скрипта: " + e.getMessage());
        }

        System.out.println("=== Завершено выполнение скрипта ===\n");
    }

    private void runInteractiveMode() {
        System.out.println("Интерактивный режим. Введите 'exit' для выхода.\n");

        while (true) {
            System.out.print(username + "@" + hostname + ":~$ ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            if (processCommand(input)) break;
        }
    }

    private boolean processCommand(String input) {
        List<String> tokens = parseInput(input);
        String command = tokens.getFirst();
        List<String> args = tokens.subList(1, tokens.size());

        switch (command) {
            case "exit" -> {
                System.out.println("Выход из эмулятора...");
                return true;
            }
            case "ls", "cd" -> handleStubCommand(command, args);
            default -> System.out.println("Ошибка: неизвестная команда '" + command + "'");
        }
        return false;
    }

    private List<String> parseInput(String input) {
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
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }
}
