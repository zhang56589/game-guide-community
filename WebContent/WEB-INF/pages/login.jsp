<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="header.jspf" />

<div style="max-width:400px;margin:50px auto;background:#fff;padding:30px;border-radius:8px;box-shadow:0 2px 10px rgba(0,0,0,.1);">
    <h2 style="text-align:center;color:#2c3e50;margin-top:0;">🔑 用户登录</h2>

    <c:if test="${not empty error}">
        <div style="background:#fdecea;border:1px solid #f5b7b1;color:#c0392b;padding:10px;border-radius:4px;margin-bottom:15px;font-size:13px;">
            ⚠ ${error}
        </div>
    </c:if>

    <form action="${ctx}/dologin" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
        <div style="margin-bottom:15px;">
            <label style="display:block;margin-bottom:5px;color:#34495e;">账号</label>
            <input type="text" name="username" value="${param.username}" required
                   style="width:100%;padding:10px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>
        <div style="margin-bottom:20px;">
            <label style="display:block;margin-bottom:5px;color:#34495e;">密码</label>
            <input type="password" name="password" required
                   style="width:100%;padding:10px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>
        <button type="submit"
                style="width:100%;padding:10px;background:#3498db;color:#fff;border:none;border-radius:4px;font-size:16px;cursor:pointer;">
            登录
        </button>
    </form>

    <p style="text-align:center;color:#7f8c8d;margin-top:20px;font-size:13px;">
        还没账号？<a href="${ctx}/register" style="color:#3498db;text-decoration:none;">立即注册</a>
    </p>
    <p style="text-align:center;color:#bdc3c7;font-size:12px;margin-top:10px;">
        管理员: admin / 123456 &nbsp;|&nbsp; 普通用户: player1 / 123456
    </p>
</div>
</body>
</html>
