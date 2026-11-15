<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="header.jspf" />

<div style="max-width:1200px;margin:20px auto;padding:0 20px;">
    <h2 style="color:#2c3e50;margin-top:10px;">📖 攻略管理
        <a href="${ctx}/admin" style="float:right;color:#3498db;text-decoration:none;font-size:14px;">← 返回控制台</a>
    </h2>

    <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:6px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.1);">
        <thead style="background:#34495e;color:#fff;">
            <tr>
                <th style="padding:10px;width:60px;">ID</th>
                <th style="padding:10px;">标题</th>
                <th style="padding:10px;width:110px;">游戏</th>
                <th style="padding:10px;width:100px;">作者</th>
                <th style="padding:10px;width:70px;">浏览</th>
                <th style="padding:10px;width:80px;">删除</th>
                <th style="padding:10px;width:80px;">审核</th>
                <th style="padding:10px;width:150px;">发布时间</th>
                <th style="padding:10px;width:200px;">操作</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${strategies}" var="s">
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:10px;text-align:center;">${s.id}</td>
                    <td style="padding:10px;">
                        <a href="${ctx}/strategy/detail?id=${s.id}" style="color:#2c3e50;text-decoration:none;"><c:out value="${s.title}" /></a>
                    </td>
                    <td style="padding:10px;text-align:center;"><c:out value="${s.gameName}" /></td>
                    <td style="padding:10px;text-align:center;color:#3498db;"><c:out value="${s.authorName}" /></td>
                    <td style="padding:10px;text-align:center;color:#e67e22;">${s.viewCount}</td>
                    <td style="padding:10px;text-align:center;">
                        <c:if test="${s.status == 1}"><span style="color:#27ae60;">正常</span></c:if>
                        <c:if test="${s.status == 0}"><span style="color:#95a5a6;">已删</span></c:if>
                    </td>
                    <td style="padding:10px;text-align:center;">
                        <c:if test="${s.auditStatus == 0}"><span style="color:#f1c40f;">待审核</span></c:if>
                        <c:if test="${s.auditStatus == 1}"><span style="color:#27ae60;">已通过</span></c:if>
                        <c:if test="${s.auditStatus == 2}"><span style="color:#e74c3c;">已拒绝</span></c:if>
                    </td>
                    <td style="padding:10px;text-align:center;color:#95a5a6;font-size:13px;">
                        <fmt:formatDate value="${s.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </td>
                    <td style="padding:10px;text-align:center;">
                        <c:if test="${s.status == 1 and s.auditStatus != 1}">
                            <form method="post" action="${ctx}/admin/strategy/audit" style="display:inline;margin-right:5px;">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${s.id}" />
                                <input type="hidden" name="action" value="approve" />
                                <button type="submit" style="border:none;background:none;color:#27ae60;cursor:pointer;font-size:13px;text-decoration:none;">通过</button>
                            </form>
                        </c:if>
                        <c:if test="${s.status == 1 and s.auditStatus != 2}">
                            <form method="post" action="${ctx}/admin/strategy/audit" style="display:inline;margin-right:5px;"
                                  onsubmit="return confirm('确定拒绝《<c:out value="${s.title}" />》?')">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${s.id}" />
                                <input type="hidden" name="action" value="reject" />
                                <button type="submit" style="border:none;background:none;color:#e74c3c;cursor:pointer;font-size:13px;text-decoration:none;">拒绝</button>
                            </form>
                        </c:if>
                        <c:if test="${s.status == 1}">
                            <form method="post" action="${ctx}/admin/strategy/delete" style="display:inline;"
                                  onsubmit="return confirm('确定删除《<c:out value="${s.title}" />》?')">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${s.id}" />
                                <button type="submit" style="border:none;background:none;color:#95a5a6;cursor:pointer;font-size:13px;text-decoration:none;">删除</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty strategies}">
                <tr><td colspan="9" style="padding:30px;text-align:center;color:#95a5a6;">暂无攻略数据</td></tr>
            </c:if>
        </tbody>
    </table>
</div>
</body>
</html>
