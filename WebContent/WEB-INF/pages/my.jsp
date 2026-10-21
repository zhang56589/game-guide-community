<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="header.jspf" />

<div style="max-width:1000px;margin:20px auto;padding:0 20px;">
    <h2 style="color:#2c3e50;margin-top:10px;">📒 我的攻略
        <a href="${ctx}/strategy/publish" style="float:right;padding:8px 18px;background:#27ae60;color:#fff;text-decoration:none;border-radius:4px;font-size:14px;">
            + 发布新攻略
        </a>
    </h2>

    <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:6px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.1);">
        <thead style="background:#34495e;color:#fff;">
            <tr>
                <th style="padding:10px;text-align:left;">标题</th>
                <th style="padding:10px;text-align:center;width:110px;">游戏</th>
                <th style="padding:10px;text-align:center;width:80px;">状态</th>
                <th style="padding:10px;text-align:center;width:70px;">浏览</th>
                <th style="padding:10px;text-align:center;width:140px;">发布时间</th>
                <th style="padding:10px;text-align:center;width:120px;">操作</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${myList}" var="s">
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:12px;">
                        <a href="${ctx}/strategy/detail?id=${s.id}" style="color:#2c3e50;text-decoration:none;">📖 ${s.title}</a>
                    </td>
                    <td style="padding:12px;text-align:center;">${s.gameName}</td>
                    <td style="padding:12px;text-align:center;">
                        <c:if test="${s.status == 1}"><span style="color:#27ae60;">正常</span></c:if>
                        <c:if test="${s.status == 0}"><span style="color:#e74c3c;">已删除</span></c:if>
                    </td>
                    <td style="padding:12px;text-align:center;color:#e67e22;">${s.viewCount}</td>
                    <td style="padding:12px;text-align:center;color:#95a5a6;font-size:13px;">
                        <fmt:formatDate value="${s.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </td>
                    <td style="padding:12px;text-align:center;">
                        <form method="post" action="${ctx}/my/delete" style="display:inline;"
                              onsubmit="return confirm('确定删除该攻略?')">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                            <input type="hidden" name="id" value="${s.id}" />
                            <button type="submit" style="border:none;background:none;color:#e74c3c;cursor:pointer;font-size:13px;text-decoration:none;">删除</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty myList}">
                <tr><td colspan="6" style="padding:30px;text-align:center;color:#95a5a6;">你还没有发布任何攻略，快去发布一篇吧~</td></tr>
            </c:if>
        </tbody>
    </table>
</div>
</body>
</html>
