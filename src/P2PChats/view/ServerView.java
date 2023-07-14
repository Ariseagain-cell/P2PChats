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


public class ServerView {
	
	//UI  
	protected JFrame frame;  
	protected JPanel settingPanel,                //�������
					 messagePanel;  			  //��Ϣ���
	protected JSplitPane centerSplitPanel;        //�ָ����
	protected JScrollPane userPanel,              //����û����
						  logPanel;               //�ұ���Ϣ��
	protected JTextArea logTextArea;              //��������־
	protected JTextField maxClientTextField, 	  //��������
						 portTextField;           //�˿ں�
	protected JTextField serverMessageTextField;  //�㲥��Ϣ�����
	protected JButton startButton, 				  //������ť
					  stopButton, 				  //ֹͣ��ť
					  sendButton;  				  //���Ͱ�ť
	protected JList userList;                     //��̬�仯���û��б�
    
    //Model  
	protected DefaultListModel<String> listModel;
    
	//���캯��
    public ServerView() {  
        initUI();  
    }
    
    //UI��ʼ������  
    @SuppressWarnings("unchecked")
	private void initUI() {  
    	
    	//���÷���˴��ڱ��⡢Ĭ�ϴ�С�Լ�����
        frame = new JFrame("������");  
        frame.setSize(600, 400);  
        frame.setResizable(false);  
        frame.setLayout(new BorderLayout());  
          
        //������������壨����Ĭ�ϲ�����  
        maxClientTextField = new JTextField("10");  
        portTextField = new JTextField("3333");
        startButton = new JButton("����");  
        stopButton = new JButton("ֹͣ");  
  
        settingPanel = new JPanel();  
        settingPanel.setLayout(new GridLayout(1, 6));  //���ò���Ϊһ������
        settingPanel.add(new JLabel("��������"));  
        settingPanel.add(maxClientTextField);  
        settingPanel.add(new JLabel("�˿ں�"));  
        settingPanel.add(portTextField);  
        settingPanel.add(startButton);  
        settingPanel.add(stopButton);  
        settingPanel.setBorder(new TitledBorder("����������"));  //���ñ���
  
        //�����û����  
        listModel = new DefaultListModel<String>();  
  
        userList = new JList(listModel);  
        userPanel = new JScrollPane(userList);  
        userPanel.setBorder(new TitledBorder("�����û�"));  
  
        //��������־���  
        logTextArea = new JTextArea();  
        logTextArea.setEditable(false);  
        logTextArea.setForeground(Color.blue);  //����Ĭ��������ɫΪ��ɫ
  
        logPanel = new JScrollPane(logTextArea);  
        logPanel.setBorder(new TitledBorder("��������־"));  
  
        //������Ϣ���  
        serverMessageTextField = new JTextField();  
        sendButton = new JButton("����");  
  
        messagePanel = new JPanel(new BorderLayout());  
        messagePanel.add(serverMessageTextField, "Center");  
        messagePanel.add(sendButton, "East");  
        messagePanel.setBorder(new TitledBorder("�㲥��Ϣ"));  
  
  
        //���м������û�����������Ϣ����������    
        centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, logPanel);  
        centerSplitPanel.setDividerLocation(100);  //���÷ָ��������100px
  
        frame.add(settingPanel, "North");  
        frame.add(centerSplitPanel, "Center");  
        frame.add(messagePanel, "South");  
        frame.setVisible(true);    
  
        serviceUISetting(false);  //���ð�ť�Լ��ı����Ĭ��״̬
    }  
  
    protected void serviceUISetting(boolean started) {  
        maxClientTextField.setEnabled(!started);  
        portTextField.setEnabled(!started);  
        startButton.setEnabled(!started);  
        stopButton.setEnabled(started);  
        serverMessageTextField.setEnabled(started);  
        sendButton.setEnabled(started);  
    }
       
}
