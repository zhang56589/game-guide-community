<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<jsp:include page="header.jspf" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div style="max-width:800px;margin:20px auto;padding:0 20px;">
    <c:if test="${not empty strategy}">
        <div style="background:#fff;padding:30px;border-radius:8px;box-shadow:0 1px 6px rgba(0,0,0,.1);">

            <h2 style="margin:0 0 10px;color:#2c3e50;">${strategy.title}</h2>
            <div style="color:#7f8c8d;margin-bottom:20px;font-size:14px;">
                🎮 ${strategy.gameName} |
                ${strategy.categoryName} |
                <span style="color:#3498db;">@${strategy.authorName}</span> |
                👁 ${strategy.viewCount} 浏览 |
                <fmt:formatDate value="${strategy.createTime}" pattern="yyyy-MM-dd HH:mm"/>
            </div>

            <c:if test="${not empty strategy.coverImage}">
                <img src="${strategy.coverImage}" style="max-width:100%;border-radius:4px;margin-bottom:15px;" />
            </c:if>

            <hr style="border:none;border-top:1px solid #ecf0f1;margin:20px 0;" />

            <div style="line-height:1.8;font-size:15px;color:#34495e;white-space:pre-wrap;word-wrap:break-word;">
                ${strategy.content}
            </div>

            <hr style="border:none;border-top:1px solid #ecf0f1;margin:20px 0;" />

            <div style="text-align:right;">
                <a href="${ctx}/" style="background:#3498db;color:#fff;padding:8px 20px;border-radius:4px;text-decoration:none;">返回首页</a>
            </div>
        </div>
    </c:if>
</div>
</body>
</html>
