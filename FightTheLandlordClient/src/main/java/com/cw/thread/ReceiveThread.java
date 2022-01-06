package com.cw.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cw.model.Message;
import com.cw.model.Player;
import com.cw.model.Poker;
import com.cw.view.MainFrame;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// 接收消息线程
public class ReceiveThread  extends Thread {
    private Socket socket;
    private MainFrame mainFrame;
    public int status = 0; // 游戏状态
    public ReceiveThread() {
    }

    public ReceiveThread(Socket socket, MainFrame mainFrame) {
        this.socket = socket;
        this.mainFrame = mainFrame;
    }

    @Override
    public void run() {
//        super.run();
        try {
            // 接收消息
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            while (true){
                if(dataInputStream.available()>0){
                    // 接收服务端消息
                    String jsonString = dataInputStream.readUTF();
//                    System.out.println(jsonString);
                    List<Player> players = new ArrayList<>();
    //                System.out.println(jsonString);
                    // 解析json字符串
                    // json字符串转换为json数组
                    JSONArray playerJsonArray = JSONArray.parseArray(jsonString);
                    for (int i = 0; i < playerJsonArray.size(); i++) {
                        // 玩家对象
                        JSONObject playerJson = (JSONObject)playerJsonArray.get(i);
                        int id = playerJson.getInteger("id");
                        String name = playerJson.getString("name");
                        List<Poker> pokers = new ArrayList<>();
                        JSONArray pokerJsonArray = playerJson.getJSONArray("pokers");
                        for (int j = 0; j < pokerJsonArray.size(); j++) {
                            // 获得一个扑克对象
                            JSONObject pokerJson = (JSONObject)pokerJsonArray.get(j);
                            int pid = pokerJson.getInteger("id");
                            String pname = pokerJson.getString("name");
                            int num = pokerJson.getInteger("num");
                            boolean isOut = pokerJson.getBoolean("out");
                            Poker poker = new Poker(pid,pname,num,isOut);
                            pokers.add(poker);
                        }
                        Player player = new Player(id,name,pokers);
                        // 消息
                        Message message = new Message();
                        JSONObject messageJson = (JSONObject)playerJson.getJSONObject("message");
                        int type = messageJson.getInteger("type");
                        int time = messageJson.getInteger("time");
                        int ustatus = messageJson.getInteger("status");
                        message.setType(type);
                        message.setTime(time);
                        message.setStatus(ustatus);
                        List<Poker> handPokers = new ArrayList<>();
                        JSONArray handPokerJsonArray = messageJson.getJSONArray("pokers");
                        for (int j = 0; j < handPokerJsonArray.size(); j++) {
                            // 获得一个扑克对象
                            JSONObject handPokerJson = (JSONObject)handPokerJsonArray.get(j);
                            int pid = handPokerJson.getInteger("id");
                            String pname = handPokerJson.getString("name");
                            int num = handPokerJson.getInteger("num");
                            boolean isOut = handPokerJson.getBoolean("out");
                            Poker poker = new Poker(pid,pname,num,isOut);
                            handPokers.add(poker);
                        }
                        message.setPokers(handPokers);
                        player.setMessage(message);
                        players.add(player);
                    }

                    // 获得3个玩家信息了
                    if (players.size() == 3) {
                        mainFrame.setPlayers(players);
                        switch (status){
                            case 0:
                                // 显示
                                mainFrame.showAllPlayersInfo(players);
                                break;
                            case 1:
                                // 是否抢地主
                                mainFrame.robLandlord(players);
                                // 是否准备就绪
                                mainFrame.ready(players);
                                break;
                            case 2:
                                mainFrame.isCountdown = false;
                                // 获取底牌
                                mainFrame.showAllPlayersHand(players);
                                // 首次出牌
                                mainFrame.play(players);
                                status=3;
                                break;
                            case 3:
                                // 开始出牌
                                mainFrame.play(players);
                                break;
                            case 4:
                                mainFrame.isCountdown = false;
                                // 等待出牌
                                mainFrame.waiting(players);
                                break;
                            default:
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
