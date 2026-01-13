package pl.wsb.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(nullable = false)
    public String password;

    @Column(nullable = false)
    public String role;

    public User() {
    }

    public User(String username, String plainPassword, String role) {
        this.username = username;
        this.password = BcryptUtil.bcryptHash(plainPassword);
        this.role = role;
    }

    public void changePassword(String newPlainPassword) {
        this.password = BcryptUtil.bcryptHash(newPlainPassword);
    }
}