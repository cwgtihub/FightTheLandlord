package com.cw.thread;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
// 发送消息线程
public class SendThread extends Thread{
    private String msg;
    private Socket socket;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public SendThread() {

    }
    public SendThread(Socket socket) {
        this.socket=socket;
    }
    public SendThread(Socket socket,String msg) {
        this.socket=socket;
        this.msg=msg;
    }

    @Override
    public void run() {
//        super.run();
        DataOutputStream dataOutputStream = null;
        // 最后的心跳时间
        long lastReceiveTime = System.currentTimeMillis();
        // 保持时间
        long keepAliveDelay = 3000;
        // 休眠时长
        long checkDelay = 10;
        // 发送消息
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while (true){
                // 如果消息不为空
                if (msg != null) {
                    // 发送消息
                    try {
                        dataOutputStream.writeUTF(msg);
                        lastReceiveTime = System.currentTimeMillis();
                        // 清空消息
                        msg=null;
                    } catch (IOException e) {
                        e.printStackTrace();
//                        JOptionPane.showMessageDialog(null, "游戏已经开始了", "信息", JOptionPane.INFORMATION_MESSAGE);
//                        System.exit(0);
                    }
                }else {
                    // 心跳数据
                    if(System.currentTimeMillis()-lastReceiveTime>keepAliveDelay){
                        // 发送消息
                        try {
                            dataOutputStream.writeUTF("0");
                            lastReceiveTime = System.currentTimeMillis();
                        } catch (IOException e) {
                            e.printStackTrace();
//                            JOptionPane.showMessageDialog(null, "游戏已经开始了", "信息", JOptionPane.INFORMATION_MESSAGE);
//                            System.exit(0);
                        }
                    }else{
                        try {
                            Thread.sleep(checkDelay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
