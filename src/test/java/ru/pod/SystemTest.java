package ru.pod;

import com.sun.istack.internal.Nullable;
import ru.pod.exception.TestFail;
import ru.pod.models.Task;
import ru.pod.parsers.LogParser;
import ru.pod.parsers.PetriParse;
import ru.pod.workers.TokenReplay;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class SystemTest {

    private String fileName;

    public SystemTest(String filename) {
        this.fileName = filename;
    }

    public void testWithFile() throws TestFail {
        testWithFile("");
    }

    public void testWithFile(@Nullable String logName) throws TestFail {

        if (logName.isEmpty()) {
            logName = fileName.substring(0, fileName.length() - 4) + "txt";
        }
        Map<String, Task> taskMap;
        Map<String, Integer> tasksFromLog = new HashMap<>();
        try {
            File f = new File(TestConstant.WAY_FOR_FOLDER + fileName);
            PetriParse petriParse = new PetriParse(f);
            taskMap = petriParse.parsePMNL();
            File log = new File(TestConstant.WAY_FOR_FOLDER + logName);
            LogParser logParser = new LogParser(log);
            tasksFromLog.putAll(logParser.getTaskFromLog());
            TokenReplay tR = new TokenReplay(taskMap, tasksFromLog);
            System.out.println("Fitness = "+tR.tokenReplay()+"\n");

            for (Map.Entry<String, Integer> logTask:tasksFromLog.entrySet()){
                System.out.print(logTask.getKey()+" ");
            }
            System.out.println("\n");
            for (Map.Entry<String, Task> entry : taskMap.entrySet()){
                System.out.print(entry.getKey()+" ");
                System.out.println(entry.getValue().toString());
            }


        }catch (Exception ex){
            ex.printStackTrace();
            throw new TestFail(ex.getMessage());
        }
    }
}