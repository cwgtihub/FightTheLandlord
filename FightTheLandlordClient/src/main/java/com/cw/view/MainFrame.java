package com.cw.view;

import com.alibaba.fastjson.JSON;
import com.cw.model.Message;
import com.cw.model.Player;
import com.cw.model.Poker;
import com.cw.model.PokerLabel;
import com.cw.thread.ReceiveThread;
import com.cw.thread.SendThread;
import com.cw.utils.GameUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class MainFrame extends JFrame {
    public MyPanel myPanel;
    public List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    // 倒计时之前的数据
    public List<Player> beforePlayers;

    public String uname;
    public Socket socket;
    public SendThread sendThread;
    public ReceiveThread receiveThread;
    public Player currentPlayer; // 存放当前玩家对象
    public List<PokerLabel> pokerLabels = new ArrayList<>(); // 存放扑克标签列表
    public List<PokerLabel> outPokerLabels = new ArrayList<>(); // 存放要出的扑克标签列表
    public JLabel label1 = null; // 叫/抢地主
    public JLabel label2 = null; // 不叫/不抢
    public JLabel label3 = null; // 出牌
    public JLabel label4 = null; // 不出
    public JLabel label5 = null; // 不出展示
    public boolean isStart = false; // 是否开始游戏
    Countdown countdown = null; // 倒计时
    public boolean isCountdown = false; // 显示倒计时
    public int countdownX = 0; // 倒计时X
    public int countdownY = 0; // 倒计时Y
    public int time = 0; // 倒计时剩余时间
    public List<JLabel> leftLabels = new ArrayList<>(); // 存放左边的展示标签列表
    public List<JLabel> rightLabels = new ArrayList<>(); // 存放右边的展示标签列表
    public boolean isPlay = false; // 是否可以出牌
    public MainFrame(String uname, Socket socket){
        this.uname = uname;
        this.socket = socket;

        // 设置窗口属性
        this.setSize(1200,700);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 添加myPanel
        myPanel=new MyPanel();
        myPanel.setMainFrame(this);
        myPanel.setBounds(0, 0, 1200, 700);
        this.add(myPanel);
        // 启动发送消息线程
        sendThread=new SendThread(socket,uname);
        sendThread.start();
        // 启动接收消息线程
        receiveThread=new ReceiveThread(socket,this);
        receiveThread.start();

    }

    // 显示
    public void showAllPlayersInfo(List<Player> players) {
        isCountdown = false;
        if(countdown!=null){
            countdown.close();
        }
        // 显示玩家名称
        isStart = true;
        myPanel.repaint();
        // 显示当前玩家的扑克列表
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getName().equals(uname)){
                currentPlayer=players.get(i);
            }
        }
        List<Poker> pokers = currentPlayer.getPokers();
        for (int i = 0; i < pokers.size(); i++) {
            // 创建扑克标签
            Poker poker = pokers.get(i);
            PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
            // 显示正面
            pokerLabel.turnUp();
            // 添加到面板中
            this.myPanel.add(pokerLabel);
            this.pokerLabels.add(pokerLabel);
            // 动态显示出来
            this.myPanel.setComponentZOrder(pokerLabel, 0);
            // 一张一张的显示出来
            GameUtil.move(pokerLabel, 300+(30*i), 450);
        }

        // 对扑克列表排序
        Collections.sort(pokerLabels);
        // 重新移动位置
        for (int i = 0; i < pokerLabels.size(); i++) {
            // 动态显示出来
            this.myPanel.setComponentZOrder(pokerLabels.get(i), 0);
            // 一张一张的显示出来
            GameUtil.move(pokerLabels.get(i), 300+(30*i), 450);
        }
        receiveThread.status=1;
        myPanel.repaint();
    }

    // 抢地主方法
    public void robLandlord(List<Player> players) {
        isCountdown = false;
        if(countdown!=null){
            countdown.close();
        }
        if(label1!=null){
            myPanel.remove(label1);
        }
        if(label2!=null){
            myPanel.remove(label2);
            myPanel.revalidate();
            myPanel.repaint();
        }
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getName().equals(uname)){
                currentPlayer=players.get(i);
            }
        }
        int status = currentPlayer.getMessage().getStatus();
