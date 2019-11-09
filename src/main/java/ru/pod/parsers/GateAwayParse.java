package ru.pod.parsers;

import ru.pod.models.Task;

import java.util.ArrayList;
import java.util.Map;

public class GateAwayParse {

    private Map<String,Task> taskMap;

    GateAwayParse(Map<String, Task> taskMap) {
        this.taskMap = taskMap;
    }

    public Map<String, Task> parseGateway(){
        Map<String, Task> parseMap=taskMap;
        boolean XOR=false;
        for (Map.Entry<String, Task> entry : taskMap.entrySet()){
            if(entry.getValue().getName().matches("xor_.*")){
                XOR=true;
            }

        }

        if (XOR){
            parseMap=workWithXORGateAway();
        }

        return parseMap;
    }

    private Map<String, Task> workWithXORGateAway(){
        Map<String,Task> parsetTaskMap=taskMap;
        for (Map.Entry<String, Task> entry:parsetTaskMap.entrySet()){
            if (entry.getValue().getName().matches("xor_")||
                    (entry.getValue().getName().matches("xor_merge.*"))||
                    entry.getValue().getName().matches("xor_split.*")) {
                // Узлу который идет ДО найденного проставляем таргет(цель) найденных
                for (String nodeName : entry.getValue().getSource()){
                    Task earlyTask =taskMap.get(nodeName);
                    earlyTask.getTarget().remove(entry.getValue());
                    earlyTask.setTarget(entry.getValue().getTarget());
                }
                // Узлу который идет ПОСЛЕ найденного, проставляем сурсы(источники) найденного
                for (Task task : entry.getValue().getTarget()) {
                    Task afterTask = taskMap.get(task.getPlaceId());
                    afterTask.getSource().remove(entry.getValue().getPlaceId());
                    afterTask.setSource(entry.getValue().getSource());
                }
                parsetTaskMap.put(entry.getKey(),new Task());
            }
        }
        return deleteNull(parsetTaskMap);
    }



    private Map<String,Task> targetSource(Map<String,Task> parsetTaskMap, String task){
        for (Task target:parsetTaskMap.get(task).getTarget()){
            target.setNewSource();
            target.setSource(parsetTaskMap.get(task).getSource());
        }

        for (String source:parsetTaskMap.get(task).getSource()){
            parsetTaskMap.get(source).setNewTarget();
            parsetTaskMap.get(source).setTarget(parsetTaskMap.get(task).getTarget());
        }
        parsetTaskMap.put(task,new Task());
        return parsetTaskMap;
    }

    private Map<String,Task> targetSource(Map<String,Task> parsetTaskMap, Map.Entry<String, Task> entry){
        for (String source:entry.getValue().getSource()){
            parsetTaskMap.get(source).setNewTarget();
            parsetTaskMap.get(source).setTarget(entry.getValue().getTarget());
        }
        for (Task target:entry.getValue().getTarget()){
            target.setNewSource();
            target.setSource(entry.getValue().getSource());
        }
        parsetTaskMap.put(entry.getKey(),new Task());
        return parsetTaskMap;
    }

    private Map<String,Task> deleteNull(Map<String,Task> parsetTaskMap){
        ArrayList<String> forDelete=new ArrayList<>();
        for (Map.Entry<String, Task> entry : parsetTaskMap.entrySet()){
            if (entry.getValue().equals(new Task())){
                forDelete.add(entry.getKey());
            }
        }
        for (String key:forDelete){
            parsetTaskMap.remove(key);
        }
        return parsetTaskMap;
    }
}

