package Konfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Handler {
    private final Map<String, Function<String[], Integer>> cmdMap = new HashMap<>();

    public static final int SUCCESS = 0;
    public static final int UNKNOWN_COMMAND = 1;

    public Handler() {
        // initialize map of commands
        cmdMap.put("ls", this::LS);
        cmdMap.put("cd", this::CD);
    }

    private int LS(String[] params) {
        System.out.println("Name: ls\nParams: " + Arrays.toString(params));
        return SUCCESS;
    }

    private int CD(String[] params) {
        System.out.println("Name: cd\nParams: " + Arrays.toString(params));
        return SUCCESS;
    }

    public int handle(String cmd, String[] params) {
        if (cmdMap.containsKey(cmd)) {
            cmdMap.get(cmd).apply(params);
        }
        else {
            return UNKNOWN_COMMAND;
        }
        return SUCCESS;
    }
}
