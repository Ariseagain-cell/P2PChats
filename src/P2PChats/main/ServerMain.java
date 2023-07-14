package P2PChats.main;

import P2PChats.model.User;
import P2PChats.view.ServerView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;

/* 
	message type 
	1.alias: 
    	USER <= USER_NAME%USER_IPADDR 

	2.format: 
		server:  
        MSG     @   to      @   from    @   content  
                    ALL         SERVER      xxx 
                    ALL         USER        xxx 
                    USER        USER        xxx 

        LOGIN   @   status  @   content 
                    SUCCESS     xxx 
                    FAIL        xxx 

        USER    @   type    @   other 
                    ADD         USER 
                    DELETE      USER 
                    LIST        number  {@  USER}+ 

        ERROR   @   TYPE 

        CLOSE 
 
	client: 
        MSG     @   to      @   from    @   content 
                    ALL         USER        xxx 
                    USER        USER        xxx 
        LOGOUT 

        LOGIN   @   USER     

*/ 

public class ServerMain extends ServerView {
	
	//Socket  
    private ServerSocket serverSocket;  
  
    //Status  
    private boolean isStart = false;  //�жϷ������Ƿ��Ѿ�����
    private int maxClientNum;  //�����������
  
    //Threads  
    //ArrayList<ClientServiceThread> clientServiceThreads;  
    ConcurrentHashMap<String, ClientServiceThread> clientServiceThreads;  
    ServerThread serverThread;
	