//        System.out.println(status);
        // 重新开始
        if (status == -1) {
            isStart = false;
            myPanel.removeAll();
            pokerLabels = new ArrayList<>();
            // 开始游戏状态
            receiveThread.status = 0;
            myPanel.revalidate();
            myPanel.repaint();
            this.setPlayers(null);
            return;
        }
        int type = currentPlayer.getMessage().getType();
        String jtext1 = "叫地主";
        String jtext2 = "不叫";
        // 是否抢地主
        if(type==3||type==4){
            jtext1 = "抢地主";
            jtext2 = "不抢";
        }
        if(type==1||type==3||type==4){
            label1 = new JLabel(jtext1,JLabel.CENTER);
            GameUtil.jLabelBtn(label1,300 , 400,Color.green);
            this.myPanel.add(label1);
            label2 = new JLabel(jtext2,JLabel.CENTER);
            GameUtil.jLabelBtn(label2,420 , 400,Color.green);
            this.myPanel.add(label2);
            label1.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    isCountdown = false;
                    if(countdown!=null){
                        countdown.close();
                    }
                    // 叫地主
                    if(type==1){
                        // 开始游戏状态
                        receiveThread.status = 2;
                        Message message = new Message();
                        message.setType(1);
                        String jsonString = JSON.toJSONString(message);
                        sendThread.setMsg(jsonString);
                    }else if(type==3||type==4){
                        // 抢地主
                        // 开始游戏状态
                        receiveThread.status = 2;
                        Message message = new Message();
                        message.setType(3);
                        String jsonString = JSON.toJSONString(message);
                        sendThread.setMsg(jsonString);
                    }
                    myPanel.remove(label1);
                    myPanel.remove(label2);
                    myPanel.revalidate();
                    myPanel.repaint();
                }
            });
            label2.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    isCountdown = false;
                    if(countdown!=null){
                        countdown.close();
                    }
                    // 不叫
                    if(type==1){
                        Message message = new Message();
                        message.setType(0);
                        String jsonString = JSON.toJSONString(message);
                        sendThread.setMsg(jsonString);
                    }else if(type==3){
                        // 不抢
                        Message message = new Message();
                        message.setType(2);
                        String jsonString = JSON.toJSONString(message);
                        sendThread.setMsg(jsonString);
                    }else if(type==4){
                        // 不抢
                        isStart = false;
                        myPanel.removeAll();
                        pokerLabels = new ArrayList<>();
                        // 开始游戏状态
                        receiveThread.status = 0;
                        Message message = new Message();
                        message.setType(4);
                        String jsonString = JSON.toJSONString(message);
                        sendThread.setMsg(jsonString);
                        myPanel.revalidate();
                        setPlayers(null);
                        myPanel.repaint();
                    }
                    if(type!=4){
                        myPanel.remove(label1);
                        myPanel.remove(label2);
                        myPanel.revalidate();
                        myPanel.repaint();
                    }
                }
            });
            countdownX=540;
            countdownY=430;
            if(countdown!=null){
                countdown.close();
            }
            isCountdown = true;
            time = currentPlayer.getMessage().getTime();
            // 倒计时
            countdown = new Countdown(players,currentPlayer,0);
            countdown.start();
        }
        myPanel.repaint();
    }

    // 非地主准备
    public void ready(List<Player> players) {
        int index = -1;
        int index2 = 0;
        // 显示当前玩家的扑克列表
        for (int i = 0; i < players.size(); i++) {
            // 地主
            if(players.get(i).getMessage().getType()==5){
                index=i;
            }
            // 当前玩家
            if(players.get(i).getName().equals(uname)) {
                currentPlayer = players.get(i);
                index2 = i;
            }
        }
        if(index!=-1 && index!=index2){
            int x1=110,x2=1000,y=70;
            // 当前是第一个玩家
            if(index2 == 0){
                // 右边
                if(index==1){
                    // 地主图标
                    JLabel landlord = new JLabel("地主",JLabel.CENTER);
                    GameUtil.jLabelBtn(landlord,x2 , y,Color.red);
                    myPanel.add(landlord);
                }else{
                    // 左边
                    // 地主图标
                    JLabel landlord = new JLabel("地主",JLabel.CENTER);
                    GameUtil.jLabelBtn(landlord,x1 , y,Color.red);
                    myPanel.add(landlord);
                }
            }else if(index2 == 1){
                // 右边
                if(index==2){
                    // 地主图标
                    JLabel landlord = new JLabel("地主",JLabel.CENTER);
                    GameUtil.jLabelBtn(landlord,x2 , y,Color.red);
                    myPanel.add(landlord);
                }else{
                    // 左边
                    // 地主图标
                    JLabel landlord = new JLabel("地主",JLabel.CENTER);
                    GameUtil.jLabelBtn(landlord,x1 , y,Color.red);
                    myPanel.add(landlord);
                }
            }else{
                // 当前是最后一个玩家
                // 右边
                if(index==0){
                    // 地主图标
                    JLabel landlord = new JLabel("地主",JLabel.CENTER);
                    GameUtil.jLabelBtn(landlord,x2 , y,Color.red);
                    myPanel.add(landlord);
                }else{
                    // 左边
                    // 地主图标
                    JLabel landlord = new JLabel("地主",JLabel.CENTER);
                    GameUtil.jLabelBtn(landlord,x1 , y,Color.red);
                    myPanel.add(landlord);
                }
            }
            for (int i = 0; i < pokerLabels.size(); i++) {
                // 添加事件
                pokerLabelLevent(pokerLabels.get(i));
            }
            if(index==players.size()-1){
                index=0;
            }else{
                index++;
            }
            if(currentPlayer.getName().equals(players.get(index).getName())){
                // 准备出牌
                receiveThread.status=3;
            }else{
                // 等待出牌
                receiveThread.status=4;
            }
        }
        myPanel.repaint();
    }

    // 获取底牌
    public void showAllPlayersHand(List<Player> players) {
        // 显示当前玩家的扑克列表
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getName().equals(uname)){
                currentPlayer=players.get(i);
                // 地主图标
                JLabel landlord = new JLabel("地主",JLabel.CENTER);
                GameUtil.jLabelBtn(landlord,180 , 450,Color.red);
                myPanel.add(landlord);
            }
        }
        if(currentPlayer.getMessage().getType()==5){
            List<Poker> pokers = currentPlayer.getMessage().getPokers();
            for (int i = 0; i < pokers.size(); i++) {
                // 创建扑克标签
                Poker poker = pokers.get(i);
                PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                // 显示正面
                pokerLabel.turnUp();
                // 添加到面板中
                this.myPanel.add(pokerLabel);
                this.pokerLabels.add(pokerLabel);
                GameUtil.move(pokerLabel, 400+(105*i), 250);
            }

            // 对扑克列表排序
            Collections.sort(pokerLabels);
            // 重新移动位置
            for (int i = 0; i < pokerLabels.size(); i++) {
                // 添加事件
                pokerLabelLevent(pokerLabels.get(i));
                // 动态显示出来
                this.myPanel.setComponentZOrder(pokerLabels.get(i), 0);
                // 一张一张的显示出来
                GameUtil.move(pokerLabels.get(i), 300+(30*i), 450);
            }
        }
        myPanel.revalidate();
        myPanel.repaint();
    }

    // 倒计时处理
    class Countdown extends Thread{
        public boolean isCountdown2;
        public List<Player> players;
        public Player currentPlayer;
        public  int isStart;
        public Countdown(List<Player> players, Player currentPlayer, int i) {
            this.players = players;
            this.currentPlayer = currentPlayer;
            this.isStart = i;
            this.isCountdown2 = true;
            myPanel.repaint();
        }
        @Override
        public void run() {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    time--;
                    if(time<=0){
                        if(isStart==1 && receiveThread.status!=4){
                            // 结束状态
                            receiveThread.status = 4;
                        }
                        time = 0;
                        isCountdown = false;
                        if (label1 != null) {
                            myPanel.remove(label1);
                        }
                        if (label2 != null) {
                            myPanel.remove(label2);
                        }
                        if(label3!=null){
                            myPanel.remove(label3);
                        }
                        if(label4!=null){
                            myPanel.remove(label4);
                        }

                        myPanel.revalidate();
                        myPanel.repaint();
                        // 开始出牌倒计时
                        if(isStart==1){
                            boolean isPlay2 = false;
                            for (int i = 0; i < players.size(); i++) {
                                if(players.get(i).getMessage().getType()==5 || players.get(i).getMessage().getType()==8){
                                    isPlay2 = true;
                                }
                            }
                            if(isPlay2){
                                // 出最小一张牌
                                PokerLabel pokerLabel = pokerLabels.get(pokerLabels.size()-1);
                                outPokerLabels.add(pokerLabel);
                                Collections.sort(outPokerLabels);
                                for (int i = 0; i < outPokerLabels.size(); i++) {
                                    // 动态显示出来
                                    myPanel.setComponentZOrder(outPokerLabels.get(i), 0);
                                    GameUtil.move(outPokerLabels.get(i), 400, 300);
                                    pokerLabels.remove(outPokerLabels.get(i));
                                }
                                // 重新移动位置
                                for (int i = 0; i < pokerLabels.size(); i++) {
                                    myPanel.setComponentZOrder(pokerLabels.get(i), 0);
                                    // 一张一张的显示出来
                                    GameUtil.move(pokerLabels.get(i), 300+(30*i), 450);
                                }
//                                    System.out.println(uname+"出");
                                if(pokerLabels.size()==0){
//                                        JOptionPane.showMessageDialog(null, "赢了", "信息", JOptionPane.INFORMATION_MESSAGE);
                                    // 状态
                                    receiveThread.status = 0;
                                }
                            }else{
//                                    System.out.println(uname+"不出");
                                for (int i = 0; i < outPokerLabels.size(); i++) {
                                    GameUtil.move(outPokerLabels.get(i), outPokerLabels.get(i).getX(), 450);
                                }
                                outPokerLabels = new ArrayList<>();
                                label5 = new JLabel("不出",JLabel.CENTER);
                                GameUtil.jLabelBtn(label5,450 , 400,Color.orange);
                                myPanel.add(label5);
                            }
                            isPlay=false;
                            myPanel.revalidate();
                            myPanel.repaint();
                        }
                        timer.cancel();
                        timer.purge();
                        interrupt();
                    }else{
                        if(!isCountdown2){
                            timer.cancel();
                            timer.purge();
                            interrupt();
                        }
                        myPanel.revalidate();
                        myPanel.repaint();
                    }
                }
            },0, 1000);
        }

        public void close(){
            time = 0;
            this.isCountdown2 = false;
            isCountdown = false;
        }
    }

    // 纸牌事件
    public void pokerLabelLevent(PokerLabel pokerLabel) {
        pokerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = pokerLabel.getX();
                int y = pokerLabel.getY();
                // 判断是否可以出牌
                if(isPlay){
//                    System.out.println(y);
                    if(y==450){
                        outPokerLabels.add(pokerLabel);
                        GameUtil.move(pokerLabel, x, 425);
                    }else{
                        outPokerLabels.remove(pokerLabel);
                        GameUtil.move(pokerLabel, x, 450);
                    }
                }
            }
        });
    }


    // 出牌方法
    public void play(List<Player> players) {
        if (label4!=null){
            myPanel.remove(label4);
        }
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getName().equals(uname)){
                currentPlayer = players.get(i);
                index = i;
            }
        }
        for (int i = 0; i < outPokerLabels.size(); i++) {
            myPanel.remove(outPokerLabels.get(i));
        }
        myPanel.revalidate();
        myPanel.repaint();
        outPokerLabels = new ArrayList<>();
        if(currentPlayer.getMessage().getType()!=5){
            // 展示
            exhibition(players,index);
        }

