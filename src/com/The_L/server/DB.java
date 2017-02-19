
package com.The_L.server;

import java.io.*;
import java.util.logging.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
/**
 *
 * @author rajat-dhyani
 */
public class DB
{
    private String filepath;
    public DB(){
        
    }
    public DB(String filepath){
        this.filepath = filepath;
    }
    public boolean isUser(String username){
        
        try {
            File xml = new File(filepath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xml);
            doc.getDocumentElement().normalize();
            
            NodeList nl = doc.getElementsByTagName("user");
            int length = nl.getLength();
            for( int i=0; i< length; i++){
                Node node = nl.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE){
                    Element ele = (Element) node;
                    if( getTagValue("username", ele).equals(username))
                        return true;
                }
            }
            
            return false;
        } 
        catch (ParserConfigurationException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (SAXException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean chkLogin(String username, String password){
        
        try {
            File xml = new File(filepath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xml);
            doc.getDocumentElement().normalize();
            
            NodeList nl = doc.getElementsByTagName("user");
            int length = nl.getLength();
            for( int i=0; i< length; i++){
                Node node = nl.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE){
                    Element ele = (Element) node;
                    if( getTagValue("username", ele).equals(username) && getTagValue("password", ele).equals(password))
                        return true;
                }
            }
            
            return false;
        } 
        catch (ParserConfigurationException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (SAXException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
      
    }
    public void addUser(String username, String password){
       
        try
        {
            File xml = new File(filepath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xml);
            
            Node node = doc.getFirstChild();
            
            Element newuser = doc.createElement("user");
            Element newusername = doc.createElement("username");
            Element newpassword = doc.createElement("password");
            
            newusername.setTextContent(username);
            newpassword.setTextContent(password);
            
            newuser.appendChild(newusername);
            newuser.appendChild(newpassword);
            
            node.appendChild(newuser);
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            DOMSource dom = new DOMSource(node);
            StreamResult sr = new StreamResult(xml);
            t.transform(dom, sr);
        }
       catch (ParserConfigurationException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (SAXException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
         } 
        catch (IOException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
         } catch (TransformerConfigurationException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String getTagValue(String str, Element ele) {
	NodeList list = ele.getElementsByTagName(str).item(0).getChildNodes();
        Node value = (Node) list.item(0);
	return value.getNodeValue();
  }
    
}
