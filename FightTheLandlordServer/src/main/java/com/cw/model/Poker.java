package com.cw.model;
// 扑克对象
public class Poker {
    private int id; // id
    private String name; // 名称
    private int num; // 数值
    private boolean isOut; // 是否打出

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

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

    public Poker() {
    }

    public Poker(int id, String name, int num) {
        this.id = id;
        this.name = name;
        this.num = num;
    }

    public Poker(int id, String name, int num, boolean isOut) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.isOut = isOut;
    }
}
