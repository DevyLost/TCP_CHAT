package de.DevyLost;

public interface Useradministration {
    public void addUser(String username, char[] password);
    public boolean checkUser(String username, char[] password);
}
