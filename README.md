# P2pChats
基于P2P的局域网即时通信系统
## 项目要求

1．实现一个图形用户界面局域网内的消息系统。

2．功能：建立一个局域网内的简单的P2P消息系统，程序既是服务器又是客户，服务器端口使用3333。

  2.1 用户注册及对等方列表的获取：对等方A启动后，用户设置自己的信息（用户名，所在组）；扫描网段中在线的对等方（3333端口打开），向所有在线对等方的服务端口发送消息，接收方接收到消息后，把对等方A加入到自己的用户列表中，并发应答消息；对等方A把回应消息的其它对等方加入用户列表。双方交换的消息格式自己根据需要定义，至少包括用户名、IP地址。

  2.2 发送消息和文件：用户在列表中选择用户，与用户建立TCP连接，发送文件或消息。

3．用户界面：界面上包括对等方列表；消息显示列表；消息输入框；文件传输进程显示及操作按钮或菜单。

## 开发工具及运行环境

1.编程语言：java

2.开发工具：IntelliJ IDEA 2019.2.3

3.JDK version 11.0.4

4.运行平台：windows10

## 关键性代码
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
                sendMessage("LOGIN@SUCCESS@" + user.description() + "与服务器连接成功！");  
                int clientNum = clientServiceThreads.size();  
                if (clientNum > 0) {  
                    //告诉该客户端还有谁在线  
                    StringBuffer buffer = new StringBuffer();  
                    buffer.append("@");  
                    for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                        ClientServiceThread serviceThread = entry.getValue();  
                        buffer.append(serviceThread.getUser().description() + "@");  
                        //告诉其他用户此用户在线  
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
                logMessage("服务线程开启失败！");  
            }  
        }  
        public void run() {  
            while (isRunning) {  
                try {  
                    String message = reader.readLine();  
                   // System.out.println("recieve message: " + message);  
                    if (message.equals("LOGOUT")) {  
                        logMessage(user.description() + "下线...");  
  
                        int clientNum = clientServiceThreads.size();  
                          
                        //告诉其他用户该用户已经下线  
                        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                            entry.getValue().sendMessage("USER@DELETE@" + user.description());  
                        }  
                        //移除该用户以及服务器线程  
                        listModel.removeElement(user.getName());  
                        clientServiceThreads.remove(user.description());  

                        close();  
                        return;  
                    } else {  //发送消息
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
                //发送给某一个人  
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

## 经典的TCP通信服务器客户端架构

服务器有一个服务器等待用户连接的线程，该线程循环等待客户端的TCP连接请求。一旦用ServerSocket.accept()捕捉到了连接请求，就为该TCP连接分配一个客户服务线程，通过该消息传递线程服务器与客户端通信。服务器发送消息通过该客户服务线程的方法在主线程完成，而接收消息全部在客户服务线程中循环接收并处理。

客户机能发起一个向服务器的socket连接请求，一旦收到服务器成功响应连接请求，客户机便为这个socket分配一个消息接收线程，否则关闭该socket。和服务器任务分配类似，发送消息作为非常用方法在主线程中完成，而接收消息在消息接收线程中不停刷新并作相应处理。

## BUG
目前发现的一个Bug是服务器关闭时，我关了服务器接收Socket请求的线程并close了该ServerSocket，但是该线程仍然继续执行了一次ServerSocket.accept()。我尝试用了synchronized方法并判断ServerSocket是否关闭，但这个异常还是会出现。我捕捉了该异常，仅仅printStackTrace而没有做其他错误处理，幸运的是这小Bug并不影响服务器关闭，所有客户端都能正确的接收服务器关闭的消息。

