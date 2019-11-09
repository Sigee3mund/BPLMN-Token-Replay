package ru.pod;

import ru.pod.exception.TestFail;

public class TestMain {

    public static void main (String[] args) {
        String fN="XOR";

        SystemTest test=new SystemTest(fN+".pnml");
        try {
            test.testWithFile();
        }catch (TestFail ex){
            ex.getMessage();
        }
    }
}

