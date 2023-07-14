package P2PChats.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


public class ClientView {
	//UI  
	protected JFrame frame;
	protected JPanel settingPanel,         //�������
				     messagePanel;  	   //��Ϣ���
	protected JSplitPane centerSplitPanel; //�ָ����
	protected JScrollPane userPanel,	   //����û����
						  messageBoxPanel; //�ұ���Ϣ��
	protected JTextArea messageTextArea;   //��Ϣ�༭��
	protected JTextField nameTextField,    //�û��������
						 ipTextField;	   //������IP��ַ�����
	protected JTextField portTextField;	   //�˿������
	protected JTextField messageTextField; //��Ϣ�༭��
	protected JLabel messageToLabel;       //To..��ǩ
	protected JButton connectButton, 	   //���Ӱ�ť
					  disconnectButton,    //�Ͽ���ť
	                  sendButton;  		   //���Ͱ�ť
	protected JList userList;  			   //��̬�仯���û��б�
    
    //Model  
	protected DefaultListModel<String> listModel;  
    
    //���캯��
    public ClientView() {  
        initUI();  
    } 
    
    //UI��ʼ������
    private void initUI() {  
    	
    	//���ÿͻ��˴��ڱ��⡢��С�Լ�����
        frame = new JFrame("�ͻ���");  
        frame.setSize(600, 400);  
        frame.setResizable(false);  
        frame.setLayout(new BorderLayout());  
          
        //��������ʼ����  
        ipTextField = new JTextField("192.168.31.87");
        portTextField = new JTextField("3333");
        nameTextField = new JTextField("������");
        connectButton = new JButton("����");  
        disconnectButton = new JButton("�Ͽ�");  
  
        //�������
        settingPanel = new JPanel();  
        settingPanel.setLayout(new GridLayout(1, 8));  //���ò���Ϊһ�а���
        settingPanel.add(new JLabel("         ����:")); //Ϊ�������������
        settingPanel.add(nameTextField);  
        settingPanel.add(new JLabel("  ������IP:"));  
        settingPanel.add(ipTextField);  
        settingPanel.add(new JLabel("  �˿ں�:"));  
        settingPanel.add(portTextField);  
        settingPanel.add(connectButton);  
        settingPanel.add(disconnectButton);  
        settingPanel.setBorder(new TitledBorder("�ͻ�������")); //��������������
  
        //�����û����  
        listModel = new DefaultListModel<String>();  
        userList = new JList(listModel);  
        userPanel = new JScrollPane(userList);  
        userPanel.setBorder(new TitledBorder("�����û�"));  //���������û�������
  
        //������Ϣ���  
        messageTextArea = new JTextArea();  
        messageTextArea.setEditable(false);        //���ø����򲻿ɱ༭
        messageTextArea.setForeground(Color.blue); //��������Ĭ����ɫΪ��ɫ
  
        messageBoxPanel = new JScrollPane(messageTextArea);   //����Ϊ�����������ı���
        messageBoxPanel.setBorder(new TitledBorder("������Ϣ")); //���ñ��� 
  
        //������Ϣ���  
        messageToLabel = new JLabel("To:������  ");   //Ĭ��Ϊ���͸�������
        messageTextField = new JTextField();  
        sendButton = new JButton("����");  
  
        messagePanel = new JPanel(new BorderLayout());  //����������������
        messagePanel.add(messageToLabel, "West");  
        messagePanel.add(messageTextField, "Center");  
        messagePanel.add(sendButton, "East");  
        messagePanel.setBorder(new TitledBorder("������Ϣ"));  
  
        //���м������û�����������Ϣ����������  
        centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, messageBoxPanel);  
        centerSplitPanel.setDividerLocation(100);  //���÷ָ��������100px
  
        frame.add(settingPanel, "North");  
        frame.add(centerSplitPanel, "Center");  
        frame.add(messagePanel, "South");  
        frame.setVisible(true);  
   
        serviceUISetting(false); //���ð�ť�Լ��ı����Ĭ��״̬
    }
    
    public void serviceUISetting(boolean connected) {  
        nameTextField.setEnabled(!connected);  
        ipTextField.setEnabled(!connected);  
        portTextField.setEnabled(!connected);  
        connectButton.setEnabled(!connected);  
        disconnectButton.setEnabled(connected);  
        messageTextField.setEnabled(connected);  
        sendButton.setEnabled(connected);  
    }
    
}
