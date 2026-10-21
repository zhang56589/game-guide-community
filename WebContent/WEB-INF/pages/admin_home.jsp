<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="header.jspf" />

<div style="max-width:800px;margin:30px auto;padding:0 20px;">
    <h2 style="color:#2c3e50;margin-top:10px;">⚙ 管理后台 · 控制台</h2>

    <div style="display:flex;gap:20px;margin-bottom:30px;">
        <div style="flex:1;background:#3498db;color:#fff;padding:25px;border-radius:6px;text-align:center;">
            <div style="font-size:14px;opacity:.85;">总用户数</div>
            <div style="font-size:40px;font-weight:bold;margin-top:10px;">${userCount}</div>
        </div>
        <div style="flex:1;background:#e67e22;color:#fff;padding:25px;border-radius:6px;text-align:center;">
            <div style="font-size:14px;opacity:.85;">攻略总数</div>
            <div style="font-size:40px;font-weight:bold;margin-top:10px;">${strategyCount}</div>
        </div>
    </div>

    <div style="background:#fff;padding:25px;border-radius:6px;box-shadow:0 1px 4px rgba(0,0,0,.1);">
        <h3 style="margin-top:0;color:#2c3e50;">功能导航</h3>
        <ul style="line-height:2;color:#34495e;">
            <li><a href="${ctx}/admin/user" style="color:#3498db;">👥 用户管理</a> — 查看、禁用、启用、提升管理员</li>
            <li><a href="${ctx}/admin/strategy" style="color:#e67e22;">📖 攻略管理</a> — 查看、删除所有攻略</li>
            <li><a href="${ctx}/" style="color:#27ae60;">🏠 返回前台首页</a></li>
        </ul>
    </div>
</div>
</body>
</html>