//        System.out.println(uname+":p"+currentPlayer.getMessage().getType());
        // 判断是否出牌
        if(currentPlayer.getMessage().getType()==5 || currentPlayer.getMessage().getType()==6 || currentPlayer.getMessage().getType()==8){
            if(label5!=null){
                myPanel.remove(label5);
            }
            isPlay=true;
            label3 = new JLabel("出牌",JLabel.CENTER);
            GameUtil.jLabelBtn(label3,300 , 350,Color.blue);
            this.myPanel.add(label3);
            label4 = new JLabel("不出",JLabel.CENTER);
            GameUtil.jLabelBtn(label4,420 , 350,Color.orange);
            this.myPanel.add(label4);
            label3.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
//                super.mousePressed(e);
                    // 出牌
                    boolean verifyPlay = false;
                    if(outPokerLabels.size()!=0){
                        Collections.sort(outPokerLabels);
                        // 验证出牌
                        verifyPlay = verifyPlay(outPokerLabels,players);
                    }
                    if(verifyPlay){
                        List<Poker> pokers = new ArrayList<>();
                        for (int i = 0; i < outPokerLabels.size(); i++) {
                            // 动态显示出来
                            myPanel.setComponentZOrder(outPokerLabels.get(i), 0);
                            GameUtil.move(outPokerLabels.get(i), 400+(30*i), 300);
                            pokerLabels.remove(outPokerLabels.get(i));
                            pokers.add(new Poker(outPokerLabels.get(i).getId(), outPokerLabels.get(i).getName(), outPokerLabels.get(i).getNum(), true));
                        }
                        // 重新移动位置
                        for (int i = 0; i < pokerLabels.size(); i++) {
                            myPanel.setComponentZOrder(pokerLabels.get(i), 0);
                            // 一张一张的显示出来
                            GameUtil.move(pokerLabels.get(i), 300+(30*i), 450);
                        }
                        myPanel.remove(label3);
                        myPanel.remove(label4);
                        // 获胜
                        if(pokerLabels.size()==0){
                            // 结束状态
                            receiveThread.status = 4;
                            isPlay=false;
                            Message message = new Message();
                            message.setType(9);
                            message.setPokers(pokers);
                            String jsonString = JSON.toJSONString(message);
                            sendThread.setMsg(jsonString);
                        }else{
                            // 结束状态
                            receiveThread.status = 4;
                            isPlay=false;
                            Message message = new Message();
                            message.setType(6);
                            message.setPokers(pokers);
                            String jsonString = JSON.toJSONString(message);
                            sendThread.setMsg(jsonString);
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "请按规则出牌", "信息", JOptionPane.INFORMATION_MESSAGE);
                        for (int i = 0; i < outPokerLabels.size(); i++) {
                            GameUtil.move(outPokerLabels.get(i), outPokerLabels.get(i).getX(), 450);
                        }
                        outPokerLabels = new ArrayList<>();
                    }
                    myPanel.revalidate();
                    myPanel.repaint();
                }
            });
            label4.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
