package P2PChats.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import P2PChats.model.User;
import P2PChats.view.*;

public class ClientMain extends ClientView {
	
	//model
	private User me;
	// ���������û�
    private ConcurrentHashMap<String, User> onlineUsers = new ConcurrentHashMap<String, User>();  
    private String sendTarget = "ALL";  //Ĭ�Ϸ��Ͷ���
  
    //Socket  
    private Socket socket;  
    private PrintWriter writer;    //�����
    private BufferedReader reader; //������ 
  
    // ���������Ϣ���߳�  
    private MessageThread messageThread;  
  
    //Status  
    private boolean isConnected;   //�ж��Ƿ����ӵ������
    
    //���캯��
    public ClientMain() {
    	
    // д��Ϣ���ı����а��س���ʱ�¼�
    messageTextField.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            send();  
        }  
    });
    
    // �������Ͱ�ťʱ�¼�
    sendButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            send();  
        }  
    });  

    // �������Ӱ�ťʱ�¼�
    connectButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            if (!isConnected) {  
                connect();  
            }  
        }  
    });  

    // �����Ͽ���ťʱ�¼�
    disconnectButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            if (isConnected) {  
                disconnect();  
            }  
        }  
    });  

    // �رմ���ʱ�¼�
    frame.addWindowListener(new WindowAdapter() {  
        public void windowClosing(WindowEvent e) {  
            if (isConnected) {  
                disconnect();  
            }  
            System.exit(0);  
        }  
    });  

    // Ϊ�����û���ӵ���¼�
    userList.addListSelectionListener(new ListSelectionListener() { 
    	
        public void valueChanged(ListSelectionEvent e) {  
            int index = userList.getSelectedIndex();  //��ȡ��������û������
            if (index < 0) return;  
 
            if (index == 0) {  //Ĭ��Ϊ������
                sendTarget = "ALL";  
                messageToLabel.setText("To: ������");  
            } else {  
                String name = (String)listModel.getElementAt(index);  //��ȡ������û�������
                if (onlineUsers.containsKey(name)) {  
                    sendTarget = onlineUsers.get(name).description();  
                    messageToLabel.setText("To: " + name);  //��To..��ǩ��ΪTo �û���
                } else {  
                    sendTarget = "ALL";  
                    messageToLabel.setText("To: ������");  
                }  
            }  
        }  
    });
}
    
    //����
    private void connect() {  
        int port;  
          
        try {  
            port = Integer.parseInt(portTextField.getText().trim());  //��ȡ�˿ں�
        } catch(NumberFormatException e) {  
            showErrorMessage("�˿ںű���Ϊ������");  
            return;  
        }  
  
        if (port < 1024 || port > 65535) {  //�ж϶˿ں��Ƿ����
            showErrorMessage("�˿ںű�����1024��65535֮��");  
            return;  
        }  
  
        String name = nameTextField.getText().trim();  //��ȡ�û���
  
        if (name == null || name.equals("")) {  //�ж��û����Ƿ�Ϊ��
            showErrorMessage("���ֲ���Ϊ�գ�");  
            return;  
        }  
  
        String ip = ipTextField.getText().trim();  //��ȡIP��ַ
  
        if (ip == null || ip.equals("")) {  //�ж�IP��ַ�Ƿ�Ϊ��
            showErrorMessage("IP��ַ����Ϊ�գ�");  
            return;  
        }  
  
        try {  
            listModel.addElement("������");  
  
            me = new User(name, ip);  
            socket = new Socket(ip, port);  //����ָ��IP��ַ�Լ��˿ںŽ����߳�
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //������
            writer = new PrintWriter(socket.getOutputStream());  //�����
  
            String myIP = socket.getLocalAddress().toString().substring(1);  //��ȡ�ͻ������ڵ�IP��ַ
            sendMessage("LOGIN@" + name + "%" + myIP);  //�����û���¼��Ϣ
  
            messageThread = new MessageThread();  //����������Ϣ���߳�
            messageThread.start();  
            isConnected = true;  
  
        } catch(Exception e) {  
            isConnected = false;  
            logMessage("�ͻ�������ʧ��");  
            listModel.removeAllElements();  //�Ƴ���������������û�
            e.printStackTrace();  
            return;  
        }  
  
        logMessage("�ͻ������ӳɹ�");       //�����ӳɹ�����Ϣ��ʾ����Ϣ�����
        serviceUISetting(isConnected); //���ð�ť��״̬
    }  
  
    //��Ϣ����
    private void send() {  
        if (!isConnected) {  
            showErrorMessage("δ���ӵ���������");  
            return;  
        }  
        String message = messageTextField.getText().trim();  //��ȡ���Ϳ����� 
        if (message == null || message.equals("")) {  
            showErrorMessage("��Ϣ����Ϊ�գ�");  
            return;  
        }  
  
        String to = sendTarget;  
        try {  
        	//�������������Ϣ
        	//MSG@+��������Ϣ�û��� %IP��ַ��+���������û��� %IP��ַ��+@+message
            sendMessage("MSG@" + to + "@" + me.description() + "@" + message);  
            logMessage("��->" + to + ": " + message);  
        } catch(Exception e) {  
            e.printStackTrace();  
            logMessage("������ʧ�ܣ���->" + to + ": " + message);  
        }  
  
        messageTextField.setText(null);  //������ϰ�������ÿ�
    }  
  
    //�Ͽ�����
    private synchronized void disconnect() {  
        try {  
        	//����������ͶϿ����ӵ���Ϣ
            sendMessage("LOGOUT");  
  
            messageThread.close();  
            listModel.removeAllElements();  
            onlineUsers.clear();  
  
            reader.close();  
            writer.close();  
            socket.close();  
            isConnected = false;  
            serviceUISetting(false);  
  
            sendTarget = "ALL";  
            messageToLabel.setText("To: ������");  
  
            logMessage("�ѶϿ�����...");  
        } catch(Exception e) {  
            e.printStackTrace();  
            isConnected = true;  
            serviceUISetting(true);  
            showErrorMessage("�������Ͽ�����ʧ�ܣ�");  
        }  
    }  
  
    private void sendMessage(String message) {  
        writer.println(message);  
        writer.flush();  
    }  
  
    private void logMessage(String msg) {  
        messageTextArea.append(msg + "\r\n");  
    }  
  
    private void showErrorMessage(String msg) {  
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);  
    }   
              
    //������Ϣ���߳�
    private class MessageThread extends Thread {  
        private boolean isRunning = false;  
  
        public MessageThread() {  
            isRunning = true;  
        }  
  
        public void run() {  
            while (isRunning) {  //���Ͻ�����Ϣ
                try {  
                    String message = reader.readLine();  
                    StringTokenizer tokenizer = new StringTokenizer(message, "@");  
                    String command = tokenizer.nextToken();  
  
                    if (command.equals("CLOSE")) {  
                        logMessage("�������ѹرգ����ڶϿ�����...");  
                        disconnect();  
                        isRunning = false;  
                        return;  
                    } else if (command.equals("ERROR")) {  
                        String error = tokenizer.nextToken();  
                        logMessage("���������ش��󣬴������ͣ�" + error);  
                    } else if (command.equals("LOGIN")) {  
                        String status = tokenizer.nextToken();  
                        if (status.equals("SUCCESS")) {  
                            logMessage("��¼�ɹ���" + tokenizer.nextToken());  
                        } else if (status.equals("FAIL")) {  
                            logMessage("��¼ʧ�ܣ��Ͽ����ӣ�ԭ��" + tokenizer.nextToken());  
                            disconnect();  
                            isRunning = false;  
                            return;  
                        }  
                    } else if (command.equals("USER")) {  
                        String type = tokenizer.nextToken();  
                        if (type.equals("ADD")) {  
                            String userDescription = tokenizer.nextToken();  
                            User newUser = new User(userDescription);  
                            onlineUsers.put(newUser.getName(), newUser);  
                            listModel.addElement(newUser.getName());  
  
                            logMessage("���û���" + newUser.description() + "�����ߣ�");  
  
                        } else if (type.equals("DELETE")) {  
                            String userDescription = tokenizer.nextToken();  
                            User deleteUser = new User(userDescription);  
                            onlineUsers.remove(deleteUser.getName());  
                            listModel.removeElement(deleteUser.getName());  
  
                            logMessage("�û���" + deleteUser.description() + "�����ߣ�");  
  
                            if (sendTarget.equals(deleteUser.description())) {  
                                sendTarget = "ALL";  
                                messageToLabel.setText("To: ������");  
                            }  
  
                        } else if (type.equals("LIST")) {  
                            int num = Integer.parseInt(tokenizer.nextToken());  
                            for (int i = 0; i < num; i++) {  
                                String userDescription = tokenizer.nextToken();  
                                User newUser = new User(userDescription);  
                                onlineUsers.put(newUser.getName(), newUser);  
                                listModel.addElement(newUser.getName());  
  
                                logMessage("��ȡ���û���" + newUser.description() + "�����ߣ�");  
                            }  
                        }  
                    } else if (command.equals("MSG")) {  
                        StringBuffer buffer = new StringBuffer();  
                        String to = tokenizer.nextToken();  
                        String from = tokenizer.nextToken();  
                        String content = tokenizer.nextToken();  
  
                        buffer.append(from);  
                        if (to.equals("ALL")) {  
                            buffer.append("��Ⱥ����");  
                        }  
                        buffer.append(": " + content);  
                        logMessage(buffer.toString());  
                    }  
  
                } catch(Exception e) {  
                    e.printStackTrace();  
                    logMessage("������Ϣ�쳣��");  
                }  
            }  
        }  
  
        public void close() {  
            isRunning = false;  
        }  
    }
    
    
    // ������
    public static void main(String args[]){     	
       new ClientMain();            
    }
    
}
