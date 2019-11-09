package ru.pod;

import ru.pod.models.Task;
import ru.pod.service.LogService;
import ru.pod.service.PetriService;
import ru.pod.service.TokenReplayService;
import ru.pod.workers.NestedProcessChecker;

import java.util.Map;


public class Main {

    private static String fileName = "with.pnml";
    private static PetriService petriService = new PetriService(fileName);
    private static LogService logService = new LogService(fileName);

    public static void main(String[] args) {

        Map<String, Task> taskMap = petriService.getPetriMap();
        Map<String, Integer> tasksFromLog = logService.getLogTask();
        taskMap = NestedProcessChecker.findNestedProcess(taskMap, fileName);
        TokenReplayService.getFitness(taskMap, tasksFromLog);
    }
}
