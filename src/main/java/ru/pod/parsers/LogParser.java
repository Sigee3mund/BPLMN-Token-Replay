package ru.pod.parsers;

import com.sun.istack.internal.NotNull;
import ru.pod.exception.LogParserException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogParser {

    private File fileName;

    public LogParser(@NotNull File fileName) throws LogParserException {
        if ((fileName.canRead()) && (fileName.exists())) {
            this.fileName = fileName;
        } else {
            throw new LogParserException("LogFile is not load: Uncorrected data");
        }
    }

    public Map<String, Integer> getTaskFromLog() throws LogParserException {
        Map<String, Integer> taskFromLog = new HashMap<>();
        ArrayList<String> allTasks;
        boolean withReplayNumber = false;
        String pattern = ("(<.*>)\\s*(\\d+)");
        try {
            allTasks = Files.lines(Paths.get(String.valueOf(fileName)), StandardCharsets.UTF_8)
                    .collect(Collectors.toCollection(ArrayList::new));

        } catch (Exception e) {
            throw new LogParserException("Cannot Read the File");
        }

        if (allTasks.get(0).matches(pattern)) {
            withReplayNumber = true;
        }
        for (String task : allTasks) {
            if (!task.isEmpty()) {
                if (!withReplayNumber) {
                    if (taskFromLog.containsKey(task)) {
                        taskFromLog.put(task, taskFromLog.get(task) + 1);
                    } else {
                        taskFromLog.put(task, 1);
                    }
                } else {
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(task);
                    if (m.matches()) {
                        taskFromLog.put(m.group(1), Integer.parseInt(m.group(2)));
                    }
                }
            }
        }
        return taskFromLog;
    }

}
