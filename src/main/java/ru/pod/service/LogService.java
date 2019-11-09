package ru.pod.service;

import ru.pod.exception.LogParserException;
import ru.pod.models.WayToModels;
import ru.pod.parsers.LogParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LogService {
    private String logName;

    public LogService(String fileName) {
        if (fileName.matches(".*.pnml")){
            this.logName = fileName.substring(0, fileName.length() - 4) + "txt";
        }else {
            this.logName = fileName + ".txt";
        }
    }

    public Map<String, Integer> getLogTask() {
        Map<String, Integer> tasksFromLog = new HashMap<>();
        try {
            File log = new File(WayToModels.WAY_FOR_FOLDER + logName);
            LogParser logParser = new LogParser(log);
            tasksFromLog.putAll(logParser.getTaskFromLog());
        } catch (
                LogParserException ex) {
            ex.printStackTrace();
        }
        return tasksFromLog;
    }
}
