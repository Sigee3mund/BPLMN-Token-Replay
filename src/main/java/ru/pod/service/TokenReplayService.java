package ru.pod.service;

import ru.pod.models.Task;

import java.util.Map;

import ru.pod.workers.TokenReplay;

public class TokenReplayService {

    public static double getFitness(Map<String, Task> taskMap, Map<String, Integer> tasksFromLog) {
        if ((tasksFromLog.isEmpty()) || (taskMap.isEmpty())) {
            throw new RuntimeException();
        }
        double fitness;
        try {
            TokenReplay tR = new TokenReplay(taskMap, tasksFromLog);
            fitness = tR.tokenReplay();
            System.out.println("General Fitness = " + fitness);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.err.println("Fitness = 0");
            fitness = 0.0;
        }
        return fitness;
    }
}
