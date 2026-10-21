package com.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;         // 主键
    private String  username;   // 登录账号
    private String  password;   // 密码(加盐MD5)
    private String  salt;       // 密码盐值
    private String  nickname;   // 昵称
    private Integer role;       // 0=普通用户, 1=管理员
    private Integer status;     // 0=禁用, 1=正常
    private Date    createTime; // 注册时间

    // ========== 构造器 ==========
    public User() {}

    // ========== Getter / Setter ==========
    public Integer getId()        { return id; }
    public void    setId(Integer id)              { this.id = id; }

    public String  getUsername()  { return username; }
    public void    setUsername(String username)   { this.username = username; }

    public String  getPassword()  { return password; }
    public void    setPassword(String password)   { this.password = password; }

    public String  getSalt()      { return salt; }
    public void    setSalt(String salt)          { this.salt = salt; }

    public String  getNickname()  { return nickname; }
    public void    setNickname(String nickname)   { this.nickname = nickname; }

    public Integer getRole()      { return role; }
    public void    setRole(Integer role)          { this.role = role; }

    public Integer getStatus()    { return status; }
    public void    setStatus(Integer status)      { this.status = status; }

    public Date    getCreateTime(){ return createTime; }
    public void    setCreateTime(Date createTime) { this.createTime = createTime; }

    // ========== 业务方法 ==========
    /** 判断是否管理员 */
    public boolean isAdmin() {
        return role != null && role == 1;
    }

    /** 判断是否正常 */
    public boolean isActive() {
        return status != null && status == 1;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', nickname='" + nickname 
             + "', role=" + role + ", status=" + status + "}";
    }
}
