package Konfig;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.Base64;

public class ShellEmulator {
    private final Scanner scanner = new Scanner(System.in);

    private String username;
    private String hostname;
    private String vfsPath = "(не задан)";
    private String scriptPath = "(не задан)";
    private final VDirectory rootDir = new VDirectory("/", null);
    private VDirectory currentDir = rootDir;

    public static void main(String[] args) {
        new ShellEmulator().start(args);
    }

    private void start(String[] args) {
        username = System.getProperty("user.name", "user");
        hostname = getHostName();

        if (args.length == 0) askUserForPaths();
        else parseArguments(args);

        printDebugInfo();
        loadVFS(vfsPath);

        if (!scriptPath.equals("(не задан)") && !scriptPath.isBlank()) {
            executeStartupScript(scriptPath);
        }

        runInteractiveMode();
    }

    // === Настройка и конфигурация ===
    private void askUserForPaths() {
        System.out.println("=== Настройка эмулятора ===");
        System.out.print("Введите путь к виртуальной файловой системе (VFS .csv): ");
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

    private void loadVFS(String path) {
        if (path.equals("(не задан)")) {
            System.out.println("VFS не задана — используется пустая файловая система.\n");
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Ошибка: файл VFS не найден (" + path + ")");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            if (header == null || !header.contains("path")) {
                System.out.println("Ошибка: неверный формат CSV-файла VFS.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 3);
                if (parts.length < 2) continue;

                String filePath = parts[0].trim().replace("\\", "/"); // исправление под Windows
                String type = parts[1].trim();
                String content = (parts.length == 3) ? parts[2] : "";

                if (type.equals("dir")) {
                    createDirectory(filePath);
                } else if (type.equals("file")) {
                    byte[] data = new byte[0];
                    if (!content.isEmpty()) {
                        try {
                            data = Base64.getDecoder().decode(content);
                        } catch (Exception e) {
                            System.out.println("Ошибка декодирования файла: " + filePath);
                        }
                    }
                    createFile(filePath, new String(data));
                }
            }

            System.out.println("VFS успешно загружена!\n");
        } catch (IOException e) {
            System.out.println("Ошибка при чтении VFS: " + e.getMessage());
        }
    }

    private void createDirectory(String path) {
        if (path.equals("/")) return;

        String[] parts = path.split("/");
        VDirectory current = rootDir;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            VDirectory next = current.subdirs.get(part);
            if (next == null) {
                next = new VDirectory(part, current);
                current.subdirs.put(part, next);
            }
            current = next;
        }
    }

    private void createFile(String path, String content) {
        int lastSlash = path.lastIndexOf('/');
        String dirPath = (lastSlash <= 0) ? "/" : path.substring(0, lastSlash);
        String fileName = path.substring(lastSlash + 1);

        VDirectory dir = navigateToDirectory(dirPath);
        if (dir == null) {
            createDirectory(dirPath);
            dir = navigateToDirectory(dirPath);
        }
        if (dir != null) dir.files.put(fileName, new VFile(fileName, content));
    }

    private VDirectory navigateToDirectory(String path) {
        if (path.equals("/") || path.isEmpty()) return rootDir;
        String[] parts = path.split("/");
        VDirectory dir = rootDir;

        for (String p : parts) {
            if (p.isEmpty()) continue;
            dir = dir.subdirs.get(p);
            if (dir == null) return null;
        }
        return dir;
    }

    // === REPL и команды ===
    private void runInteractiveMode() {
        System.out.println("Интерактивный режим. Введите 'exit' для выхода.\n");
        while (true) {
            System.out.print(username + "@" + hostname + ":" + currentDir.getPath() + "$ ");
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
            case "ls" -> commandLs();
            case "cd" -> commandCd(args);
            default -> System.out.println("Ошибка: неизвестная команда '" + command + "'");
        }
        return false;
    }

    private List<String> parseInput(String input) {
        String[] parts = input.split("\\s+");
        return new ArrayList<>(Arrays.asList(parts));
    }

    private void commandLs() {
        System.out.println("Содержимое каталога " + currentDir.getPath() + ":");
        if (currentDir.subdirs.isEmpty() && currentDir.files.isEmpty()) {
            System.out.println("(пусто)");
        } else {
            for (String name : currentDir.subdirs.keySet()) System.out.println("[dir]  " + name);
            for (String name : currentDir.files.keySet()) System.out.println("[file] " + name);
        }
    }

    private void commandCd(List<String> args) {
        if (args.isEmpty()) {
            System.out.println("Ошибка: не указан путь.");
            return;
        }

        String path = args.getFirst().replace("\\", "/");
        VDirectory target;

        if (path.equals("/")) {
            currentDir = rootDir;
            return;
        } else if (path.equals("..")) {
            if (currentDir.parent != null) currentDir = currentDir.parent;
            return;
        } else if (path.startsWith("/")) {
            target = navigateToDirectory(path);
        } else {
            target = navigateToDirectory(currentDir.getPath() + "/" + path);
        }

        if (target != null) currentDir = target;
        else System.out.println("Ошибка: каталог '" + path + "' не найден.");
    }

    static class VDirectory {
        String name;
        VDirectory parent;
        Map<String, VDirectory> subdirs = new LinkedHashMap<>();
        Map<String, VFile> files = new LinkedHashMap<>();

        VDirectory(String name, VDirectory parent) {
            this.name = name;
            this.parent = parent;
        }

        String getPath() {
            if (parent == null) return "/";
            if (parent.getPath().equals("/")) return "/" + name;
            return parent.getPath() + "/" + name;
        }
    }

    static class VFile {
        String name;
        String content;

        VFile(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
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
                System.out.println(username + "@" + hostname + ":" + currentDir.getPath() + "$ " + line);
                processCommand(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении скрипта: " + e.getMessage());
        }

        System.out.println("=== Завершено выполнение скрипта ===\n");
    }
}
