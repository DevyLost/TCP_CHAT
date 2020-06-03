package de.DevyLost;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

class ClientWorker implements Runnable {
  private Socket client;
  private JTextArea textArea;
  private PrintWriter out;
  private Useradmin admin;

  private static ArrayList<ClientWorker> workerList = new ArrayList<ClientWorker>();
  private static HashMap<String, Long> IPTimes = new HashMap<String, Long>();
  private static synchronized void addWorker(ClientWorker cw){
    workerList.add(cw);
  }
  private static boolean isLoginAllowed(String ip, long durms, long nowms){
    if(IPTimes.containsKey(ip)){
      long lastms = IPTimes.get(ip).longValue();
      if (nowms-lastms < durms){
        return false;
      }
    }
    return true;
  }
  private static synchronized void logFailedLogin(String ip, long nowms){
    IPTimes.put(ip, new Long(nowms));
  }

  ClientWorker(Socket client, JTextArea textArea) {
    this.client = client;
    this.textArea = textArea;
    try{
      this.out = new PrintWriter(client.getOutputStream(), true);
    } catch (IOException e) {
      System.out.println("out failed");
      System.exit(-1);
    }
    this.admin = new Useradmin();
  }

  private static synchronized void send(String line){
    ListIterator<ClientWorker> iter = workerList.listIterator();
    while(iter.hasNext()){
      ClientWorker cw = iter.next();
      if(cw.client.isConnected()){
        cw.write(line);
      }
      else{
        try{
          iter.remove();
        }catch(IllegalStateException e){
        }
      }
    }
  }

  private void write(String line){
    this.out.println(line);
  }
/*
  private boolean isCharIn(char c, String set){
    return set.indexOf(c) != -1;
  }
*/
  private char[] readPass(BufferedReader in) throws IOException{
    char[] pass = new char[1024];
    int inInt = in.read();
    int charCount = 0;
    while(inInt != -1 && charCount < 1024){
      char inChar = (char)inInt;
      //this.textArea.append("#"+inInt);
      if(!Character.isWhitespace(inInt)){
        pass[charCount] = inChar;
        charCount++;
        inInt = in.read();
      }else{
        in.read();
        break;
      }
    }
    char[] retPass = new char[charCount];
    System.arraycopy(pass, 0, retPass, 0, charCount);
    Arrays.fill(pass, Character.MIN_VALUE);
    return retPass;
  }

  public void run(){
    String line;
    BufferedReader in = null;
    String user;
    boolean check = false;
    boolean allowed = false;
    long now = 0;
    String ip = this.client.getInetAddress().getHostAddress();
    try{
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
    } catch (IOException e) {
      System.out.println("in failed");
      System.exit(-1);
    }
    while(!check){
      allowed = false;
      try{
        while(!allowed){
          write("username:");
          user = in.readLine();
          write("password:");
          char[] pass = readPass(in);
          now = System.currentTimeMillis();
          allowed = isLoginAllowed(ip, 10000, now);
          if(!allowed){
            System.out.println("UNACCEPTABLE!!");
          }else{
            check = this.admin.checkUser(user,pass);
          }
          Arrays.fill(pass, Character.MIN_VALUE);
        }
      } catch (IOException e) {
        System.out.println("in failed");
        System.exit(-1);
      }
      if(check){
        addWorker(this);
        write("password accepted");
      }else{
        logFailedLogin(ip, now);
        write("password not accepted");
      }
    }
    while(check){
      try{
        line = in.readLine();
        if(line == null){
          workerList.remove(this);
          return;
        }
//Send data back to all clients
        send(line);
        textArea.append(line);
      } catch (IOException e) {
        System.out.println("Read failed");
         System.exit(-1);
       }
    }
  }
}

