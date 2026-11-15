<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ include file="header.jspf" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div style="max-width:1100px;margin:20px auto;padding:0 20px;">
    <c:if test="${not empty error}">
        <div style="background:#fdecea;border:1px solid #f5b7b1;color:#c0392b;padding:10px;border-radius:4px;margin-bottom:15px;">
            ⚠ <c:out value="${error}" />
        </div>
    </c:if>

    <%-- 分类导航 --%>
    <div style="background:#f8f9fa;padding:15px 20px;border-radius:6px;margin-bottom:20px;">
        <strong style="margin-right:12px;">分类：</strong>
        <a href="${ctx}/" style="margin-right:10px;text-decoration:none;">全部</a>
        <c:forEach items="${categories}" var="cat">
            <a href="${ctx}/search?category=${cat.id}" style="margin-right:10px;text-decoration:none;"><c:out value="${cat.name}" /></a>
        </c:forEach>
    </div>

    <%-- 结果标题 --%>
    <c:if test="${not empty keyword}">
        <h3>搜索 " <c:out value="${keyword}" /> " 的结果</h3>
    </c:if>
    <c:if test="${not empty category}">
        <h3>分类：<c:out value="${category.name}" /> 下的攻略</h3>
    </c:if>

    <%-- 攻略列表 --%>
    <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:6px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.1);">
        <thead style="background:#34495e;color:#fff;">
            <tr>
                <th style="padding:10px 12px;text-align:left;">标题</th>
                <th style="padding:10px 12px;text-align:center;width:120px;">游戏</th>
                <th style="padding:10px 12px;text-align:center;width:100px;">分类</th>
                <th style="padding:10px 12px;text-align:center;width:100px;">作者</th>
                <th style="padding:10px 12px;text-align:center;width:90px;">浏览</th>
                <th style="padding:10px 12px;text-align:center;width:140px;">发布时间</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty searchList}">
                    <c:set var="list" value="${searchList}" />
                </c:when>
                <c:when test="${not empty categoryList}">
                    <c:set var="list" value="${categoryList}" />
                </c:when>
                <c:otherwise>
                    <c:set var="list" value="${strategyList}" />
                </c:otherwise>
            </c:choose>
            <c:forEach items="${list}" var="s">
                <tr style="border-bottom:1px solid #eee;" onmouseover="this.style.background='#f1f3f5'" onmouseout="this.style.background=''">
                    <td style="padding:12px;">
                        <a href="${ctx}/strategy/detail?id=${s.id}" style="color:#2c3e50;text-decoration:none;font-weight:500;">
                            📖 <c:out value="${s.title}" />
                        </a>
                    </td>
                    <td style="padding:12px;text-align:center;"><c:out value="${s.gameName}" /></td>
                    <td style="padding:12px;text-align:center;color:#7f8c8d;"><c:out value="${s.categoryName}" /></td>
                    <td style="padding:12px;text-align:center;color:#3498db;"><c:out value="${s.authorName}" /></td>
                    <td style="padding:12px;text-align:center;color:#e67e22;">${s.viewCount}</td>
                    <td style="padding:12px;text-align:center;color:#95a5a6;font-size:13px;">
                        <fmt:formatDate value="${s.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty list}">
                <tr><td colspan="6" style="padding:30px;text-align:center;color:#95a5a6;">暂无攻略</td></tr>
            </c:if>
        </tbody>
    </table>
</div>
</body>
</html>
