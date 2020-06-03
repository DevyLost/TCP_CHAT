package de.DevyLost;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Arrays;
import java.util.Scanner; // Import the Scanner class to read text files
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.Console;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.DevyLost.Useradministration;


public class Useradmin implements Useradministration{

    public static void main(String[] args) {

        Console con = System.console();
        if(args.length == 2){
            String function = args[0];
            String username = args[1];
            Useradmin admin = new Useradmin();
            char[] password = null;
            if(function.equalsIgnoreCase("adduser")){
                con.printf("Bitte Passwort fuer neuen Nutzer eingeben.\n");
                password = con.readPassword();
                admin.addUser(username, password);
            }else if(function.equalsIgnoreCase("checkuser")){
                con.printf("Bitte Passwort des Nutzers eingeben.\n");
                password = con.readPassword();
                if(admin.checkUser(username, password)){
                    con.printf("Diese Angaben sind korrekt.");
                }else
                    con.printf("Diese Angaben sind nicht korrekt.");
            }
            if (password != null)
                Arrays.fill(password, Character.MIN_VALUE);
        }else
            showHelp();
    }

    public static void showHelp(){

    }

    private static void debugMessage(String message){
        if(true)
            System.out.println(message);
    }
    private static void functionMessage(String function, String args){
        if(true)
            System.out.println("Entering ".concat(function).concat("(").concat(args).concat(")"));
    }

    public boolean checkUserExists(String username){
        functionMessage("checkUserExists", "".concat(username));
        File passfile = new File("passwords.txt");
        if (!passfile.exists()) {
            return false;
        }
        Scanner userReader;
        try {
            userReader = new Scanner(passfile);
        }catch (IOException e) {
            debugMessage("An error occurred.");
            e.printStackTrace();
            return false;
        }
        Pattern pattern = Pattern.compile("^".concat(username).concat(" .*$"));
        while(userReader.hasNextLine()) {
            String curLine = userReader.nextLine();
            Matcher m = pattern.matcher(curLine);
            if(m.matches())
                return true;
        }
        return false;
    }

    public void addUser(String username, char[] password){
        functionMessage("addUser", "".concat(username).concat(", ").concat("***"));
        try {
            File passfile = new File("passwords.txt");
            if(!passfile.exists()){
                if (passfile.createNewFile()) {
                    debugMessage("File created: " + passfile.getName());
                } else {
                    debugMessage("File already exists.");
                    return;
                }
            }
            if(checkUserExists(username))
                return;
            else{
                String csalt = PasswordHasher.secureSalt(512);
                if(csalt == "")
                    return;
                String sechash = PasswordHasher.secureHash(password, csalt, 5000);
                FileWriter userWriter = new FileWriter("passwords.txt");
                String userLine = username.concat(" ").concat(csalt).concat(" ").concat(sechash).concat("\n");
                userWriter.write(userLine);
                userWriter.flush();
                userWriter.close();

            }
        } catch (IOException e) {
            debugMessage("An error occurred.");
            e.printStackTrace();
        }
    }

    public boolean checkUser(String username, char[] password){
        functionMessage("checkUser", "".concat(username).concat(", ").concat("***"));
        File passfile = new File("passwords.txt");
        if (!passfile.exists()) {
            return false;
        }
        Scanner userReader;
        try {
            userReader = new Scanner(passfile);
        }catch (IOException e) {
            debugMessage("An error occurred.");
            e.printStackTrace();
            return false;
        }
        Pattern pattern = Pattern.compile("^".concat(username).concat(" .*$"));
        while(userReader.hasNextLine()) {
            String curLine = userReader.nextLine();
            Matcher m = pattern.matcher(curLine);
            if(m.matches()){
                String userLine = curLine;
                String[] userInfo = userLine.split(" ");
                String thash = PasswordHasher.secureHash(password, userInfo[1], 5000);
                String fhash = userInfo[2];
                if(thash.equals(fhash)){
                    return true;
                }else
                    return false;
            }
        }
        return false;
    }
}
