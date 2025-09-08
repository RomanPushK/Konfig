package Konfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Emulator {
    private static String getUser() {
        String username = System.getProperty("user.name");
        String dir = System.getProperty("user.dir");
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return username + "@" + hostname + ":" + dir + "$";
        } catch (UnknownHostException e){
            return username + "@localhost:" + dir + "$";
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Handler handler = new Handler();

        System.out.println(getUser());

        while (true) {
            String[] line = input.nextLine().split(" ");
            String cmd = line[0];
            String[] params = Arrays.copyOfRange(line, 1, line.length);

            if (Objects.equals(cmd, "exit")) break;

            int result = handler.handle(cmd, params);
            if (result != 0) System.out.println("Error. Code: " + result);
        }
        input.close();
    }
}
