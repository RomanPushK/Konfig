package Konfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Emulator {
    final static Path rootDir = Paths.get("D:/Mirea/3 семестр/Konfig").toAbsolutePath().normalize();
    static Path currentDir = rootDir;

    private static String getUser() {
        String username = System.getProperty("user.name");
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return username + "@" + hostname + ":";
        } catch (UnknownHostException e){
            return username + "@localhost:";
        }
    }

    private static String getPath(int full) {
        if (full == 0) {
            return currentDir.toString().replace(rootDir.toString(), "~") + "$";
        }
        return currentDir.toString() + "$";
    }

    private static String getPath() {
        return currentDir.toString().replace(rootDir.toString(), "~") + "$";
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Handler handler = new Handler();
        while (true) {
            System.out.print(getUser() + getPath() + " ");
            String[] line = input.nextLine().split(" ");
            String cmd = line[0];
            String[] params = Arrays.copyOfRange(line, 1, line.length);

            if (Objects.equals(cmd, "exit")) break;

            int result = handler.handle(cmd, params);
            if (result != 0) {
                switch (result) {
                    case 1:
                        System.out.println("Error. Unknown command. Code: " + result);
                        break;
                    default:
                        System.out.println("Error. Unknown error. Code: " + result);
                        break;
                }
            }
        }
        input.close();
    }
}
