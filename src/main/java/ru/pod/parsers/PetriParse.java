package ru.pod.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.pod.exception.ParseException;
import ru.pod.models.Task;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PetriParse {
    private String fileName;

    public PetriParse(File fileName) throws ParseException {
        if ((fileName.canRead()) && (fileName.exists())) {
            this.fileName = fileName.toString();
        } else {
            throw new ParseException("File is not load: Uncorrected data;\n" + fileName);
        }
    }

    public Map<String, Task> parsePMNL() throws ParseException {

        Map<String, Task> taskMap = new HashMap<String, Task>();
        NodeList petriTaskPlace;
        NodeList petriFinalMarkings;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(fileName);
            Node root = document.getDocumentElement();
            if ((root.getFirstChild().equals(root.getLastChild())) && (root.getNodeName().equals("pnml"))) {
                Node net = root.getFirstChild();
                NodeList childOfNet = net.getChildNodes();
                System.out.println(childOfNet.item(0).getTextContent() + "\n");
                for (int j = 1; j < childOfNet.getLength(); j++) {
                    Node miniTask = childOfNet.item(j);
                    if ((miniTask.getNodeName().equals("page")) || (miniTask.getNodeName().equals("finalmarkings"))) {
                        if (miniTask.getNodeName().equals("page")) {
                            petriTaskPlace = miniTask.getChildNodes();
                            taskMap = petriTaskParse(petriTaskPlace);
                        }
                        if (miniTask.getNodeName().equals("finalmarkings")) {
                            petriFinalMarkings = miniTask.getChildNodes();
                            petriTaskPlace = null;
                        }
                    } else {
                        if (!miniTask.getNodeName().equals("#text")) {
                            System.err.println("PROGRAM FORGET:" + miniTask.getNodeName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskMap;
    }

    private Map<String, Task> petriTaskParse(NodeList petriTaskPlace) throws ParseException {
        String placeId, name, localNodeId, posX, posY, source, target;
        Map<String, Task> taskMap = new HashMap<String, Task>();
        ArrayList<String> usedTask = new ArrayList<>();
        for (int i = 1; i < petriTaskPlace.getLength(); i++) {
            Node petriNode = petriTaskPlace.item(i);
            if (!petriNode.getNodeName().equals("arc")) {
                placeId = String.valueOf(petriTaskPlace.item(i).getAttributes().getNamedItem("id"));
                placeId = placeId.substring(4, placeId.length() - 1);
                name = petriNode.getFirstChild().getTextContent();
                localNodeId = String.valueOf(findRightChildNode(petriNode, "toolspecific").getAttributes().getNamedItem("localNodeID"));
                localNodeId = localNodeId.substring(13, localNodeId.length() - 1);
                posX = findRightChildNode(petriNode, "graphics").getFirstChild().getAttributes().getNamedItem("x").toString();
                posX = posX.substring(3, posX.length() - 1);
                posY = findRightChildNode(petriNode, "graphics").getFirstChild().getAttributes().getNamedItem("y").toString();
                posY = posY.substring(3, posY.length() - 1);
                Task task = new Task(name, Float.parseFloat(posX), Float.parseFloat(posY), localNodeId, placeId);
                taskMap.put(placeId, task);
                usedTask.add(placeId);
            } else {
                if (petriNode.getNodeName().equals("arc")) {
                    Task taskForComplementTarget;
                    Task taskSource;

                    source = String.valueOf(petriNode.getAttributes().getNamedItem("source"));
                    target = String.valueOf(petriNode.getAttributes().getNamedItem("target"));
                    source = source.substring(8, source.length() - 1);
                    target = target.substring(8, target.length() - 1);

                    taskForComplementTarget = taskMap.get(source);
                    taskForComplementTarget.setTarget(taskMap.get(target));
                    taskMap.put(source, taskForComplementTarget);

                    taskSource = taskMap.get(target);
                    taskSource.setSource(source);
                    taskMap.put(target, taskSource);
                }
            }
        }
        return  new GateAwayParse(deleteNoName(taskMap, usedTask)).parseGateway();
    }

    private Map<String, Task> deleteNoName(Map<String, Task> taskMap, ArrayList<String> usedTask) {
        for (Map.Entry<String, Task> entry : taskMap.entrySet()) {
            if ((entry.getValue().getName().equals(String.valueOf((char) 34 + " " + (char) 34))) ||
                    (entry.getValue().getName().equals("t_start_")) ||
                    (entry.getValue().getName().equals("start_event__initial")) ||
                    (entry.getValue().getName().equals("end_event__ended")) ||
                    (entry.getValue().getName().equals("t_end_")) ||
                    (entry.getValue().getName().equals("o")) || (entry.getValue().getName().equals("i"))) {
                usedTask.remove(entry.getKey());
                // Узлу который идет ДО найденного проставляем таргет(цель) найденных
                for (String nodeName : entry.getValue().getSource()) {
                    Task earlyTask = taskMap.get(nodeName);
                    earlyTask.getTarget().remove(entry.getValue());
                    earlyTask.setTarget(entry.getValue().getTarget());
                }

                // Узлу который идет ПОСЛЕ найденного, проставляем сурсы(источники) найденного
                for (Task task : entry.getValue().getTarget()) {
                    Task afterTask = taskMap.get(task.getPlaceId());
                    afterTask.getSource().remove(entry.getValue().getPlaceId());
                    afterTask.setSource(entry.getValue().getSource());
                }

                taskMap.put(entry.getKey(), new Task());
            }
        }

        ArrayList<String> forDelete = new ArrayList<>();
        for (Map.Entry<String, Task> entry : taskMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                forDelete.add(entry.getKey());
            }
        }
        for (String key : forDelete) {
            taskMap.remove(key);
        }
        return taskMap;
    }


    private Node findRightChildNode(Node parentNode, String nameRightNode) throws ParseException {
        Node rightNode;
        for (int i = 0; i < parentNode.getChildNodes().getLength(); i++) {
            rightNode = parentNode.getChildNodes().item(i);
            if (rightNode.getNodeName().equals(nameRightNode)) {
                return rightNode;
            }
        }
        throw new ParseException("Required node not found!");
    }
}
