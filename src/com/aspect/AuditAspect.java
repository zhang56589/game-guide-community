package com.aspect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bean.AuditLog;
import com.bean.User;
import com.dao.AuditLogDao;

/**
 * 审计切面 —— 纯POJO，无注解，全部由 XML <aop:config> 配置
 * 拦截 com.service.impl 下所有 public 方法
 * 记录：操作人、目标方法、入参、耗时、是否成功
 */
public class AuditAspect {

    private AuditLogDao auditLogDao;

    /** XML 注入 */
    public void setAuditLogDao(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    /**
     * 环绕通知：拦截 Service 层所有方法
     * XML 中通过 <aop:around method="around" /> 绑定
     */
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        AuditLog log = new AuditLog();

        // 1. 取当前操作人（从 request session）
        fillOperator(log);

        // 2. 方法信息
        String className  = pjp.getTarget().getClass().getName();
        String methodName = pjp.getSignature().getName();
        log.setMethod(className + "." + methodName);
        log.setParams(buildParams(pjp.getArgs()));

        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            success = false;
            errorMsg = t.toString();
            // 截断防止过长
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            throw t; // 继续抛出，不吞异常
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.setDurationMs((int) cost);
            log.setSuccess(success ? 1 : 0);
            log.setErrorMsg(errorMsg);

            // 异步不重要，直接写；即使日志写入失败也不影响主业务
            try {
                auditLogDao.insert(log);
            } catch (Exception ignored) {
                // 审计失败不影响业务，静默忽略
            }
        }
        return result;
    }

    /** 从 RequestContextHolder 取操作人 */
    private void fillOperator(AuditLog log) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest request = attrs.getRequest();
            HttpSession session = request.getSession(false);
            if (session == null) return;
            User u = (User) session.getAttribute("loginUser");
            if (u != null) {
                log.setOperator(u.getUsername());
                log.setNickname(u.getNickname());
            }
        } catch (Exception ignored) {
            // 非 HTTP 线程场景（如定时任务），操作人留空
        }
    }

    /** 入参转字符串（截断保护） */
    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" | ");
            sb.append(safeToString(args[i]));
        }
        String s = sb.toString();
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }

    /** 安全 toString，防止对象没有合理 toString 或序列化异常 */
    private String safeToString(Object o) {
        if (o == null) return "null";
        try {
            // Date 格式友好化
            if (o instanceof Date) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) o);
            }
            String s = o.toString();
            return s.length() > 500 ? s.substring(0, 500) : s;
        } catch (Exception e) {
            return "[" + o.getClass().getSimpleName() + " toString error]";
        }
    }
}
