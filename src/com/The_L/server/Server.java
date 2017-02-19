package com.The_L.server;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import com.The_L.Message.MessagePassing;
/**
 *
 * @author rajat_000
 */
class SocketThread extends Thread{
    public Socket socket = null;
    public Server server = null;
    public ServerUI ui;
    public ObjectOutputStream out = null;
    public ObjectInputStream in = null;
    public String username="";
    public int ID = -1;
    
    public SocketThread() {
    }

    public SocketThread(Socket socket, Server server) {
        super();
        this.socket = socket;
        this.server = server;
        ID = this.socket.getPort();
        ui = this.server.ui;
    }
    public void sendMessage(MessagePassing message){
        try{
            out.writeObject(message);
            out.flush();
        }
        catch(IOException ex){
             Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int getID(){
        return ID;
    }

    @Override
    public void run() {
        ui.jTAServer.append("\n Server running : "+ ID);
        while(true)
        {
            try{
                MessagePassing msg = (MessagePassing)in.readObject();
                System.out.println(ID+ " "+ msg);
                server.handle(ID, msg);
            }
            catch(IOException | ClassNotFoundException ex){
                Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
                server.remove(ID);
                stop();        
            }
        }
    }
    public void open()throws IOException{
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        
    }
    public void close()throws IOException{
        
            if(out!=null)
                out.close();
            if(in!=null)
                in.close();
            if(socket!=null)
                socket.close();
         
    }
    
    
}

public class Server implements Runnable{
    public SocketThread clients[];
    public ServerSocket server;
    public Thread thread;
    public ServerUI ui;
    public DB database;
    public int clientCount = 0, port = 59120;
   
    public Server() {
    }
    
    public Server(ServerUI ui) {
        this.ui = ui;
        clients = new SocketThread[50];
        database = new DB(this.ui.filePath);
        try{
            server = new ServerSocket(this.port);
            this.port=server.getLocalPort();
            this.ui.jTAServer.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
            start();
        }
        catch(Exception ex){
            this.ui.jTAServer.append("Can not bind to port : " + port + "\nRetrying"); 
            this.ui.retryStart(0);
	}
    }

    public Server(ServerUI ui, int port) {
        this.ui = ui;
        this.port = port;
        clients = new SocketThread[50];
        database = new DB(this.ui.filePath);
        try{
            server = new ServerSocket(this.port);
            this.port = server.getLocalPort();
            this.ui.jTAServer.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
            start();
        } catch (IOException ex) {
                this.ui.jTAServer.append("\nCan not bind to port " + port + ": " + ex.getMessage()); 
	}
        
    }

    @Override
    public void run() {
        while(thread!= null){
            try{
                ui.jTAServer.append("\nWaiting for a client......");
                addThread(server.accept());
            }
            catch(Exception ex){
                ui.jTAServer.append("\nServer accept error: \n");
                ui.retryStart(0);
            }
        }
    }
    
    public void start(){
        if(thread == null){
            thread = new Thread(this);
            thread.start();
        }
    }
    
    public void stop(){
        if(thread != null){
            thread.stop();
            thread = null;
        }
    }
    private int getClient(int ID){
        for(int i=0;i<clientCount;i++){
            if(clients[i].getID()== ID)
                return i;
        }
        return -1;
    }
    public void broadCast(String type,String content,String sender){
        MessagePassing message = new MessagePassing(type, sender,"ALL", content);
        for (int i=0; i<clientCount;i++){
            clients[i].sendMessage(message);
        }
    }
    public SocketThread getUserThread(String user){
         for (int i=0; i<clientCount;i++){
            if (clients[i].username.equals(user))
                return clients[i];
        }
        return null;
    }
    public void getUserList(String towhom){
         for (int i=0; i<clientCount;i++){
             getUserThread(towhom).sendMessage(new MessagePassing("New User","Server",towhom,clients[i].username));
        }
     }
    public synchronized void handle(int ID,MessagePassing msg){
        if (msg.content.equals(".bye")){
            broadCast("signout",  msg.sender, "SERVER");
            remove(ID); 
	}
	else{
            if(msg.type.equals("login")){
                if(getUserThread(msg.sender) == null){
                    if(database.chkLogin(msg.sender, msg.content)){
                        clients[getClient(ID)].username = msg.sender;
                        clients[getClient(ID)].sendMessage(new MessagePassing("login", "SERVER", msg.sender,"TRUE" ));
                        broadCast("newuser", msg.sender, "SERVER");
                        getUserList(msg.sender);
                    }
                    else{
                        clients[getClient(ID)].sendMessage(new MessagePassing("login", "SERVER",msg.sender, "FALSE"));
                    } 
                }
                else{
                    clients[getClient(ID)].sendMessage(new MessagePassing("login", "SERVER",  msg.sender,"FALSE"));
                }
            }
            else if(msg.type.equals("message")){
                if(msg.reciever.equals("ALL")){
                    broadCast("message", msg.content, msg.sender);
                }
                else{
                    getUserThread(msg.reciever).sendMessage(new MessagePassing(msg.type, msg.sender, msg.reciever, msg.content));
                    clients[getClient(ID)].sendMessage(new MessagePassing(msg.type, msg.sender, msg.reciever, msg.content));
                }
            }
            else if(msg.type.equals("test")){
                clients[getClient(ID)].sendMessage(new MessagePassing("test", "SERVER", msg.sender, "OK"));
            }
            else if(msg.type.equals("signup")){
                if(getUserThread(msg.sender) == null){
                    if(!database.isUser(msg.sender)){
                        database.addUser(msg.sender, msg.content);
                        clients[getClient(ID)].username = msg.sender;
                        clients[getClient(ID)].sendMessage(new MessagePassing("signup", "SERVER", msg.sender, "TRUE"));
                        clients[getClient(ID)].sendMessage(new MessagePassing("login", "SERVER", msg.sender, "TRUE"));
                        broadCast("newuser", msg.sender,"SERVER");
                        getUserList(msg.sender);
                    }
                    else{
                        clients[getClient(ID)].sendMessage(new MessagePassing("signup", "SERVER", msg.sender, "FALSE"));
                    }
                }
                else{
                    clients[getClient(ID)].sendMessage(new MessagePassing("signup", "SERVER", msg.sender, "FALSE"));
                }
            }
            else if(msg.type.equals("upload_req")){
                if(msg.reciever.equals("All")){
                    clients[getClient(ID)].sendMessage(new MessagePassing("message", "SERVER", msg.sender, "Uploading to 'All' forbidden"));
                }
                else{
                   getUserThread(msg.reciever).sendMessage(new MessagePassing("upload_req", msg.sender, msg.reciever, msg.content));
                }
            }
            else if(msg.type.equals("upload_res")){
                if(!msg.content.equals("NO")){
                    String IP = getUserThread(msg.sender).socket.getInetAddress().getHostAddress();
                    getUserThread(msg.reciever).sendMessage(new MessagePassing("upload_res", IP, msg.reciever, msg.content));
                }
                else{
                    getUserThread(msg.reciever).sendMessage(new MessagePassing("upload_res", msg.sender, msg.reciever, msg.content));
                }
            }
	}
    }
    public synchronized void remove(int port){
        int pos = getClient(port);
        if(pos>0){
            SocketThread toRemove = clients[pos];
            ui.jTAServer.append("\n Removing Client Thread : "+ port+" at "+pos);
             if (pos < clientCount-1){
                for (int i = pos+1; i < clientCount; i++){
                    clients[i-1] = clients[i];
	        }
	    }
	    clientCount--;
	    try{  
	      	toRemove.close(); 
	    }
	    catch(IOException ioe){  
	      	ui.jTAServer.append("\nError closing thread: " + ioe); 
	    }
	    toRemove.stop(); 
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length){  
            ui.jTAServer.append("\nClient accepted: " + socket);
	    clients[clientCount] = new SocketThread(socket,this);
	    try{  
	      	clients[clientCount].open(); 
	        clients[clientCount].start();  
	        clientCount++; 
	    }
	    catch(IOException ioe){  
	      	ui.jTAServer.append("\nError opening thread: " + ioe); 
	    } 
	}
	else{
            ui.jTAServer.append("\nClient refused: maximum " + clients.length + " reached.");
	}
    }
    
}
