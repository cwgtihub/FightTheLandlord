package com.cw.model;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// 玩家对象
public class Player {
    private int id; // 玩家id
    private String name; // 玩家名称
    private Socket socket; // 玩家socket连接
    private List<Poker> pokers = new ArrayList<>(); // 扑克列表
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

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

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public List<Poker> getPokers() {
        return pokers;
    }

    public void setPokers(List<Poker> pokers) {
        this.pokers = pokers;
    }

    public Player() {
    }

    public Player(String name) {
        this.name = name;
    }
    public Player(int id) {
        this.id = id;
    }

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Player(int id, String name, Socket socket) {
        this.id = id;
        this.name = name;
        this.socket = socket;
    }

    public Player(int id, String name, List<Poker> pokers) {
        this.id = id;
        this.name = name;
        this.pokers = pokers;
    }

    public Player(int id, String name, Socket socket, List<Poker> pokers) {
        this.id = id;
        this.name = name;
        this.socket = socket;
        this.pokers = pokers;
    }
}
