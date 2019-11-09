package ru.pod.workers;

import ru.pod.exception.TokenReplayException;
import ru.pod.models.Task;

import java.util.*;

public class TokenReplay {
    private final Map<String, Task> taskMap;
    private final Map<String, Integer> logList;

    public TokenReplay(Map<String, Task> tasksMap, Map<String, Integer> logList) {
        this.taskMap = tasksMap;
        this.logList = logList;
    }

    public double tokenReplay() throws TokenReplayException {
        double smallFitness;
        double nestedFitness = 0.0;
        int n = 1;
        float p = 0, c = 0, m = 0, r = 0, allP = 0, allC = 0, allM = 0, allR = 0;
        Task startTask = new Task();
        List<Task> lastTask = new ArrayList<>();
        for (Map.Entry<String, Task> oneTask : taskMap.entrySet()) {
            if (oneTask.getValue().getName().equals("start_event_")) {
                startTask = oneTask.getValue();
            } else {
                if (oneTask.getValue().getTarget().isEmpty()) {
                    lastTask.add(oneTask.getValue());
                }
            }
            if (oneTask.getValue().getNestedFitness() > -0.1) {
                nestedFitness += oneTask.getValue().getNestedFitness();
                n++;
            } else {
                oneTask.getValue().setNestedFitness(0.0);
            }
        }
        for (Map.Entry<String, Integer> oneWorkFromLog : logList.entrySet()) {
            getFitnessForOneLogTask(oneWorkFromLog.getKey(), startTask, lastTask);
            for (Map.Entry<String, Task> task : taskMap.entrySet()) {
                p = p + task.getValue().getpReal();
                c = c + task.getValue().getcReal();
                m = m + task.getValue().getM();
                r = r + task.getValue().getrReal();
            }

            smallFitness = (0.5 * (1 - m / c) + 0.5 * (1 - r / p) + nestedFitness) / n;
            System.out.println("Fitness of trace " + oneWorkFromLog.getKey() + " = " + smallFitness);
            cleanTaskMap();
            allC += c * oneWorkFromLog.getValue();
            allP += p * oneWorkFromLog.getValue();
            allM += m * oneWorkFromLog.getValue();
            allR += r * oneWorkFromLog.getValue();
            p = 0;
            c = 0;
            m = 0;
            r = 0;
        }


        return (0.5 * (1 - allM / allC) + 0.5 * (1 - allR / allP)  + nestedFitness) / n;
    }

    private void getFitnessForOneLogTask(String logTask, Task start, List<Task> last) {
        ArrayList<String> logWork = oneTaskFromLogSmashOnTaskName(logTask);
        Map<String, String> nameNodeMap = nodeNameMap();
        start.addP();
        Task taskNow = new Task();
        Task taskLast = new Task();
        for (Task task : start.getTarget()) {
            if (task.getName().equals(logWork.get(0))) {
                taskNow = task;
                taskLast = start;
                logWork.remove(0);
            }
        }
        if (taskNow.isEmpty()) {
            start.addR();
            taskNow = taskMap.get(nameNodeMap.get(logWork.get(0)));
            taskLast = taskMap.get(taskNow.getSource().get(0));
            if (taskLast.equals(last)) {
                taskLast.addM();
            }
            logWork.remove(0);
        }

        while (!checkOnLast(last, taskNow)) {
            Task taskNext = new Task();
            // Организация перехода. Перенос меток
            if (taskLast.getP() > 0) {
                taskNow.addC();
                taskLast.incP();
            } else {
                if (taskLast.getR() > 0) {
                    taskNow.addC();
                    taskLast.incR();
                } else {
                    taskNow.addC();
                    taskLast.addM();
                }
            }
            taskNow.addP();
            // Поиск следующего задания при сравнении с логом. Если таких нет просто найти по логу. Если лог пуст то сразу на конец.
            if (!logWork.isEmpty()) {
                for (Task task : taskNow.getTarget()) {
                    if (task.getName().equals(logWork.get(0))) {
                        taskNext = task;
                        logWork.remove(0);
                        break;
                    } else {
                        if (task.getName().matches(".*" + logWork.get(0) + ".*")) {
                            for (Task taskMini : task.getTarget()) {
                                if (taskMini.getName().equals(logWork.get(0))) {
                                    taskNext = taskMini;
                                    logWork.remove(0);
                                }
                            }
                        }
                    }
                }
                if (taskNext.isEmpty()) {
                    taskNow.addR();
                    taskNow.incP();
                    taskNext = taskMap.get(nameNodeMap.get(logWork.get(0)));
                    taskNow = taskMap.get(taskNext.getSource().get(0));
                    logWork.remove(0);
                }
            } else {
                if (!taskNow.equals(last)) {
                    taskNow.addR();
                    taskNext = last.get(0);
                    taskNext.addC();
                }

            }
            taskLast = taskNow;
            taskNow = taskNext;
        }


        if (taskLast.getP() > 0) {
            taskNow.addC();
            taskLast.incP();
        } else {
            if (taskLast.getR() > 0) {
                taskNow.addC();
                taskLast.incR();
            } else {
                taskNow.addC();
                taskLast.addM();
            }
        }
    }

    private boolean checkOnLast(List<Task> lastTasks, Task task) {
        boolean isLast = false;
        for (Task last : lastTasks) {
            if (task.equals(last)) {
                isLast = true;
            }
        }
        return isLast;
    }

    private void cleanTaskMap() {
        for (Map.Entry<String, Task> task : taskMap.entrySet()) {
            task.getValue().cleanToken();
        }
    }


    private HashMap<String, String> nodeNameMap() {
        HashMap<String, String> nameNodeMap = new HashMap<>();
        for (Map.Entry<String, Task> oneTask : taskMap.entrySet()) {
            nameNodeMap.put(oneTask.getValue().getName(), oneTask.getKey());
        }
        return nameNodeMap;
    }

    private ArrayList<String> oneTaskFromLogSmashOnTaskName(String logTask) {
        StringBuilder oneTask = new StringBuilder();
        ArrayList<String> logWork = new ArrayList<>();
        for (char oneChar : logTask.toCharArray()) {
            if ((oneChar != '<') && (oneChar != ',') && (oneChar != '>')) {
                oneTask.append(oneChar);
            } else {
                oneTask = new StringBuilder(oneTask.toString().trim());
                if (oneTask.length() > 0) {
                    logWork.add(oneTask.toString());
                }
                oneTask = new StringBuilder();
            }
        }
        logWork.add("end_event_");

        return logWork;
    }

}
