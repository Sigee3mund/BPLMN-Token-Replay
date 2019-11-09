package ru.pod.service;

import ru.pod.models.Task;
import ru.pod.workers.NestedProcessChecker;

import java.util.Map;

public class NestedProcessService {

    public static Task getNestedFitness(Map<String, Task> nestedTaskMap, String nestedFileName, Task taskWithProcess) {

        nestedTaskMap = NestedProcessChecker.findNestedProcess(nestedTaskMap, nestedFileName);
        LogService logService = new LogService(nestedFileName);
        double nestedFitness = TokenReplayService.getFitness(nestedTaskMap, logService.getLogTask());
        taskWithProcess.setNestedFitness(nestedFitness);
        return taskWithProcess;
    }
}



