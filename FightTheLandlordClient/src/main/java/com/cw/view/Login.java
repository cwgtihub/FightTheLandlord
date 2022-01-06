package com.cw.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Login extends JFrame {
    private JLabel unameJLabel;
    private JTextField unameJTextField;
    private JButton btnJButton;
    private JButton cancelJButton;
    protected JFrame frame = this;
    public Login() {
        // 创建组件对象
        this.unameJLabel=new JLabel("用户名：");
        this.unameJLabel.setHorizontalAlignment(JLabel.CENTER);
        this.unameJTextField=new JTextField();
        this.btnJButton=new JButton("登录");
        this.cancelJButton=new JButton("取消");
        // 添加组件到窗口中
        this.setLayout(new GridLayout(2,2));
        this.add(unameJLabel);
        this.add(unameJTextField);
        this.add(btnJButton);
        this.add(cancelJButton);

        // 创建监听器对象绑定到按钮
        MyEvent myEvent = new MyEvent();
        this.btnJButton.addActionListener(myEvent);

        this.setSize(400,300);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    // 创建事件监听器类
    class MyEvent implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            // 点击登录
            // 获取用户名
            String uname = unameJTextField.getText();
            if(uname.length()<3){
                JOptionPane.showMessageDialog(null, "名称不能小于3位", "信息", JOptionPane.INFORMATION_MESSAGE);
                unameJTextField.requestFocus();
            }else{
                // 创建一个socket连接服务器
                try {
                    Socket socket = new Socket("192.168.3.180", 8888);
                    // 跳转到主窗口
                    new MainFrame(uname,socket);
                    System.out.println(uname+"登录");
                    // 关闭当前窗口
                    frame.dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }
}
