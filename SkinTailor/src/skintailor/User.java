package skintailor;

public class User {
    private String userID;
    private String username;
    private String email;
    private String password;
    private boolean isAdmin;

    // Constructor
    public User(String userID, String username, String email, String password, boolean isAdmin) {
        this.userID = userID;
        this.username = username;
        this.email = email; // 
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // Getters
    public String getUserId() {
        return userID;
    }

    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email; // Getter for email
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    // Setters (if needed)
    public void setUserId(String userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setEmail() {
        this.email = email; // Setter for email
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}