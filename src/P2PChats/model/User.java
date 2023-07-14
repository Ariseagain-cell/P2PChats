package P2PChats.model;

/**
 *
 * @author Zwh
 *  �û���ģ�Ͷ���
 */

public class User {  
    private String name;    //�û���
    private String ipAddr;  //IP��ַ
      
    public User(String userDescription) {  
        String items[] = userDescription.split("%");  //���ַ�����%�ָ�
        this.name = items[0];    //��һ���ָ����û���
        this.ipAddr = items[1];  //�ڶ����ָ���IP��ַ
    }  
  
    public User(String name, String ipAddr) {  
        this.name = name;  
        this.ipAddr = ipAddr;  
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public String getIpAddr() {  
        return ipAddr;  
    }  
  
    public String description() {  
        return name + "%" + ipAddr;  //ͳһ�� ���û����� + ��%�� + ��IP��ַ�� ����ʽ��ʾ
    }  
}  
