package gamestudio.server.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Table(name = "users")
@Entity
@NamedQuery(name = "User.findByUsername",
            query = "SELECT u FROM User u WHERE u.username=:username")
@NamedQuery(name = "User.findById",
            query = "SELECT u FROM User u WHERE u.ident=:id")
public class User implements Serializable
{
    @Id
    @GeneratedValue
    private int ident;

    @Column(unique = true, nullable = false)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public User() {}

    public User(String username, String passwordHash, Date createdAt)
    {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
