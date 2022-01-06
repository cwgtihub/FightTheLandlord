package com.cw.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private int type; // 消息类型 0：不叫 1:叫地主 2: 不抢 3:抢地主 4:都不抢 5:获取底牌准备就绪 6:出牌 7:不出 8:都不出继续出牌 9：获胜 10:输了
    private List<Poker> pokers = new ArrayList<>(); // 扑克出牌列表
    private int time; // 倒计时
    private int status; // 游戏进度

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Poker> getPokers() {
        return pokers;
    }

    public void setPokers(List<Poker> pokers) {
        this.pokers = pokers;
    }

    public Message() {
    }

    public Message(int type) {
        this.type = type;
    }

    public Message(int type, List<Poker> pokers) {
        this.type = type;
        this.pokers = pokers;
    }

    public Message(int type, int time) {
        this.type = type;
        this.time = time;
    }

    public Message(int type, int time, List<Poker> pokers) {
        this.type = type;
        this.time = time;
        this.pokers = pokers;
    }
}
