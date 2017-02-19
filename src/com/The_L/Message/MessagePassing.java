/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.The_L.Message;


import java.io.Serializable;

public class MessagePassing implements Serializable{
    
    private static final long serialVersionUID = 1L;
    public String type, sender, content, reciever;
    
    public MessagePassing(String type, String sender, String reciever, String content){
        this.type = type; 
        this.sender = sender; 
        this.content = content; 
        this.reciever = reciever;
    }
    
    @Override
    public String toString(){
        return "{type='"+type+"', sender='"+sender+"', reciever='"+reciever+"', content='"+content+"'}";
    }
}

