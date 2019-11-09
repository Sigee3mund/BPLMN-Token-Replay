package ru.pod.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task {
    private String name;
    private String placeId;
    private float x;
    private float y;
    private int m=0,mReal=0,p=0,pReal=0,r=0,rReal=0,c=0,cReal=0;
    private String localNodeId;
    private List<Task> target=new ArrayList<>();
    private List<String> source=new ArrayList<>();
    private double nestedFitness = -0.1;

    public Task() {
    }

    public Task(String name, float x, float y, String localNodeId, String placeId) {
        this.name=name;
        this.x = x;
        this.y = y;
        this.localNodeId=localNodeId;
        this.placeId=placeId;
        target.clear();
        source.clear();
    }

    public void setTarget(Task task) {
        if (!target.contains(task)){
            target.add(task);
        }
    }

    public void setTarget(List<Task> tasks) {
        for (Task task:tasks){
            setTarget(task);
        }
    }

    public List<Task> getTarget() {
        return target;
    }

    public List<String> getSource() {

        return source;
    }

    public void setSource(String source) {
        if (!this.source.contains(source)){
            this.source.add(source);}
    }

    public void setSource(List<String> source) {
        for (String oneSource:source){
            setSource(oneSource);
        }
    }

    public void setNewSource(){
        this.source.clear();
    }

    public String getLocalNodeId() {
        return localNodeId;
    }

    public String getName() {
        if (name.isEmpty()){
            return String.valueOf((char) 34+" "+(char) 34);
        }else{
        return name;
        }
    }

    public String getPlaceId() {
        return placeId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }


    public void setNewTarget(){
        target.clear();
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public void addM(){
        this.m=m+1;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
        setpReal(this.p);
    }

    public void addP(){
        this.p=p+1;
        addPReal();
    }

    public void incP(){
        this.p=p-1;
    }

    public int getpReal() {
        return pReal;
    }

    private void addPReal() {
        this.pReal = pReal+1;
    }

    private void setpReal(int pReal) {
        this.pReal = pReal;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
        setrReal(r);
    }

    public void addR(){
        this.r=r+1;
        addrReal();
    }

    public void incR(){
        this.r=r-1;
        setrReal(getrReal()-1);
    }

    public int getrReal() {
        return rReal;
    }

    private void setrReal(int rReal) {
        this.rReal = rReal;
    }

    private void addrReal() {
        this.rReal = rReal+1;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
        setcReal(this.c);
    }

    public void addC(){
        this.c=c+1;
        addcReal();
    }

    public void incC(){
        this.c=c-1;
    }

    public int getcReal() {
        return cReal;
    }

    private void setcReal(int cReal) {
        this.cReal = cReal;
    }

    private void addcReal() {
        this.cReal = cReal+1;
    }

    public double getNestedFitness() {
        return nestedFitness;
    }

    public void setNestedFitness(double nestedFitness) {
        this.nestedFitness = nestedFitness;
    }

    @Override
    public String toString() {
        String targets="";
        String sources="";
        for(Task task:target) {
            targets = targets + task.getName() + " " + task.getPlaceId() + ";";
        }
        for(String task:source) {
            sources =sources+task + " ,";
        }

        if (targets.equals("")){
            targets="THE END";
        }
        if (sources.equals("")){
            sources="START";
        }
            return "Task{" +
                    "name= " + name +
                    ", placeId= " + placeId  +
                    ", x=" + x +
                    ", y=" + y +
                    ", localNodeId= " + localNodeId  +
                    ", source= " + sources  +
                    " target="+targets+
                    "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Float.compare(task.x, x) == 0 &&
                Float.compare(task.y, y) == 0 &&
                Objects.equals(name, task.name) &&
                Objects.equals(placeId, task.placeId) &&
                Objects.equals(localNodeId, task.localNodeId) &&
                Objects.equals(target, task.target) &&
                Objects.equals(source, task.source);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, placeId, x, y, m, p, r, c, localNodeId, target, source);
    }

    public boolean isEmpty(){
        return this.equals(new Task());
    }

    public void cleanToken(){
        this.cReal=0;
        this.pReal=0;
        this.mReal=0;
        this.rReal=0;
        this.m=0;
        this.p=0;
        this.r=0;
        this.c=0;
    }

}
