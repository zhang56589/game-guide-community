package com.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.bean.Strategy;
import com.bean.User;
import com.dao.StrategyDao;

/**
 * 全局越权访问过滤器（原生 Filter，非 Spring Bean）
 * - /my/*   未登录→302；/my/delete 校验资源归属
 * - /admin/* 必须 role==1
 * DAO 从 Spring root context 动态获取
 */
public class AuthFilter implements Filter {

    private StrategyDao strategyDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Spring root context 在 ContextLoaderListener 启动后就存在，这里一定能取到
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(
                filterConfig.getServletContext());
        if (ctx != null && ctx.containsBean("strategyDao")) {
            this.strategyDao = ctx.getBean("strategyDao", StrategyDao.class);
        }
    }

    @Override public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
        int semi = path.indexOf(';');
        if (semi > 0) path = path.substring(0, semi);

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");

        // ========== /my/* 路径 ==========
        if (path.equals("/my") || path.startsWith("/my/")) {
            if (loginUser == null) {
                resp.sendRedirect(ctx + "/login");
                return;
            }
            // BUG-2 修复：fail-closed，strategyDao 为空直接报错而非静默放行
            if (path.startsWith("/my/delete")) {
                if (strategyDao == null) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "安全过滤器初始化失败，请联系管理员");
                    return;
                }
                String idStr = req.getParameter("id");
                if (idStr != null) {
                    Integer id;
                    try {
                        id = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        // BUG-3 修复：非法 id 返回 400，不再抛 500
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数 id 格式错误");
                        return;
                    }
                    Strategy s = strategyDao.findById(id);
                    if (s == null || !s.getUserId().equals(loginUser.getId())) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "该攻略不属于您");
                        return;
                    }
                }
            }
            chain.doFilter(request, response);
            return;
        }

        // ========== /admin/* 路径 ==========
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            if (loginUser == null) { resp.sendRedirect(ctx + "/login"); return; }
            if (loginUser.getRole() == null || loginUser.getRole() != 1) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "仅管理员可访问后台");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
