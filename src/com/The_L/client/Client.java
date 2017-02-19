
package com.The_L.client;


import com.The_L.Message.MessagePassing;
import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author rajat_000
 */
public class Client implements Runnable{
    public int port;
    public String serverAddress;
    public Socket socket;
    public ChatUI ui;
    public ConnectToServer cts;
    public ObjectInputStream In;
    public ObjectOutputStream Out;

    public Client(ConnectToServer cts)throws IOException{
        this.cts = cts;
        this.ui = cts.ui;
        this.serverAddress = cts.serverAddr;
        this.port = cts.port;
        socket = new Socket(InetAddress.getByName(serverAddress), port);
            
        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());
           
    }
    
    public void sendMessage(MessagePassing msg){
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : "+msg.toString());
        } 
        catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }
    public void run() {
        boolean keepRunning = true;
        while(keepRunning){
            try {
                MessagePassing msg = (MessagePassing) In.readObject();
                System.out.println("Incoming : "+msg.toString());
                
                if(msg.type.equals("message")){
                    if(msg.reciever.equals(cts.username)){
                        ui.jTInbox.append("["+msg.sender +" > Me] : " + msg.content + "\n");
                    }
                    else{
                        ui.jTInbox.append("["+ msg.sender +" > "+ msg.reciever +"] : " + msg.content + "\n");
                    }
                }
                else if(msg.type.equals("login")){
                    if(msg.content.equals("TRUE")){
                        cts.jBSignup.setEnabled(false); 
                        cts.jBLogin.setEnabled(false);
                        JOptionPane.showMessageDialog(null, "Login Successful");
                        cts.setVisible(false);
                        ui.setVisible(true);
                    }
                    else{
                          JOptionPane.showMessageDialog(null, "Login Failed");
                    }
                }
                else if(msg.type.equals("test")){
                    cts.jBConnect.setEnabled(false);
                    cts.jBConnect.setText("Connected");
                    cts.jBLogin.setEnabled(true); 
                    cts.jBSignup.setEnabled(true);
                    cts.jTUserName.setEditable(true);
                    cts.jTPassword.setEditable(true);
                    cts.jTHost.setEditable(false);
                    cts.jTPort.setEditable(false);
                    
                }
                else if(msg.type.equals("newuser")){
                    if(!msg.sender.equals(cts.username)){
                        boolean exists = false;
                        for(int i = 0; i < ui.model.getSize(); i++){
                            if(ui.model.getElementAt(i).equals(msg.content)){
                                exists = true;
                                break;
                            }
                        }
                        if(!exists){ 
                            ui.model.addElement(msg.content);
                            ui.jTInbox.append("Welcome "+ msg.content+" to THE L CHAT \n");
                        }
                    }
                }
                else if(msg.type.equals("New User")){
                    if(!msg.sender.equals(cts.username)){
                        boolean exists = false;
                        for(int i = 0; i < ui.model.getSize(); i++){
                            if(ui.model.getElementAt(i).equals(msg.content)){
                                exists = true;
                                break;
                            }
                        }
                        if(!exists){ 
                            ui.model.addElement(msg.content);
                         }
                    }
                }
                else if(msg.type.equals("signup")){
                    if(msg.content.equals("TRUE")){
                        cts.jBSignup.setEnabled(false); 
                        cts.jBLogin.setEnabled(false);
                        JOptionPane.showMessageDialog(null, "Singup Successful\n");
                        cts.setVisible(false);
                        ui.setVisible(true);
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Signup Failed\n");
                    }
                }
                else if(msg.type.equals("signout")){
                    if(msg.content.equals(cts.username)){
                        cts.jTHost.setEnabled(true); 
                        cts.jTPort.setEnabled(true); 
                        cts.jBConnect.setEnabled(true); 
                        cts.jBSignup.setEnabled(false); 
                        cts.jBLogin.setEnabled(false);
                        JOptionPane.showMessageDialog(null, "Singout Successful\n");
                        ui.setVisible(false);
                        cts.setVisible(true);
                       
                        for(int i = 1; i < ui.model.size(); i++){
                            ui.model.removeElementAt(i);
                        }
                        
                        cts.clientThread.stop();
                    }
                    else{
                        ui.model.removeElement(msg.content);
                        ui.jTInbox.append("["+ msg.content +" > All] : "+ msg.content +" has signed out\n");
                    }
                }
                else{
                    ui.jTInbox.append("[SERVER > Me] : Unknown message type\n");
                }
            }
            catch(Exception ex) {
                keepRunning = false;
                 cts.jTHost.setEnabled(true); 
                 cts.jTPort.setEnabled(true); 
                 cts.jBConnect.setEnabled(true); 
                 cts.jBSignup.setEnabled(false); 
                 cts.jBLogin.setEnabled(false);
                 JOptionPane.showMessageDialog(null, "Connection Failure. Please Try Again Later\n");
                        
                for(int i = 1; i < ui.model.size(); i++){
                    ui.model.removeElementAt(i);
                }
                
                cts.clientThread.stop();
                
                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }    
    }
    public void closeThread(Thread t){
        t = null;
    }
}
