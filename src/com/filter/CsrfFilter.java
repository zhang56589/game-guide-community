package com.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * CSRF Token 校验过滤器
 * 1) 登录后首次访问生成 token 存入 session
 * 2) POST 请求必须带 _csrf 字段（form hidden 或 query param）
 * 3) 白名单：登录/注册 接口免校验（因为 session 还没 token）
 */
public class CsrfFilter implements Filter {

    public static final String TOKEN_KEY = "_csrf";

    /** 免校验白名单（路径；jsessionid= 已在 L64 统一 strip） */
    private static final Set<String> WHITELIST = new HashSet<>(Arrays.asList(
            "/dologin",
            "/doregister"
    ));

    @Override public void init(FilterConfig filterConfig) throws ServletException {}
    @Override public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // PERF-5 修复：GET/HEAD/OPTIONS/TRACE 不做 token 校验，也不创建无谓 session
        String method = req.getMethod();
        if ("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)
                || "TRACE".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // POST/PUT/DELETE：先确保 session 存在并生成 token
        HttpSession session = req.getSession(true);
        if (session.getAttribute(TOKEN_KEY) == null) {
            session.setAttribute(TOKEN_KEY, UUID.randomUUID().toString());
        }

        // 2) 白名单跳过
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
        // 去掉 ";jsessionid=..."
        int semi = path.indexOf(';');
        if (semi > 0) path = path.substring(0, semi);
        if (WHITELIST.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 4) 取请求中的 token（form hidden input 或 query string）
        String reqToken = req.getParameter(TOKEN_KEY);
        String sessionToken = (String) session.getAttribute(TOKEN_KEY);

        if (reqToken == null || !reqToken.equals(sessionToken)) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "CSRF Token 校验失败，请返回上一页刷新后重试。");
            return;
        }

        chain.doFilter(request, response);
    }
}