//                super.mousePressed(e);
                    // 不出
                    if(currentPlayer.getMessage().getType()==6){
                        for (int i = 0; i < outPokerLabels.size(); i++) {
                            GameUtil.move(outPokerLabels.get(i), outPokerLabels.get(i).getX(), 450);
                        }
                        outPokerLabels = new ArrayList<>();
                        myPanel.remove(label3);
                        myPanel.remove(label4);
                        label5 = new JLabel("不出",JLabel.CENTER);
                        GameUtil.jLabelBtn(label5,450 , 400,Color.orange);
                        myPanel.add(label5);
                        // 结束状态
                        receiveThread.status = 4;
                        isPlay=false;
                        Message message = new Message();
                        message.setType(7);
                        List<Poker> pokers = new ArrayList<>();
                        pokers.add(new Poker(-1, "不出", -1, true));
                        message.setPokers(pokers);
                        String jsonString = JSON.toJSONString(message);
                        sendThread.setMsg(jsonString);
                    }else {
                        JOptionPane.showMessageDialog(null, "请出牌", "信息", JOptionPane.INFORMATION_MESSAGE);
                    }
                    myPanel.revalidate();
                    myPanel.repaint();
                }
            });

            countdownX=540;
            countdownY=380;
            isCountdown = false;
            if(countdown!=null){
                countdown.close();
            }
            isCountdown = true;
            time = currentPlayer.getMessage().getTime();
            // 倒计时
            countdown = new Countdown(players,currentPlayer,1);
            countdown.start();
        }


        if(currentPlayer.getMessage().getType()==9){
            JOptionPane.showMessageDialog(null, "赢了", "信息", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("pw");
//            receiveThread.status=0;
        }else if(currentPlayer.getMessage().getType()==10){
            JOptionPane.showMessageDialog(null, "输了", "信息", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("pf");
//            receiveThread.status=0;
        }
        myPanel.revalidate();
        myPanel.repaint();
    }

    // 等待出牌
    public void waiting(List<Player> players) {
        if (label4!=null){
            myPanel.remove(label4);
        }
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getName().equals(uname)){
                currentPlayer = players.get(i);
                index = i;
            }
            if(players.get(i).getMessage().getType()==5||players.get(i).getMessage().getType()==6||players.get(i).getMessage().getType()==8){
                receiveThread.status=3;
            }
        }
        // 展示
        exhibition(players,index);

        System.out.println("w:"+uname + receiveThread.status);

        if(currentPlayer.getMessage().getType()==9){
            JOptionPane.showMessageDialog(null, "赢了", "信息", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("ww");
//            receiveThread.status=0;
        }else if(currentPlayer.getMessage().getType()==10){
            JOptionPane.showMessageDialog(null, "输了", "信息", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("wf");
//            receiveThread.status=0;
        }
        myPanel.revalidate();
        myPanel.repaint();
    }

    // 展示出牌
    public void exhibition(List<Player> players,int index) {
//        System.out.println("p:"+uname);
        for (int i = 0; i < leftLabels.size(); i++) {
            myPanel.remove(leftLabels.get(i));
        }
        for (int i = 0; i < rightLabels.size(); i++) {
            myPanel.remove(rightLabels.get(i));
        }
        myPanel.revalidate();
        myPanel.repaint();
        leftLabels = new ArrayList<>();
        rightLabels = new ArrayList<>();
        int x1=150,x2=800,y=125;

        // 当前是第一个玩家
        if(index == 0){
            // 右边
            List<Poker> pokersR = players.get(index + 1).getMessage().getPokers();
            if(pokersR.size()>10){
                x2 = x2 - 105 - (pokersR.size())*30;
            }else {
                x2 = x2 - 105 + (10 - pokersR.size())*30;
            }
            for (int i = 0; i < pokersR.size(); i++) {
                // 创建扑克标签
                Poker poker =  pokersR.get(i);
                if(poker.getId()!=-1){
                    PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                    // 显示正面
                    pokerLabel.turnUp();
                    myPanel.add(pokerLabel);
                    rightLabels.add(pokerLabel);
                    myPanel.setComponentZOrder(pokerLabel, 0);
                    // 一张一张的显示出来
                    GameUtil.move(pokerLabel, x2+(30*i), y);
                }else{
                    JLabel labelr = new JLabel("不出",JLabel.CENTER);
                    GameUtil.jLabelBtn(labelr,x2 , y,Color.orange);
                    myPanel.add(labelr);
                    rightLabels.add(labelr);
                }
            }

            // 左边
            List<Poker> pokersL = players.get(index+2).getMessage().getPokers();
            for (int i = 0; i < pokersL.size(); i++) {
                // 创建扑克标签
                Poker poker =  pokersL.get(i);
                if(poker.getId()!=-1){
                    PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                    // 显示正面
                    pokerLabel.turnUp();
                    myPanel.add(pokerLabel);
                    leftLabels.add(pokerLabel);
                    myPanel.setComponentZOrder(pokerLabel, 0);
                    // 一张一张的显示出来
                    GameUtil.move(pokerLabel, x1+(30*i), y);
                }else{
                    JLabel labell = new JLabel("不出",JLabel.CENTER);
                    GameUtil.jLabelBtn(labell,x1 , y,Color.orange);
                    myPanel.add(labell);
                    leftLabels.add(labell);
                }
            }

        }else if(index == 1){
            // 右边
            List<Poker> pokersR = players.get(index + 1).getMessage().getPokers();
            if(pokersR.size()>10){
                x2 = x2 - 105 - (pokersR.size())*30;
            }else {
                x2 = x2 - 105 + (10 - pokersR.size())*30;
            }
            for (int i = 0; i < pokersR.size(); i++) {
                // 创建扑克标签
                Poker poker =  pokersR.get(i);
                if(poker.getId()!=-1){
                    PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                    // 显示正面
                    pokerLabel.turnUp();
                    myPanel.add(pokerLabel);
                    rightLabels.add(pokerLabel);
                    myPanel.setComponentZOrder(pokerLabel, 0);
                    // 一张一张的显示出来
                    GameUtil.move(pokerLabel, x2+(30*i), y);
                }else{
                    JLabel labelr = new JLabel("不出",JLabel.CENTER);
                    GameUtil.jLabelBtn(labelr,x2 , y,Color.orange);
                    myPanel.add(labelr);
                    rightLabels.add(labelr);
                }
            }

            // 左边
            List<Poker> pokersL = players.get(index-1).getMessage().getPokers();
            for (int i = 0; i < pokersL.size(); i++) {
                // 创建扑克标签
                Poker poker =  pokersL.get(i);
                if(poker.getId()!=-1){
                    PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                    // 显示正面
                    pokerLabel.turnUp();
                    myPanel.add(pokerLabel);
                    leftLabels.add(pokerLabel);
                    myPanel.setComponentZOrder(pokerLabel, 0);
                    // 一张一张的显示出来
                    GameUtil.move(pokerLabel, x1+(30*i), y);
                }else{
                    JLabel labell = new JLabel("不出",JLabel.CENTER);
                    GameUtil.jLabelBtn(labell,x1 , y,Color.orange);
                    myPanel.add(labell);
                    leftLabels.add(labell);
                }
            }

        }else{
            // 当前是最后一个玩家
            // 右边
            List<Poker> pokersR = players.get(index-2).getMessage().getPokers();
            if(pokersR.size()>10){
                x2 = x2 - 105 - (pokersR.size())*30;
            }else {
                x2 = x2 - 105 + (10 - pokersR.size())*30;
            }
            for (int i = 0; i < pokersR.size(); i++) {
                // 创建扑克标签
                Poker poker =  pokersR.get(i);
                if(poker.getId()!=-1){
                    PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                    // 显示正面
                    pokerLabel.turnUp();
                    myPanel.add(pokerLabel);
                    rightLabels.add(pokerLabel);
                    myPanel.setComponentZOrder(pokerLabel, 0);
                    // 一张一张的显示出来
                    GameUtil.move(pokerLabel, x2+(30*i), y);
                }else{
                    JLabel labelr = new JLabel("不出",JLabel.CENTER);
                    GameUtil.jLabelBtn(labelr,x2 , y,Color.orange);
                    myPanel.add(labelr);
                    rightLabels.add(labelr);
                }
            }

            // 左边
            List<Poker> pokersL = players.get(index-1).getMessage().getPokers();
            for (int i = 0; i < pokersL.size(); i++) {
                // 创建扑克标签
                Poker poker =  pokersL.get(i);
                if(poker.getId()!=-1){
                    PokerLabel pokerLabel = new PokerLabel(poker.getId(),poker.getName(),poker.getNum(),poker.isOut());
                    // 显示正面
                    pokerLabel.turnUp();
                    myPanel.add(pokerLabel);
                    leftLabels.add(pokerLabel);
                    myPanel.setComponentZOrder(pokerLabel, 0);
                    // 一张一张的显示出来
                    GameUtil.move(pokerLabel, x1+(30*i), y);
                }else{
                    JLabel labell = new JLabel("不出",JLabel.CENTER);
                    GameUtil.jLabelBtn(labell,x1 , y,Color.orange);
                    myPanel.add(labell);
                    leftLabels.add(labell);
                }
            }

        }
    }

    // 验证出牌是否正确
    private boolean verifyPlay(List<PokerLabel> outPokerLabels, List<Player> players) {
        // 验证是否符合  从大到小
        int outSize = outPokerLabels.size();
        int playType = 0; // 1 火箭 2 对牌 3 炸弹 4 三带一 5 单牌 6 三张牌 7 三带二 8 单顺 9 双顺 10 三顺 11 三顺同数量的单牌 12 三顺同数量的对牌 13 四带两个单牌 14 四带对牌
        boolean isVerify = false;
        int scoreP = 0; // 出牌分值
        List<Poker> pokers = new ArrayList<>(); // 前面玩家出的牌
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getName().equals(uname)){
                currentPlayer = players.get(i);
            }else {
                // 不是不出
                if(players.get(i).getMessage().getPokers().size() > 0 && players.get(i).getMessage().getPokers().get(0).getId()!=-1){
                    pokers = players.get(i).getMessage().getPokers();
                }
            }
        }
        // 双张
        if(outSize==2){
            int num = 0;
            for (int i = 0; i < outSize; i++) {
                // 火箭
                if(outPokerLabels.get(i).getNum()==17 || outPokerLabels.get(i).getNum() == 16){
                    num++;
                }
            }
            // 火箭
            if(num==2){
                scoreP=17+16;
                playType = 1;
                isVerify = true;
            }else{
                // 判断是否是对牌
                if(outPokerLabels.get(0).getNum()==outPokerLabels.get(1).getNum()){
                    scoreP=outPokerLabels.get(0).getNum()*2;
                    isVerify = true;
                    playType = 2;
                }
            }
        }
        // 四张
        if(outSize==4){
            int num = 1;
            int numM = 0; // 多的值
            for (int i = 1; i < outSize; i++) {
                // 相同数
                if(outPokerLabels.get(i).getNum()==outPokerLabels.get(i-1).getNum()){
                    numM = outPokerLabels.get(i).getNum();
                    num++;
                }else{
                    if(num>=3){
                        break;
                    }
                    num = 1;
                }
            }
            if(num==4){
                //  炸弹
                scoreP = numM*4;
                playType = 3;
                isVerify = true;
            }else if(num==3){
                //  三带一
                scoreP = numM*3;
                playType = 4;
                isVerify = true;
            }
        }

        // 一张
        if(outSize==1){
            scoreP = outPokerLabels.get(0).getNum();
            playType = 5;
            isVerify = true;
        }

        // 三张
        if(outSize==3){
            isVerify = true;
            for (int i = 1; i < outSize; i++) {
                // 判断不相等
                if(outPokerLabels.get(i).getNum()!=outPokerLabels.get(i-1).getNum()){
                    isVerify = false;
                }
            }
            playType = 6;
            scoreP = outPokerLabels.get(0).getNum()*3;
        }

        // 大于5张
        if(outSize>=5){
            // 判断是否是三带二
            boolean isVerify7 = false;
            if(outSize==5){
                scoreP = 0;
                // 三带二
                int num3 = 0;
                int num2 = 0;
                int num = 0;
                for (int i = 0; i < outSize; i++) {
                    if(i>0){
                        if(outPokerLabels.get(i).getNum()==outPokerLabels.get(i-1).getNum()){
                            num++;
                            if(i==(outSize-1)){
                                if(num==3){
                                    num3 = outPokerLabels.get(i).getNum();
                                }else if(num==2){
                                    num2 = outPokerLabels.get(i).getNum();
                                }
                            }
                        }else{
                            if(num==3){
                                num3 = outPokerLabels.get(i-1).getNum();
                            }else if(num==2){
                                num2 = outPokerLabels.get(i-1).getNum();
                            }
                            num=1;
                        }
                    }else{
                        num++;
                    }
                }
                if(num3!=0&&num2!=0){
                    scoreP = num3*3;
                    playType = 7;
                    isVerify7 = true;
                    isVerify = true;
                }
            }

            // 判断是否是四带两个单牌
            boolean isVerify13 = false;
            if(outSize==6){
                scoreP = 0;
                // 四带两个单牌
                int num4 = 0;
                int num2 = 0;
                int num = 0;
                for (int i = 0; i < outSize; i++) {
                    if(i>0){
                        if(outPokerLabels.get(i).getNum()==outPokerLabels.get(i-1).getNum()){
                            num++;
                            if(i==(outSize-1)){
                                if(num==4){
                                    num4 = outPokerLabels.get(i).getNum();
                                }else if(num==2){
                                    num2 = outPokerLabels.get(i).getNum();
                                }
                            }
                        }else{
                            if(num==4){
                                num4 = outPokerLabels.get(i-1).getNum();
                            }else if(num==2){
                                num2 = outPokerLabels.get(i-1).getNum();
                            }
                            num=1;
                        }
                    }else{
                        num++;
                    }
                }
                if(num4!=0&&num2==0){
                    scoreP = num4*4;
                    playType = 13;
                    isVerify13 = true;
                    isVerify = true;
                }
            }

            // 判断是否是四带对牌
            boolean isVerify14 = false;
            if(outSize==8){
                scoreP = 0;
                // 四带对牌
                int num4 = 0;
                int num21 = 0;
                int num22 = 0;
                int num = 0;
                for (int i = 0; i < outSize; i++) {
                    if(i>0){
                        if(outPokerLabels.get(i).getNum()==outPokerLabels.get(i-1).getNum()){
                            num++;
                            if(i==(outSize-1)){
                                if(num==4){
                                    num4 = outPokerLabels.get(i).getNum();
                                }else if(num==2){
                                    if(num21!=0){
                                        num22 = outPokerLabels.get(i).getNum();
                                    }else{
                                        num21 = outPokerLabels.get(i).getNum();
                                    }
                                }
                            }
                        }else{
                            if(num==4){
                                num4 = outPokerLabels.get(i-1).getNum();
                            }else if(num==2){
                                if(num21!=0){
                                    num22 = outPokerLabels.get(i-1).getNum();
                                }else{
                                    num21 = outPokerLabels.get(i-1).getNum();
                                }
                            }
                            num=1;
                        }
                    }else{
                        num++;
                    }
                }
                if(num4!=0&&num21!=0&&num22!=0){
                    scoreP = num4*4;
                    playType = 14;
                    isVerify14 = true;
                    isVerify = true;
                }
            }


            if(!isVerify7 && !isVerify13 && !isVerify14){
                scoreP = 0;
                // 判断是否是单顺
                boolean isVerify8 = true;
                // 判断是否是双顺
                boolean isVerify9 = outSize%2==0; // 是否偶数
                // 判断是否是三顺
                boolean isVerify10 = outSize%3==0; // 是否偶数
                // 不包括2和大小王
                if(outPokerLabels.get(0).getNum()>=15){
                    isVerify8 = false;
                    isVerify9 = false;
                    isVerify10 = false;
                }
                // 判断是否是飞机带翅膀
                boolean isVerify11 = outSize%4==0; // 是否偶数
                boolean isVerify12 = outSize%5==0; // 是否偶数
                List<Integer> num3s = new ArrayList(); // 存放3张相同的
                List<Integer> num2s = new ArrayList(); // 存放2张相同的
                List<Integer> nums = new ArrayList(); // 存放单张的
                int num = 0;
                if(isVerify8 || isVerify9 || isVerify10 || isVerify11 || isVerify12){
                    for (int i = 0; i < outSize; i++) {
                        scoreP+=outPokerLabels.get(i).getNum();
                        // 单顺判断前面是否大1
                        if(isVerify8 && i>0 && outPokerLabels.get(i).getNum()!=(outPokerLabels.get(i-1).getNum() - 1)){
                            isVerify8 = false;
                        }
                        // 双顺
                        if(isVerify9 && (i+1)%2 == 0){
                            // 判断前面是否不等
                            if(outPokerLabels.get(i).getNum()!=outPokerLabels.get(i-1).getNum()){
                                isVerify9 = false;
                            }
                            // 判断是否有序并超过3张相同
                            if(i>=2 && outPokerLabels.get(i).getNum()!=(outPokerLabels.get(i-2).getNum()-1) && outPokerLabels.get(i).getNum()==outPokerLabels.get(i-2).getNum()){
                                isVerify9 = false;
                            }
                        }
                        // 三顺
                        if(isVerify10 && (i+1)%3 == 0){
                            // 判断前面是否不等
                            if(outPokerLabels.get(i).getNum() != outPokerLabels.get(i-1).getNum() || outPokerLabels.get(i).getNum() != outPokerLabels.get(i-2).getNum()){
                                isVerify10 = false;
                            }
                            // 判断是否有序并超过4张相同
                            if(i>=3 && outPokerLabels.get(i).getNum()!=(outPokerLabels.get(i-3).getNum()-1)  && outPokerLabels.get(i).getNum()==outPokerLabels.get(i-3).getNum()){
                                isVerify10 = false;
                            }
                        }
                        // 判断是否是飞机带翅膀
                        if(i>0){
                            if(outPokerLabels.get(i).getNum()==outPokerLabels.get(i-1).getNum()){
                                num++;
                                if(i==(outSize-1)){
                                    if(num==4){
                                        isVerify11 = false;
                                        isVerify12 = false;
                                    }else if(num==3){
                                        if(num3s.size()>0 && (num3s.get(num3s.size()-1) - 1) != outPokerLabels.get(i).getNum()){
                                            isVerify11 = false;
                                            isVerify12 = false;
                                        }else{
                                            num3s.add(outPokerLabels.get(i).getNum());
                                        }
                                    }else if(num==2){
                                        num2s.add(outPokerLabels.get(i).getNum());
                                    }
                                }
                            }else {
                                if(num==4){
                                    isVerify11 = false;
                                    isVerify12 = false;
                                }else if(num==3){
                                    if(num3s.size()>0 && (num3s.get(num3s.size()-1) - 1) != outPokerLabels.get(i-1).getNum()){
                                        isVerify11 = false;
                                        isVerify12 = false;
                                    }else{
                                        num3s.add(outPokerLabels.get(i-1).getNum());
                                    }
                                }else if(num==2){
                                    num2s.add(outPokerLabels.get(i-1).getNum());
                                }else if(num==1){
                                    nums.add(outPokerLabels.get(i-1).getNum());
                                }
                                num=1;
                            }
                        }else{
                            num++;
                        }
                    }
                }

                // 单顺
                if(isVerify8){
                    isVerify = true;
                    playType = 8;
                }
                // 双顺
                if(isVerify9){
                    isVerify = true;
                    playType = 9;
                }
                // 三顺
                if(isVerify10){
                    isVerify = true;
                    playType = 10;
                }
                boolean is3 = false;
                // 三顺同数量的单牌
                if(isVerify11 && num3s.size()>=2){
                    is3 = true;
                    if(num3s.size()==nums.size() && num2s.size()==0 && !(nums.get(0)==17 && nums.get(1)==16)){
                        isVerify = true;
                        isVerify11 = true;
                        playType = 11;
                        scoreP = 0;
                        for (int i = 0; i < num3s.size(); i++) {
                            scoreP+=num3s.get(i);
                        }
                    }else{
                        isVerify11 = false;
                    }
                }
                // 三顺同数量的对牌
                if(isVerify12 && num3s.size()>=2){
                    is3 = true;
                    if(num3s.size()==num2s.size() && nums.size()==0){
                        isVerify = true;
                        isVerify12 = true;
                        playType = 12;
                        scoreP = 0;
                        for (int i = 0; i < num3s.size(); i++) {
                            scoreP+=num3s.get(i);
                        }
                    }else{
                        isVerify12 = false;
                    }
                }
                if(is3 && !isVerify11 && !isVerify12){
                    isVerify = false;
                }
            }
        }
        // 不符合直接退出
        if(!isVerify){
            return isVerify;
        }
        // 符合就能出牌
        if(currentPlayer.getMessage().getType()==5 || currentPlayer.getMessage().getType()==8){
            return isVerify;
        }else{
            if(playType==1){
                // 火箭
                return isVerify;
            }else if(playType==2){
                // 对牌判断
                if(pokers.size()==2){
                    // 前面出牌的分值
                    int scoreW = pokers.get(0).getNum() + pokers.get(1).getNum();
                    if(scoreP>scoreW){
                        isVerify = true;
                    }else {
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==3){
                //  炸弹
                // 其他玩家火箭
                if(pokers.size()==2){
                    int scoreW = pokers.get(0).getNum() + pokers.get(1).getNum();
                    if(scoreW==33){
                        isVerify = false;
                    }
                }else if(pokers.size()==4){
                    // 其他玩家炸弹
                    int num = 1;
                    int numM = 0;
                    for (int i = 1; i < pokers.size(); i++) {
                        // 相同数
                        if(pokers.get(i).getNum()==pokers.get(i-1).getNum()){
                            numM = pokers.get(i).getNum();
                            num++;
                        }else{
                            break;
                        }
                    }
                    if(num==4){
                        int scoreW = numM*4;
                        if(scoreP<scoreW){
                            isVerify = false;
                        }else{
                            isVerify = true;
                        }
                    }else{
                        isVerify = true;
                    }
                }else{
                    isVerify = true;
                }
            }else if(playType==4){
                // 三带一
               if(pokers.size()==4){
                    // 其他玩家炸弹
                    int num = 1;
                    int numM = 0;
                    for (int i = 1; i < pokers.size(); i++) {
                        // 相同数
                        if(pokers.get(i).getNum()==pokers.get(i-1).getNum()){
                            numM = pokers.get(i).getNum();
                            num++;
                        }else{
                            num = 1;
                        }
                    }
                    if(num==4){
                        isVerify = false;
                    }else{
                        // 三带一判断
                        int scoreW = numM*3;
                        if(scoreP<scoreW){
                            isVerify = false;
                        }else{
                            isVerify = true;
                        }
                    }
                }else{
                    // 其他玩家非三带一
                    isVerify = false;
                }
            }else if(playType==5){
                // 单牌
                if(pokers.size()==1){
                    int scoreW = pokers.get(0).getNum();
                    if(scoreP>scoreW){
                        isVerify = true;
                    }else{
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==6){
                // 三张牌
                if(pokers.size()==3){
                    int scoreW = pokers.get(0).getNum()*3;
                    if(scoreP>scoreW){
                        isVerify = true;
                    }else{
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==7){
                // 三带二
                if(pokers.size()==5){
                    int num3 = 0;
                    int num2 = 0;
                    int num = 0;
                    for (int i = 0; i < pokers.size(); i++) {
                        if(i>0){
                            if(pokers.get(i).getNum()==pokers.get(i-1).getNum()){
                                num++;
                                if(i==pokers.size()-1){
                                    if(num==3){
                                        num3 = pokers.get(i).getNum();
                                    }else if(num==2){
                                        num2 = pokers.get(i).getNum();
                                    }
                                }
                            }else{
                                if(num==3){
                                    num3 = pokers.get(i-1).getNum();
                                }else if(num==2){
                                    num2 = pokers.get(i-1).getNum();
                                }
                                num=1;
                            }
                        }else{
                            num++;
                        }
                    }
                    if(num3!=0&&num2!=0){
                        int scoreW = num3*3;
                        if(scoreP>scoreW){
                            isVerify = true;
                        }else {
                            isVerify = false;
                        }
                    }else {
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==8){
                // 单顺
                if(pokers.size()==outSize){
                    // 不包括2和大小王
                    if(pokers.get(0).getNum()<15){
                        int scoreW = pokers.get(0).getNum();
                        isVerify = true;
                        for (int i = 1; i < pokers.size(); i++) {
                            scoreW+=pokers.get(i).getNum();
                            // 判断前面是否大1
                            if(pokers.get(i).getNum()!=(pokers.get(i-1).getNum() - 1)){
                                isVerify = false;
                                break;
                            }
                        }
                        if(isVerify && scoreP>scoreW){
                            isVerify = true;
                        }else{
                            isVerify = false;
                        }
                    }else {
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==9){
                // 双顺
                if(pokers.size()==outSize){
                    // 不包括2和大小王
                    if(pokers.get(0).getNum()<15){
                        int scoreW = 0;
                        isVerify = true;
                        for (int i = 0; i < pokers.size(); i++) {
                            scoreW+=pokers.get(i).getNum();
                            // 双顺
                            if((i+1)%2 == 0){
                                // 判断前面是否不等
                                if(pokers.get(i).getNum()!=pokers.get(i-1).getNum()){
                                    isVerify = false;
                                }
                                // 判断是否有序并超过3张相同
                                if(i>=2 && pokers.get(i).getNum()!=(pokers.get(i-2).getNum()-1) && pokers.get(i).getNum()==pokers.get(i-2).getNum()){
                                    isVerify = false;
                                }
                            }
                        }
                        if(isVerify && scoreP>scoreW){
                            isVerify = true;
                        }else{
                            isVerify = false;
                        }
                    }else {
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==10){
                // 三顺
                if(pokers.size()==outSize){
                    // 不包括2和大小王
                    if(pokers.get(0).getNum()<15){
                        int scoreW = 0;
                        isVerify = true;
                        for (int i = 0; i < pokers.size(); i++) {
                            scoreW+=pokers.get(i).getNum();
                            // 三顺
                            if((i+1)%3 == 0){
                                // 判断前面是否不等
                                if(pokers.get(i).getNum() != pokers.get(i-1).getNum() || pokers.get(i).getNum() != pokers.get(i-2).getNum()){
                                    isVerify = false;
                                }
                                // 判断是否有序并超过4张相同
                                if(i>=3 && pokers.get(i).getNum()!=(pokers.get(i-3).getNum()-1)  && pokers.get(i).getNum()==pokers.get(i-3).getNum()){
                                    isVerify = false;
                                }
                            }
                        }
                        if(isVerify && scoreP>scoreW){
                            isVerify = true;
                        }else{
                            isVerify = false;
                        }
                    }else {
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==11 || playType==12){
                // 飞机带翅膀
                if(pokers.size()==outSize){
                    List<Integer> num3s = new ArrayList(); // 存放3张相同的
                    List<Integer> num2s = new ArrayList(); // 存放2张相同的
                    List<Integer> nums = new ArrayList(); // 存放单张的
                    int num = 0;
                    isVerify = true;
                    for (int i = 0; i < pokers.size(); i++) {
                        // 判断是否是飞机带翅膀
                        if(i>0){
                            if(pokers.get(i).getNum()==pokers.get(i-1).getNum()){
                                num++;
                                if(i==(pokers.size()-1)){
                                    if(num==4){
                                        isVerify = false;
                                    }else if(num==3){
                                        if(num3s.size()>0 && (num3s.get(num3s.size()-1) - 1) != pokers.get(i).getNum()){
                                            isVerify = false;
                                        }else{
                                            num3s.add(pokers.get(i).getNum());
                                        }
                                    }else if(num==2){
                                        num2s.add(pokers.get(i).getNum());
                                    }
                                }
                            }else {
                                if(num==4){
                                    isVerify = false;
                                }else if(num==3){
                                    if(num3s.size()>0 && (num3s.get(num3s.size()-1) - 1) != pokers.get(i-1).getNum()){
                                        isVerify = false;
                                    }else{
                                        num3s.add(pokers.get(i-1).getNum());
                                    }
                                }else if(num==2){
                                    num2s.add(pokers.get(i-1).getNum());
                                }else if(num==1){
                                    nums.add(pokers.get(i-1).getNum());
                                }
                                num=1;
                            }
                        }else{
                            num++;
                        }
                    }
                    if(isVerify && num3s.size()>=2){
                        int scoreW = 0;
                        // 三顺同数量的单牌
                        if(playType==11){
                            if(num3s.size()==nums.size() && num2s.size()==0 && !(nums.get(0)==17 && nums.get(1)==16)){
                                for (int i = 0; i < num3s.size(); i++) {
                                    scoreW+=num3s.get(i);
                                }
                                if(scoreP>scoreW){
                                    isVerify = true;
                                }else {
                                    isVerify = false;
                                }
                            }else{
                                isVerify = false;
                            }
                        }
                        // 三顺同数量的对牌
                        if(playType==12){
                            if(num3s.size()==num2s.size() && nums.size()==0){
                                for (int i = 0; i < num3s.size(); i++) {
                                    scoreW+=num3s.get(i);
                                }
                                if(scoreP>scoreW){
                                    isVerify = true;
                                }else {
                                    isVerify = false;
                                }
                            }else{
                                isVerify = false;
                            }
                        }
                    }else{
                        isVerify = false;
                    }

                }else{
                    isVerify = false;
                }
            }else if(playType==13){
                // 四带两个单牌
                if(pokers.size()==outSize){
                    int num4 = 0;
                    int num2 = 0;
                    int num = 0;
                    for (int i = 0; i < pokers.size(); i++) {
                        if(i>0){
                            if(pokers.get(i).getNum()==pokers.get(i-1).getNum()){
                                num++;
                                if(i==(pokers.size()-1)){
                                    if(num==4){
                                        num4 = pokers.get(i).getNum();
                                    }else if(num==2){
                                        num2 = pokers.get(i).getNum();
                                    }
                                }
                            }else{
                                if(num==4){
                                    num4 = pokers.get(i-1).getNum();
                                }else if(num==2){
                                    num2 = pokers.get(i-1).getNum();
                                }
                                num=1;
                            }
                        }else{
                            num++;
                        }
                    }
                    if(num4!=0&&num2==0){
                        int scoreW = num4*4;
                        if (scoreP > scoreW) {
                            isVerify = true;
                        }else{
                            isVerify = false;
                        }
                    }else{
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }else if(playType==14){
                // 四带对牌
                if(pokers.size()==outSize){
                    int num4 = 0;
                    int num21 = 0;
                    int num22 = 0;
                    int num = 0;
                    for (int i = 0; i < pokers.size(); i++) {
                        if(i>0){
                            if(pokers.get(i).getNum()==pokers.get(i-1).getNum()){
                                num++;
                                if(i==(pokers.size()-1)){
                                    if(num==4){
                                        num4 = pokers.get(i).getNum();
                                    }else if(num==2){
                                        if(num21!=0){
                                            num22 = pokers.get(i).getNum();
                                        }else{
                                            num21 = pokers.get(i).getNum();
                                        }
                                    }
                                }
                            }else{
                                if(num==4){
                                    num4 = pokers.get(i-1).getNum();
                                }else if(num==2){
                                    if(num21!=0){
                                        num22 = pokers.get(i-1).getNum();
                                    }else{
                                        num21 = pokers.get(i-1).getNum();
                                    }
                                }
                                num=1;
                            }
                        }else{
                            num++;
                        }
                    }
                    if(num4!=0&&num21!=0&&num22!=0){
                        int scoreW = num4*4;
                        if(scoreP>scoreW){
                            isVerify = true;
                        }else {
                            isVerify = false;
                        }
                    }else{
                        isVerify = false;
                    }
                }else{
                    isVerify = false;
                }
            }

            // 判断是否能出
            return isVerify;
        }

    }

}
