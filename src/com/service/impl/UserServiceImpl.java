package com.service.impl;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.bean.User;
import com.dao.UserDao;
import com.service.UserService;

public class UserServiceImpl implements UserService {

    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    // ===== SEC-4 暴力破解防护 =====
    // username → long[0]=failCount, long[1]=firstFailTs(毫秒)
    // 5 次失败后锁定 15 分钟（900000ms）
    private static final int   LOCK_MAX_FAIL = 5;
    private static final long  LOCK_COOLDOWN_MS = 15L * 60L * 1000L;
    private static final ConcurrentHashMap<String, long[]> LOGIN_FAIL_MAP = new ConcurrentHashMap<>();

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
    public boolean isLoginLocked(String username) {
        long[] record = LOGIN_FAIL_MAP.get(username);
        if (record == null) return false;
        // 超过冷却期，自动解锁
        if (System.currentTimeMillis() - record[1] > LOCK_COOLDOWN_MS) {
            LOGIN_FAIL_MAP.remove(username);
            return false;
        }
        return record[0] >= LOCK_MAX_FAIL;
    }

    @Override
    public User login(String username, String password) {
        // 先查锁定
        if (isLoginLocked(username)) {
            return null; // DoLoginController 会额外检查 isLoginLocked 并显示锁定提示
        }
        // 先按用户名查（拿到 salt）
        User u = userDao.findByUsername(username);
        if (u == null) {
            registerFail(username);
            return null;
        }
        if (u.getStatus() != null && u.getStatus() == 0) {
            registerFail(username);
            return null;
        }
        // 加盐哈希后比对
        String expected = hashPassword(password, u.getSalt());
        if (!expected.equals(u.getPassword())) {
            registerFail(username);
            return null;
        }
        // 登录成功，清掉失败计数
        LOGIN_FAIL_MAP.remove(username);
        return u;
    }

    /** 记录一次登录失败 */
    private void registerFail(String username) {
        long now = System.currentTimeMillis();
        long[] old = LOGIN_FAIL_MAP.get(username);
        if (old == null || now - old[1] > LOCK_COOLDOWN_MS) {
            // 新周期：或首次失败，或冷却期已过 → 重置
            LOGIN_FAIL_MAP.put(username, new long[]{1, now});
        } else {
            old[0]++; // 复用数组引用，ConcurrentHashMap 无需 replace
        }
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
        // PERF-2 修复：直接一条 UPDATE，避免先 findById 再 update 的 N+1
        userDao.updateStatus(id, status);
    }

    @Override
    public void updateRole(Integer id, Integer role) {
        // PERF-2 修复：直接一条 UPDATE，避免先 findById 再 update 的 N+1
        userDao.updateRole(id, role);
    }

    @Override
    public int count() {
        return userDao.count();
    }
}
