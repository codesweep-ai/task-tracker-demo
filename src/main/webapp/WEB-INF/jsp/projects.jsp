<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Projects &middot; Example.com</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css"/>
</head>
<body>
  <%@ include file="_header.jspf" %>

  <div class="content">
    <h1>Projects</h1>

    <table class="grid">
      <tr><th>ID</th><th>Name</th><th>Description</th><th>State</th><th>Owner</th><th>Actions</th></tr>
      <c:forEach var="p" items="${projects}">
        <tr>
          <td>${p.id}</td>
          <td>${p.name}</td>
          <td>${p.description}</td>
          <td class="status">${p.state}</td>
          <td>${p.ownerName}</td>
          <td>
            <c:if test="${p.state == 'ACTIVE'}">
              <form method="post" action="${pageContext.request.contextPath}/projects" style="display:inline;margin:0;">
                <input type="hidden" name="action" value="archive"/>
                <input type="hidden" name="id" value="${p.id}"/>
                <button type="submit">Archive</button>
              </form>
            </c:if>
            <a href="${pageContext.request.contextPath}/projects/delete?id=${p.id}">Delete</a>
          </td>
        </tr>
      </c:forEach>
    </table>

    <div class="toolbar">
      <form method="post" action="${pageContext.request.contextPath}/projects">
        <div class="formrow">
          <label>New project name</label>
          <input type="text" name="name"/>
        </div>
        <div class="formrow">
          <label>Description</label>
          <input type="text" name="description"/>
        </div>
        <button type="submit">Create project</button>
      </form>
    </div>
  </div>

  <div class="footer">Example.com Project Tracker &middot; v1.0 &middot; internal use only</div>
</body>
</html>
