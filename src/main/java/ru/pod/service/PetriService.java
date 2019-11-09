package ru.pod.service;

import ru.pod.exception.ParseException;
import ru.pod.models.WayToModels;
import ru.pod.models.Task;
import ru.pod.parsers.PetriParse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PetriService {
    private String fileName;

    public PetriService(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Task> getPetriMap() {
        Map<String, Task> taskMap= new HashMap<>();
        try {
            File f = new File(WayToModels.WAY_FOR_FOLDER + fileName);
            PetriParse petriParse = new PetriParse(f);
            taskMap = petriParse.parsePMNL();
        } catch (
                ParseException e) {
            e.printStackTrace();
        }
        return taskMap;
    }
}
