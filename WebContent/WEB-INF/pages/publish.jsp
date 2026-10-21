<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="header.jspf" />

<div style="max-width:700px;margin:30px auto;background:#fff;padding:30px;border-radius:8px;box-shadow:0 2px 10px rgba(0,0,0,.1);">
    <h2 style="margin-top:0;color:#2c3e50;">✍ 发布攻略</h2>

    <c:if test="${not empty error}">
        <div style="background:#fdecea;border:1px solid #f5b7b1;color:#c0392b;padding:10px;border-radius:4px;margin-bottom:15px;font-size:13px;">
            ⚠ ${error}
        </div>
    </c:if>

    <form action="${ctx}/strategy/dopublish" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
        <div style="margin-bottom:12px;">
            <label style="display:block;margin-bottom:5px;color:#34495e;font-weight:bold;">标题 *</label>
            <input type="text" name="title" value="${strategy.title}" required maxlength="100"
                   style="width:100%;padding:9px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>

        <div style="display:flex;gap:15px;margin-bottom:12px;">
            <div style="flex:1;">
                <label style="display:block;margin-bottom:5px;color:#34495e;font-weight:bold;">游戏名称 *</label>
                <input type="text" name="gameName" value="${strategy.gameName}" required
                       style="width:100%;padding:9px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
            </div>
            <div style="flex:1;">
                <label style="display:block;margin-bottom:5px;color:#34495e;font-weight:bold;">分类</label>
                <select name="categoryId"
                        style="width:100%;padding:9px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;">
                    <option value="">请选择...</option>
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}" ${strategy.categoryId==cat.id?'selected':''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div style="margin-bottom:12px;">
            <label style="display:block;margin-bottom:5px;color:#34495e;font-weight:bold;">封面图URL (可选)</label>
            <input type="text" name="coverImage" value="${strategy.coverImage}" placeholder="https://..."
                   style="width:100%;padding:9px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>

        <div style="margin-bottom:20px;">
            <label style="display:block;margin-bottom:5px;color:#34495e;font-weight:bold;">攻略内容 *</label>
            <textarea name="content" rows="12" required
                      style="width:100%;padding:10px;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;resize:vertical;font-family:inherit;">${strategy.content}</textarea>
            <small style="color:#95a5a6;">支持换行，使用空行分隔段落</small>
        </div>

        <button type="submit"
                style="padding:10px 30px;background:#27ae60;color:#fff;border:none;border-radius:4px;font-size:15px;cursor:pointer;">
            发布
        </button>
        <a href="${ctx}/my" style="margin-left:10px;padding:10px 20px;color:#7f8c8d;text-decoration:none;border:1px solid #ddd;border-radius:4px;">
            取消
        </a>
    </form>
</div>
</body>
</html>
