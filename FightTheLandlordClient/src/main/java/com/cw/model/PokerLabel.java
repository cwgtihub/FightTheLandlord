package com.cw.model;
import javax.swing.*;
import java.net.URL;

// 扑克标签
public class PokerLabel extends JLabel implements Comparable{
    private int id;
    private String name;
    private  int num;
    private boolean isOut;
    private boolean isUp;

    public PokerLabel(int id, String name, int num, boolean isOut) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.isOut = isOut;
        if(isUp){
            turnUp();
        }else{
            turnDown();
        }
        this.setSize(105,150);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    public PokerLabel() {
        this.setSize(105,150);
    }

    public PokerLabel(int id, String name, int num) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.setSize(105,150);
    }

    public PokerLabel(int id, String name, int num, boolean isOut, boolean isUp) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.isOut = isOut;
        this.isUp = isUp;
        if(isUp){
            turnUp();
        }else{
            turnDown();
        }
        this.setSize(105,150);
    }

    // 正面
    public void turnUp(){
//        URL path = PokerLabel.class.getResource("/images/poker/"+id+".jpg"); // jar包时
        String path = this.getClass().getClassLoader().getResource("").getPath()+"/images/poker/"+id+".jpg";
        this.setIcon(new ImageIcon(path));
    }

    // 反面
    public void turnDown(){
//        URL path = PokerLabel.class.getResource("/images/poker/down.jpg"); // jar包时
        String path = this.getClass().getClassLoader().getResource("").getPath()+"/images/poker/down.jpg";
        this.setIcon(new ImageIcon(path));
    }

    // 从大到小排序
    @Override
    public int compareTo(Object o) {
        PokerLabel pokerLabel = (PokerLabel) o;
        if(this.id<pokerLabel.id){
            return -1;
        }else if(this.id>pokerLabel.id){
            return 1;
        }else
            return 0;
    }
}