    //���캯��
	public ServerMain() {
		
		//�㲥��Ϣ��󶨻س���
		serverMessageTextField.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                sendAll();  
            }  
        });  
  
		//���Ͱ�ť�󶨵���¼�
        sendButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                sendAll();  
            }  
        });  
  
        //������ť�󶨵���¼�
        startButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (!isStart) {  
                    startServer();  
                }  
            }  
        });  
  
        //ֹͣ��ť�󶨵���¼�
        stopButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (isStart) {  
                    stopServer();  
                }  
            }  
        });  
  
        //�󶨴��ڹر��¼�
        frame.addWindowListener(new WindowAdapter() {  
            public void windowClosing(WindowEvent e) {  
                if (isStart) {  
                    stopServer();  
                }  
                System.exit(0);  
            }  
        });
	}
	
	//���������
	private void startServer() {  
        int port;  
  
        //�ж�����Ķ˿ںŸ������������Ƿ���Ϲ淶
        try {  
            port = Integer.parseInt(portTextField.getText().trim());  
        } catch(NumberFormatException e) {  
            showErrorMessage("�˿ںű���Ϊ������");  
            return;  
        }  
  
        if (port < 1024 || port > 65535) {  
            showErrorMessage("�˿ںű�����1024��65535֮��");  
            return;  
        }  
  
        try {  
            maxClientNum = Integer.parseInt(maxClientTextField.getText().trim());  
        } catch(NumberFormatException e) {  
            showErrorMessage("�������ޱ�������������");  
            maxClientNum = 0;  
            return;  
        }  
  
        if (maxClientNum <= 0) {  
            showErrorMessage("�������ޱ�������������");  
            maxClientNum = 0;  
            return;  
        }  
  
        try {  //���û�ȡ���Ķ˿ںſ����������߳�
            clientServiceThreads = new ConcurrentHashMap<String, ClientServiceThread>();  
            serverSocket = new ServerSocket(port);  
            serverThread = new ServerThread();  
            serverThread.start();  
            isStart = true;  
        } catch (BindException e) {  
            isStart = false;  
            showErrorMessage("����������ʧ�ܣ��˿ڱ�ռ�ã�");  
            return;  
        } catch (Exception e) {  
            isStart = false;  
            showErrorMessage("����������ʧ�ܣ������쳣��");  
            e.printStackTrace();  
            return;  
        }  
  
        logMessage("�������������������ޣ�" + maxClientNum + " �˿ںţ�" + port);  
        serviceUISetting(true);  
    }  
  
    private synchronized void stopServer() {  
        try {  
            serverThread.closeThread();  
            //�Ͽ������пͻ��˵�����
            for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                ClientServiceThread clientThread = entry.getValue();  
                clientThread.sendMessage("CLOSE");  
                clientThread.close();  
            }  
  
            clientServiceThreads.clear();  
            listModel.removeAllElements();  
            isStart = false;  
            serviceUISetting(false);  
            logMessage("�������ѹرգ�");  
        } catch(Exception e) {  
            e.printStackTrace();  
            showErrorMessage("�رշ������쳣��");  
            isStart = true;  
            serviceUISetting(true);  
        }  
    }  
  
    private void sendAll() {  
        if (!isStart) {  
            showErrorMessage("��������δ���������ܷ�����Ϣ��");  
            return;  
        }  
  
        if (clientServiceThreads.size() == 0) {  
            showErrorMessage("û���û����ߣ����ܷ�����Ϣ��");  
            return;  
        }  
  
        String message = serverMessageTextField.getText().trim();  
        if (message == null || message.equals("")) {  
            showErrorMessage("������Ϣ����Ϊ�գ�");  
            return;  
        }  
  
        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
            entry.getValue().sendMessage("MSG@ALL@SERVER@" + message);  
        }  
  
        logMessage("Server: " + message);  
        serverMessageTextField.setText(null);  
    }  
  
    private void logMessage(String msg) {  
        logTextArea.append(msg + "\r\n");  
    }  
      
    private void showErrorMessage(String msg) {  
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);  
    }
    
    //Server Thread class  
    private class ServerThread extends Thread {  
        private boolean isRunning;  
  
        public ServerThread() {  
            this.isRunning = true;  
        }  
  
        public void run() {  
            while (this.isRunning) {  
                try {  
                    if (!serverSocket.isClosed()) {  //���տͻ��˷�������������
                        Socket socket = serverSocket.accept();  
  
                        if (clientServiceThreads.size() == maxClientNum) {  //�ж������Ƿ��Ѵ�����
                            PrintWriter writer = new PrintWriter(socket.getOutputStream());  
                            writer.println("LOGIN@FAIL@�Բ��𣬷��������������Ѵﵽ���ޣ����Ժ��ԣ�");  
                            writer.flush();  
                            writer.close();  
                            socket.close();  
                        } else {  
                            ClientServiceThread clientServiceThread = new ClientServiceThread(socket);  
                            User user = clientServiceThread.getUser();
                            clientServiceThreads.put(user.description(), clientServiceThread);  
                            listModel.addElement(user.getName());  
                            logMessage(user.description() + "����...");  
  
                            clientServiceThread.start();  
                        }  
                    }  
                } catch(Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
  
        public synchronized void closeThread() throws IOException {  
            this.isRunning = false;  
            serverSocket.close();  
            System.out.println("serverSocket close!!!");  
        }  
    }  
  
    //Client Thread class  
    private class ClientServiceThread extends Thread {  
        private Socket socket;  
        private User user;  
        private BufferedReader reader;  
        private PrintWriter writer;  
        private boolean isRunning;  
  
        private synchronized boolean init() {  
            try {  
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                writer = new PrintWriter(socket.getOutputStream());  
  
                String info = reader.readLine();  
                StringTokenizer tokenizer = new StringTokenizer(info, "@");  
                String type = tokenizer.nextToken();  
                if (!type.equals("LOGIN")) {  
                    sendMessage("ERROR@MESSAGE_TYPE");  
                    return false;  
                }  
  
                user = new User(tokenizer.nextToken());  
                sendMessage("LOGIN@SUCCESS@" + user.description() + "����������ӳɹ���");  
  
                int clientNum = clientServiceThreads.size();  
                if (clientNum > 0) {  
                    //���߸ÿͻ��˻���˭����  
                    StringBuffer buffer = new StringBuffer();  
                    buffer.append("@");  
                    for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                        ClientServiceThread serviceThread = entry.getValue();  
                        buffer.append(serviceThread.getUser().description() + "@");  
                        //���������û����û�����  
                        serviceThread.sendMessage("USER@ADD@" + user.description());  
                    }  
  
                    sendMessage("USER@LIST@" + clientNum + buffer.toString());  
                }  
  
                return true;  
  
            } catch(Exception e) {  
                e.printStackTrace();  
                return false;  
            }  
        }  
  
        public ClientServiceThread(Socket socket) {  
            this.socket = socket;  
            this.isRunning = init();  
            if (!this.isRunning) {  
                logMessage("�����߳̿���ʧ�ܣ�");  
            }  
        }  
  
        public void run() {  
            while (isRunning) {  
                try {  
                    String message = reader.readLine();  
                   // System.out.println("recieve message: " + message);  
                    if (message.equals("LOGOUT")) {  
                        logMessage(user.description() + "����...");  
  
                        int clientNum = clientServiceThreads.size();  
                          
                        //���������û����û��Ѿ�����  
                        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                            entry.getValue().sendMessage("USER@DELETE@" + user.description());  
                        }  
  
                        //�Ƴ����û��Լ��������߳�  
                        listModel.removeElement(user.getName());  
                        clientServiceThreads.remove(user.description());  
  
                       // System.out.println(user.description() + " logout, now " + listModel.size() + " client(s) online...(" + clientServiceThreads.size() + " Thread(s))");  
  
                        close();  
                        return;  
                    } else {  //������Ϣ
                        dispatchMessage(message);  
                    }  
                } catch(Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
  
        public void dispatchMessage(String message) {  
            StringTokenizer tokenizer = new StringTokenizer(message, "@");  
            String type = tokenizer.nextToken();  
            if (!type.equals("MSG")) {  
                sendMessage("ERROR@MESSAGE_TYPE");  
                return;  
            }  
  
            String to = tokenizer.nextToken();  
            String from = tokenizer.nextToken();  
            String content = tokenizer.nextToken();  
  
            logMessage(from + "->" + to + ": " + content);  
            if (to.equals("ALL")) {  
                //send to everyone  
                for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                    entry.getValue().sendMessage(message);  
                }  
            } else {  
                //���͸�ĳһ����  
                if (clientServiceThreads.containsKey(to)) {  
                    clientServiceThreads.get(to).sendMessage(message);  
                } else {  
                    sendMessage("ERROR@INVALID_USER");  
                }  
            }  
        }  
  
        public void close() throws IOException {  
            this.isRunning = false;  
            this.reader.close();  
            this.writer.close();  
            this.socket.close();  
              
        }  
  
        public void sendMessage(String message) {  
            writer.println(message);  
            writer.flush();  
        }  
  
        public User getUser() {  
            return user;  
        }  
    }  
      
    //�ͻ���������
    public static void main(String args[]) {  
        new ServerMain();  
    }
    
}
