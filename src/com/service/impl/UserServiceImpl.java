package com.service.impl;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import com.bean.User;
import com.dao.UserDao;
import com.service.UserService;

public class UserServiceImpl implements UserService {

    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /** MD5 加密工具 */
    public static String md5(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(src.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 error", e);
        }
    }

    /** 生成随机 salt（32位十六进制） */
    private static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] b = new byte[16];
        sr.nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    /**
     * 密码哈希：salt 存在则 md5(pwd+salt)，否则纯 md5（兼容老用户）
     */
    private static String hashPassword(String plain, String salt) {
        if (salt != null && !salt.isEmpty()) {
            return md5(plain + salt);
        }
        return md5(plain);
    }

    @Override
    public String register(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "账号不能为空";
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return "密码至少6位";
        }
        if (userDao.findByUsername(user.getUsername()) != null) {
            return "账号已存在";
        }
        // 加盐 + 哈希
        String salt = generateSalt();
        user.setSalt(salt);
        user.setPassword(hashPassword(user.getPassword(), salt));
        user.setRole(0);
        user.setStatus(1);
        userDao.insert(user);
        return null;
    }

    @Override
    public User login(String username, String password) {
        // 先按用户名查（拿到 salt）
        User u = userDao.findByUsername(username);
        if (u == null) return null;
        if (u.getStatus() != null && u.getStatus() == 0) return null; // 禁用
        // 加盐哈希后比对
        String expected = hashPassword(password, u.getSalt());
        if (!expected.equals(u.getPassword())) return null;
        return u;
    }

    @Override
    public User findById(Integer id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public void updateNickname(Integer id, String nickname) {
        User u = userDao.findById(id);
        if (u != null) {
            u.setNickname(nickname);
            userDao.update(u);
        }
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        User u = userDao.findById(id);
        if (u != null) {
            u.setStatus(status);
            userDao.update(u);
        }
    }

    @Override
    public void updateRole(Integer id, Integer role) {
        User u = userDao.findById(id);
        if (u != null) {
            u.setRole(role);
            userDao.update(u);
        }
    }

    @Override
    public int count() {
        return userDao.count();
    }
}
