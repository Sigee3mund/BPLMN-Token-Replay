package ru.pod.workers;

import ru.pod.models.Task;
import ru.pod.service.NestedProcessService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedProcessChecker {

    public static Map<String, Task> findNestedProcess(Map<String, Task> taskMap, String fileName) { //Поиск внутренних подпроцессов
        if (fileName.matches(".*.pnml")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        Map<String, Task> returnedTaskMap = new HashMap<>();
        List<String> listTaskForSkip = new ArrayList<>();
        NestedProcessChecker nestedProcessChecker = new NestedProcessChecker();

        for (Map.Entry<String, Task> entry : taskMap.entrySet()) { //Проходим по всей мапе

            if (entry.getValue().getName().matches("(\\w+)_start")) { // Ищем любое название узла со старт в конце

                String taskWithProcessName = nestedProcessChecker.getNameNodeWithProcess(entry.getValue());
                String nestedFileName = fileName + "\\" + taskWithProcessName;

                listTaskForSkip.add(entry.getKey());
                Task startTask = entry.getValue();
                Task lastTask = new Task();

                Map<String, Task> nestedProcess = nestedProcessChecker.getNestedProcess(startTask);

                for (Map.Entry<String, Task> skipTask : nestedProcess.entrySet()) {
                    listTaskForSkip.add(skipTask.getKey());
                    if (skipTask.getValue().getName().matches("\\w+_complete")) {
                        lastTask = skipTask.getValue();
                    }
                }
                nestedProcess.remove(lastTask.getPlaceId());
                //На данном этапе уже имеем внутрений подпроцесс

                for (String nodeNameBefore : lastTask.getSource()) {
                    taskMap.get(nodeNameBefore).setNewTarget();
                }
                listTaskForSkip.add(lastTask.getPlaceId());

                Task taskWithProcess = new Task();
                for (Task afterStart : startTask.getTarget()) {
                    if (afterStart.getName().matches(taskWithProcessName + "_ready")) {
                        taskWithProcess = afterStart;
                    }
                }

                while (!taskWithProcess.getName().equals(taskWithProcessName)) {
                    listTaskForSkip.add(taskWithProcess.getPlaceId());
                    taskWithProcess = taskWithProcess.getTarget().get(0);
                }

                listTaskForSkip.add(taskWithProcess.getTarget().get(0).getPlaceId());


                taskWithProcess = NestedProcessService.getNestedFitness(nestedProcess, nestedFileName, taskWithProcess);
                Map buffer = nestedProcessChecker.setUpNewTaskMap(taskWithProcess, startTask, lastTask, taskMap);
                taskWithProcess = nestedProcessChecker.findNestedTask(buffer, taskWithProcessName);

                returnedTaskMap.put(taskWithProcess.getPlaceId(), taskWithProcess);

            } else { //А если не нашли то просто перезаписываем
                returnedTaskMap.put(entry.getKey(), entry.getValue());
            }
        }
        for (String placeId : listTaskForSkip) {
            returnedTaskMap.remove(placeId);
        }
        return returnedTaskMap;
    }


    private Map<String, Task> getNestedProcess(Task startTask) {
        Map<String, Task> nestedProcess = new HashMap<>();
        Task lastTask = new Task();
        for (Task task : startTask.getTarget()) {
            if (task.getName().matches("start_event_")) {
                task.setNewSource();
                nestedProcess.put(task.getPlaceId(), task);
                lastTask = task;
            }
        }
        ArrayList<Task> saveTask = new ArrayList<>();
        while (!lastTask.getName().matches("\\w+_complete")) { //если LastTask НЕ содержит комплит действуй
            // сохраняем подпроцесс чтобы отправить его на подсчёт
            for (Task task : lastTask.getTarget()) {
                nestedProcess.put(task.getPlaceId(), task);
            }
            saveTask.addAll(lastTask.getTarget());
            lastTask = lastTask.getTarget().get(0);
            saveTask.remove(lastTask);
        }

       // if (!saveTask.isEmpty()) {
       //     throw new RuntimeException("In this version Program cannot work with difficult nested process");
       // }
        return nestedProcess;
    }

    private String getNameNodeWithProcess(Task task) {
        String taskWithProcessName = "";
        Pattern p = Pattern.compile("(\\w+)_start");
        Matcher m = p.matcher(task.getName());
        if (m.find()) {
            taskWithProcessName = m.group(1);
        }
        return taskWithProcessName;
    }

    private Map<String, Task> setUpNewTaskMap(Task taskWithProcess, Task startTask, Task lastTask, Map<String, Task> taskMap) {

        taskWithProcess.setNewSource();
        taskWithProcess.setSource(startTask.getSource());

        for (String nodeName : taskWithProcess.getSource()) {
            taskMap.get(nodeName).setNewTarget();
            taskMap.get(nodeName).setTarget(taskWithProcess);
        }

        taskWithProcess.setNewTarget();
        taskWithProcess.setTarget(lastTask.getTarget());
        for (Task targetTask : taskWithProcess.getTarget()) {
            targetTask.setNewSource();
            targetTask.setSource(taskWithProcess.getPlaceId());
        }
        return taskMap;
    }

    private Task findNestedTask(Map<String, Task> taskMap, String nestedTaskName){
        for (Map.Entry<String, Task> entry : taskMap.entrySet()){
            if(entry.getValue().getName().equals(nestedTaskName)){
                return entry.getValue();
            }
        }
        return new Task();
    }
}
