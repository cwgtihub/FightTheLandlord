package com.cw.utils;

import com.cw.model.PokerLabel;

import javax.swing.*;
import java.awt.*;

public class GameUtil {
    public static void move(PokerLabel pokerLabel,int x,int y){
        pokerLabel.setLocation(x, y);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void jLabelBtn(JLabel jLabel,int x,int y,Color color){
        jLabel.setLayout(null);
        jLabel.setFont(new Font("仿宋", Font.BOLD,25));
        jLabel.setForeground(Color.white);
        jLabel.setLocation(x, y);
        jLabel.setSize(100,45);
        jLabel.setBackground(color);
        jLabel.setOpaque(true);
    }
}
