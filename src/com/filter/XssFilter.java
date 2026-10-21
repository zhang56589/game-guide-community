package com.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * XSS 输入过滤过滤器
 * 对所有 request.getParameter / getParameterValues / getHeader 结果做安全转义
 */
public class XssFilter implements Filter {

    private static final Pattern[] DANGER_PATTERNS = new Pattern[] {
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("</?script[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*iframe[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*object[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*embed[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*link[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*meta[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*style[^>]*>.*?</\\s*style\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
    };

    @Override public void init(FilterConfig filterConfig) throws ServletException {}
    @Override public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    /** 内部包装器：覆盖所有取参数方法，统一做 escape */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        XssRequestWrapper(HttpServletRequest request) { super(request); }

        @Override public String getParameter(String name) {
            return clean(super.getParameter(name));
        }
        @Override public String[] getParameterValues(String name) {
            String[] arr = super.getParameterValues(name);
            if (arr == null) return null;
            for (int i = 0; i < arr.length; i++) arr[i] = clean(arr[i]);
            return arr;
        }
        @Override public String getHeader(String name) {
            return clean(super.getHeader(name));
        }
        @Override public String getQueryString() {
            String q = super.getQueryString();
            if (q == null) return null;
            try { q = URLDecoder.decode(q, "UTF-8"); } catch (UnsupportedEncodingException ignored) {}
            return clean(q);
        }
        @Override public String getRequestURI() {
            return clean(super.getRequestURI());
        }

        /** 核心清洗逻辑 */
        private String clean(String src) {
            if (src == null || src.isEmpty()) return src;
            String s = src;
            for (Pattern p : DANGER_PATTERNS) {
                s = p.matcher(s).replaceAll("");
            }
            // 基本 HTML 转义（< > & " '）
            return s.replace('<', '＜')
                    .replace('>', '＞')
                    .replace('"', '＂')
                    .replace('\'', '＇');
        }
    }
}
