package com.cw.view;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cw.model.Message;
import com.cw.model.Player;
import com.cw.model.Poker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MainFrame {
    // 创建玩家对象列表
    public List<Player> players = new ArrayList<>();
    // 玩家id自增
    public int index = 0;
    // 创建存放扑克列表
    public List<Poker> allPokers = new ArrayList<>();
    // 存放底牌
    public List<Poker> lordPokers = new ArrayList<>();
    // 是否开始游戏
    public boolean isStart = false;
    public int rob = 0; // 叫地主轮数
    public int robTime = 10; // 叫地主倒计时
    public int playTime = 30; // 倒计时
    public int time = 0; // 剩余倒计时
    public int status = 0; // 游戏状态 0 初始化 1 抢地主中 2 开始
    Countdown countdown = null; // 倒计时类
    public int playOut = 0; // 不出轮数
    public String landlord = ""; // 谁是地主
    // 重新开始
    public void restart(){
        if(countdown!=null){
            countdown.close();
        }
        landlord = "";
        status = 0;
        time = 0;
        allPokers = new ArrayList<>();
        lordPokers = new ArrayList<>();
        // 创建扑克列表
        createPokers();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setType(0);
            players.get(i).getMessage().setStatus(-1);
            players.get(i).getMessage().setTime(0);
            players.get(i).setPokers(new ArrayList<>());
        }
        rob = 0; // 叫地主轮数
        playOut = 0;
        // 发牌
        deal();
        try {
            Thread.sleep(2000);
            // 谁来叫
            robWho();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public MainFrame() {
        // 创建扑克列表
        createPokers();
        try {
            // 创建服务器端socket
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("服务器启动了");
            while (true){
                // 接收客户端socket
                Socket socket = serverSocket.accept();
                if(!isStart){
                    // 开启线程处理客户端socket
                    AcceptThread acceptThread = new AcceptThread(socket);
                    acceptThread.start();
                }else{
                    // 断开
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 创建一个接收线程 处理客户端信息
    class AcceptThread extends Thread{
        public boolean exit = false;
        Socket socket;
        // 最后的心跳时间
        long lastReceiveTime = System.currentTimeMillis();
        // 最大延迟时间
        long receiveTimeDelay = 6000;
        // 创建player对象
        Player player;
        public AcceptThread(Socket socket) {
            this.socket=socket;
        }

        @Override
        public void run() {
//            super.run();
            while (!exit){
                // 心跳检测
                if(System.currentTimeMillis()-lastReceiveTime>receiveTimeDelay){
                    // 移除玩家
                    if(player!=null && !isStart){
                        Iterator<Player> iterator = players.iterator();
                        while (iterator.hasNext()){
                            String next = iterator.next().toString();
                            if(next.equals(player.toString())){
                                iterator.remove();
                            }
                        }
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 退出当前线程
                    exit=true;
                }else{
                    try {
                        // 接收客户端消息
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        // 判断是否有数据
                        if(dataInputStream.available()>0){
                            String msg = dataInputStream.readUTF();
                            lastReceiveTime = System.currentTimeMillis();
                            if(!msg.equals("0")){
                                if(isJson(msg)){
                                    // json字符串转换为json数组
                                    JSONObject messageJson = JSONObject.parseObject(msg);
                                    int type = messageJson.getInteger("type");
                                    if(countdown!=null){
                                        countdown.close();
                                    }
                                    if(type==1||type==3){
                                        landlord = player.getName();
                                        // 给底牌
                                        hand(player.getName());
                                    }else if(type == 0 || type == 2){
                                        if(status==1){
                                            // 不叫/不抢
                                            robWho(player.getName());
                                        }
                                    }else if(type==4){
                                        // 都不抢
                                        // 重新开始
                                        restart();
                                    }else if(type==6){
                                        if(status==2){
                                            List<Poker> pokers = new ArrayList<>();
                                            JSONArray pokerJsonArray = messageJson.getJSONArray("pokers");
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
                                            // 出牌
                                            play(player.getName(),pokers);
                                        }
                                    }else if(type==7){
                                        if(status==2){
                                            // 不出
                                            notOut(player.getName());
                                        }
                                    }else if(type==9){
                                        if(status==2){
                                            List<Poker> pokers = new ArrayList<>();
                                            JSONArray pokerJsonArray = messageJson.getJSONArray("pokers");
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
                                            // 结束
                                            playEnd(player.getName(),pokers);
                                        }
                                    }
                                }else{
                                    for (int i = 0; i < players.size(); i++) {
                                        if(players.get(i).getName().equals(msg)){
                                            try {
                                                socket.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            // 退出当前线程
                                            exit=true;
                                        }
                                    }
                                    if(!exit){
                                        // 创建player对象
                                        player = new Player(index++,msg,socket);
                                        // 存入玩家列表
                                        players.add(player);
                                        System.out.println(msg + "上线了");
                                        System.out.println("当前上线人数："+players.size());
                                        // 玩家人数到齐,发给三个玩家
                                        if(players.size()==3){
                                            isStart = true;
                                            // 发牌
                                            deal();
                                            try {
                                                Thread.sleep(2000);
                                                // 谁来叫
                                                robWho();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    /**
     * 检查JSON数据合法性
     */
    public boolean isJson(String str) {
        try {
            JSONObject.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 创建扑克方法
    private void createPokers() {
        // 创建大小王
        Poker redJoker = new Poker(0,"大王",17,false);
        Poker blackJoker = new Poker(1,"小王",16,false);
        allPokers.add(redJoker);
        allPokers.add(blackJoker);
        // 创建其他扑克
        String[] names = {"2","A","K","Q","J","10","9","8","7","6","5","4","3"};
        String[] decors = {"黑桃","红桃","梅花","方块"};
        int id=2;
        int num=15;
        // 遍历扑克种类
        for (String name:names) {
            // 遍历扑克花色
            for (String decor:decors){
                Poker poker = new Poker(id++,decor+name,num,false);
                allPokers.add(poker);
            }
            num--;
        }
        // 洗牌
        Collections.shuffle(allPokers);
    }

    // 发牌方法
    private void deal() {
        // 发给三个玩家
        for (int i = 0; i < allPokers.size(); i++) {
            if(i>=51){
                // 底牌
                lordPokers.add(allPokers.get(i));
            }else{
               // 依次分发给玩家
               if(i%3==0){
                   players.get(0).getPokers().add(allPokers.get(i));
               } else if(i%3==1){
                   players.get(1).getPokers().add(allPokers.get(i));
               }else{
                   players.get(2).getPokers().add(allPokers.get(i));
               }
            }
        }
        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 谁来叫
    private void robWho(){
        // 开始叫地主
        status = 1;
        // 首个随机叫地主
        int index = (int)(Math.random()* players.size());
        Message message = players.get(index).getMessage();
        message.setType(1);
        message.setTime(robTime);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setStatus(status);
            if(i!=index){
                players.get(i).getMessage().setType(-1);
                players.get(i).getMessage().setTime(0);
            }
        }
        rob++;
        // 倒计时
        countdown = new Countdown(robTime,players.get(index).getName());
        countdown.start();
        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 谁来抢
    private void robWho(String uname){
        rob++;
        // 抢地主
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setStatus(status);
            players.get(i).getMessage().setType(-1);
            players.get(i).getMessage().setTime(0);
            if(players.get(i).getName().equals(uname)){
                index=i;
            }
        }
        if(index==players.size()-1){
            index=0;
        }else{
            index++;
        }
        Message message = players.get(index).getMessage();
        if(rob==3){
            // 抢地主
            status = 1;
            message.setType(4);
            message.setTime(robTime);
        }else{
            // 抢地主
            status = 1;
            message.setType(3);
            message.setTime(robTime);
        }
//        System.out.println(players.get(index).getName()+":"+rob+":"+robTime);
        // 倒计时
        countdown = new Countdown(robTime,players.get(index).getName());
        countdown.start();
        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // 给底牌
    private void hand(String uname) {
        status=2;
        Message message = new Message();
        message.setType(5);
        message.setTime(playTime);
        for (int j = 0; j < lordPokers.size(); j++) {
            // 获取底牌
            message.getPokers().add(lordPokers.get(j));
        }
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setStatus(status);
            players.get(i).getMessage().setType(-1);
            players.get(i).getMessage().setTime(0);
            if(players.get(i).getName().equals(uname)){
                index = i;
                for (int j = 0; j < lordPokers.size(); j++) {
                    // 获取底牌
                    players.get(i).getPokers().add(lordPokers.get(j));
                }
                players.get(i).setMessage(message);
            }
        }
        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if(countdown!=null){
                countdown.close();
            }
            Thread.sleep(1000);
            // 倒计时
            countdown = new Countdown(playTime,players.get(index).getName());
            countdown.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // 倒计时处理
    class Countdown extends Thread{
        public boolean isCountdown;
        public int time;
        public String uname;
        public Countdown(int time2,String uname) {
            this.time = time2;
            this.uname = uname;
            this.isCountdown = true;
        }

        @Override
        public void run() {
            if(rob>3){
//            System.out.println(rob);
                restart();
                interrupt();
            }else{
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        time--;
                        if(time<0){
                            time=0;
                            if(status==1){
                                robWho(uname);
                            }else if(status==2){
                                // 出牌或不出
                                for (int i = 0; i < players.size(); i++) {
                                    if(players.get(i).getName().equals(uname)){
                                        Message message = players.get(i).getMessage();
                                        // 必须出牌,出最小
                                        if(message.getType()==5||message.getType()==8){
                                            List<Poker> pokers = new ArrayList<>();
                                            int maxId = -1;
                                            int maxJ = -1;
                                            int surplus = -1;
                                            for (int j = 0; j < players.get(i).getPokers().size(); j++) {
                                                if(!players.get(i).getPokers().get(j).isOut()){
                                                    surplus++;
                                                    if(maxId<players.get(i).getPokers().get(j).getId()){
                                                        maxId = players.get(i).getPokers().get(j).getId();
                                                        maxJ = j;
                                                    }
                                                }
                                            }
                                            if(maxId!=-1){
                                                pokers.add(new Poker(players.get(i).getPokers().get(maxJ).getId(), players.get(i).getPokers().get(maxJ).getName(), players.get(i).getPokers().get(maxJ).getNum(), true));
                                                if((surplus-1)== 0){
                                                    playEnd(uname,pokers);
                                                }else{
//                                                System.out.println(uname+"出");
                                                    play(uname, pokers);
                                                }
                                            }
                                        }else{
//                                        System.out.println(uname+"不出");
                                            // 不出
                                            notOut(uname);
                                        }
                                    }
                                }
                            }
                            timer.cancel();
                            timer.purge();
                            interrupt();
                        }else{
                            if(!isCountdown){
                                timer.cancel();
                                timer.purge();
                                interrupt();
                            }
                        }
                    }
                },0, 1000);
            }
        }
        public void close(){
            isCountdown = false;
        }
    }

    // 出牌/通知下一个
    private void play(String uname, List<Poker> pokers) {
        playOut = 0;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setStatus(status);
            players.get(i).getMessage().setType(-1);
            players.get(i).getMessage().setTime(0);
            players.get(i).getMessage().setPokers(new ArrayList<>());
            if(players.get(i).getName().equals(uname)){
                index=i;
                players.get(i).getMessage().setPokers(pokers);
            }
        }
//        System.out.println(pokers.size());
        for (int i = 0; i <  players.get(index).getPokers().size(); i++) {
            for (int j = 0; j < pokers.size(); j++) {
                if(players.get(index).getPokers().get(i).getId()==pokers.get(j).getId()){
                    players.get(index).getPokers().get(i).setOut(pokers.get(j).isOut());
                    continue;
                }
            }
        }


        if(index==players.size()-1){
            index=0;
        }else{
            index++;
        }
        Message message = players.get(index).getMessage();
        message.setType(6);
        message.setTime(playTime);
//        System.out.println(players.get(index).getName()+":"+rob+":"+robTime);

        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(countdown!=null){
            countdown.close();
        }
        // 倒计时
        countdown = new Countdown(playTime,players.get(index).getName());
        countdown.start();
    }


    // 不出/通知下一个
    private void notOut(String uname) {
        playOut++;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setStatus(status);
            players.get(i).getMessage().setType(-1);
            players.get(i).getMessage().setTime(0);
            if(players.get(i).getName().equals(uname)){
                index=i;
                List<Poker> pokers = new ArrayList<>();
                pokers.add(new Poker(-1, "不出", -1, true));
                players.get(i).getMessage().setPokers(pokers);
            }
        }
        if(index==players.size()-1){
            index=0;
        }else{
            index++;
        }
        Message message = players.get(index).getMessage();
        if(playOut==2){
            playOut=0;
            message.setType(8);
        }else{
            message.setType(6);
        }
        message.setTime(playTime);
//        System.out.println(players.get(index).getName()+":"+rob+":"+robTime);
        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(countdown!=null){
            countdown.close();
        }
        // 倒计时
        countdown = new Countdown(playTime,players.get(index).getName());
        countdown.start();
    }

    // 结束
    private void playEnd(String uname, List<Poker> pokers) {
        int index = 0;
        // 是否地主获胜
        boolean isLandlordWin = landlord.equals(uname);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getMessage().setStatus(status);
            players.get(i).getMessage().setTime(0);
            players.get(i).getMessage().setPokers(new ArrayList<>());
            if(players.get(i).getName().equals(uname)){
                // 获胜
                players.get(i).getMessage().setType(9);
                index=i;
                players.get(i).getMessage().setPokers(pokers);
            }else{
                // 判断是否是地主获胜
                if(isLandlordWin){
                    players.get(i).getMessage().setType(10);
                }else{
                    // 判断当前用户是否不是地主
                    if(!landlord.equals(players.get(i).getName())){
                        players.get(i).getMessage().setType(9);
                    }else{
                        players.get(i).getMessage().setType(10);
                    }
                }
            }
        }

        for (int i = 0; i <  players.get(index).getPokers().size(); i++) {
            for (int j = 0; j < pokers.size(); j++) {
                if(players.get(index).getPokers().get(i).getId()==pokers.get(j).getId()){
                    players.get(index).getPokers().get(i).setOut(pokers.get(j).isOut());
                    continue;
                }
            }
        }

        // 将玩家的信息发送客户端
        for (int i = 0; i < players.size(); i++) {
            try {
                // 当前玩家数据流
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                // 转换json字符串
                String jsonString = JSON.toJSONString(players);
                // 发送消息
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
