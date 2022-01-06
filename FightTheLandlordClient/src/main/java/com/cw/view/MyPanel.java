package com.cw.view;

import com.cw.model.Poker;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;

// 斗地主背景
public class MyPanel extends JPanel {
    static {
        URL path = MyPanel.class.getResource("/images");
    }
    MainFrame mainFrame;

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public MyPanel() {
        this.setLayout(null); // 绝对定位时
    }

    // 展示牌数
    private void drawCards(Graphics g,int index,int x,int y,Color color){
        List<Poker> pokers = mainFrame.getPlayers().get(index).getPokers();
        int outs = pokers.size();
        for (int j = 0; j < pokers.size(); j++) {
            if(pokers.get(j).isOut()){
                outs--;
            }
        }
        g.setColor(color);
        g.drawString(String.valueOf(outs), x, y);
    }
    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        URL path = MyPanel.class.getResource("/images/bg/bg.jpg"); // jar包时
        String path = this.getClass().getClassLoader().getResource("").getPath()+"/images/bg/bg.jpg";

        Image image = new ImageIcon(path).getImage();
        g.drawImage(image, 0,0 ,this.getWidth(),this.getHeight(),null);
        if(mainFrame.isStart){
            // 显示当前玩家的扑克列表
            int index = -1;
            String[] unames = new String[3];
            g.setColor(Color.YELLOW);
            g.setFont(new Font("仿宋", Font.BOLD,20));
            for (int i = 0; i < mainFrame.getPlayers().size(); i++) {
                unames[i] = mainFrame.getPlayers().get(i).getName();
                if(mainFrame.getPlayers().get(i).getName().length()>=5){
                    unames[i] = mainFrame.getPlayers().get(i).getName().substring(0, 4)+"..";
                }
                if (mainFrame.getPlayers().get(i).getName().equals(mainFrame.uname)) {
                    index = i;
                    g.drawString(unames[i], 50, 595);
                }
            }
            int x1=10,x2=1090,y=295;
            int x3=175,x4=990,y1=315,y2=290;
            // 当前是第一个玩家
            if(index == 0){
                // 右边
                g.drawString(unames[index+1], x2, y);
                // 左边
                g.drawString(unames[index+2], x1, y);
                drawCards(g,index+1,x4,y2,Color.BLACK);
                drawCards(g,index+2,x3,y1,Color.BLACK);
            }else if(index == 1){
                // 右边
                g.drawString(unames[index+1], x2, y);
                // 左边
                g.drawString(unames[index-1], x1, y);
                drawCards(g,index+1,x4,y2,Color.BLACK);
                drawCards(g,index-1,x3,y1,Color.BLACK);
            }else{
                // 当前是最后一个玩家
                // 右边
                g.drawString(unames[index-2], x2, y);
                // 左边
                g.drawString(unames[index-1], x1, y);
                drawCards(g,index-2,x4,y2,Color.BLACK);
                drawCards(g,index-1,x3,y1,Color.BLACK);
            }

            // 倒计时
            if(mainFrame.isCountdown){
                g.setColor(Color.red);
                g.setFont(new Font("仿宋", Font.BOLD,30));
                g.drawString(String.valueOf(mainFrame.time), mainFrame.countdownX, mainFrame.countdownY);
            }
        }
    }

}
