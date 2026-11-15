<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="header.jspf" />

<div style="max-width:1000px;margin:20px auto;padding:0 20px;">
    <h2 style="color:#2c3e50;margin-top:10px;">👥 用户管理
        <a href="${ctx}/admin" style="float:right;color:#3498db;text-decoration:none;font-size:14px;">← 返回控制台</a>
    </h2>

    <table style="width:100%;border-collapse:collapse;background:#fff;border-radius:6px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.1);">
        <thead style="background:#34495e;color:#fff;">
            <tr>
                <th style="padding:10px;width:60px;">ID</th>
                <th style="padding:10px;">账号</th>
                <th style="padding:10px;">昵称</th>
                <th style="padding:10px;width:90px;">角色</th>
                <th style="padding:10px;width:80px;">状态</th>
                <th style="padding:10px;width:160px;">注册时间</th>
                <th style="padding:10px;width:220px;">操作</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${users}" var="u">
                <tr style="border-bottom:1px solid #eee;"
                    onmouseover="this.style.background='#f1f3f5'"
                    onmouseout="this.style.background=''">
                    <td style="padding:10px;text-align:center;">${u.id}</td>
                    <td style="padding:10px;"><c:out value="${u.username}" /></td>
                    <td style="padding:10px;"><c:out value="${u.nickname}" /></td>
                    <td style="padding:10px;text-align:center;">
                        <c:if test="${u.role == 1}"><span style="color:#f1c40f;font-weight:bold;">管理员</span></c:if>
                        <c:if test="${u.role == 0}">普通用户</c:if>
                    </td>
                    <td style="padding:10px;text-align:center;">
                        <c:if test="${u.status == 1}"><span style="color:#27ae60;">● 正常</span></c:if>
                        <c:if test="${u.status == 0}"><span style="color:#e74c3c;">● 禁用</span></c:if>
                    </td>
                    <td style="padding:10px;text-align:center;color:#95a5a6;font-size:13px;">
                        <fmt:formatDate value="${u.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </td>
                    <td style="padding:10px;text-align:center;font-size:13px;">
                        <c:if test="${u.status == 1 and u.id != loginUser.id}">
                            <form method="post" action="${ctx}/admin/user/status" style="display:inline;margin-right:8px;"
                                  onsubmit="return confirm('禁用 <c:out value="${u.username}" /> ?')">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${u.id}" />
                                <input type="hidden" name="action" value="disable" />
                                <button type="submit" style="border:none;background:none;color:#e74c3c;cursor:pointer;text-decoration:none;">禁用</button>
                            </form>
                        </c:if>
                        <c:if test="${u.status == 0}">
                            <form method="post" action="${ctx}/admin/user/status" style="display:inline;margin-right:8px;">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${u.id}" />
                                <input type="hidden" name="action" value="enable" />
                                <button type="submit" style="border:none;background:none;color:#27ae60;cursor:pointer;text-decoration:none;">启用</button>
                            </form>
                        </c:if>
                        <c:if test="${u.role == 0}">
                            <form method="post" action="${ctx}/admin/user/status" style="display:inline;margin-right:8px;">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${u.id}" />
                                <input type="hidden" name="action" value="promote" />
                                <button type="submit" style="border:none;background:none;color:#f1c40f;cursor:pointer;text-decoration:none;">升管理</button>
                            </form>
                        </c:if>
                        <c:if test="${u.role == 1 and u.id != loginUser.id}">
                            <form method="post" action="${ctx}/admin/user/status" style="display:inline;"
                                  onsubmit="return confirm('取消 <c:out value="${u.username}" /> 管理员权限?')">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
                                <input type="hidden" name="id" value="${u.id}" />
                                <input type="hidden" name="action" value="demote" />
                                <button type="submit" style="border:none;background:none;color:#95a5a6;cursor:pointer;text-decoration:none;">降管理</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
