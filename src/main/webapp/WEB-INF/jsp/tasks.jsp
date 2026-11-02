<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Tasks &middot; Example.com</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css"/>
</head>
<body>
  <%@ include file="_header.jspf" %>

  <div class="content">
    <h1>Tasks</h1>

    <div class="toolbar">
      <form method="get" action="${pageContext.request.contextPath}/tasks">
        <label for="status">Filter by status:</label>
        <select id="status" name="status" onchange="this.form.submit()">
          <option value="">(all)</option>
          <c:forEach var="s" items="OPEN,IN_PROGRESS,PENDING_APPROVAL,DONE">
            <option value="${s}" ${statusFilter == s ? 'selected' : ''}>${s}</option>
          </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label for="sort">Sort by:</label>
        <select id="sort" name="sort" onchange="this.form.submit()">
          <option value="updated"  ${sortBy == 'updated'  ? 'selected' : ''}>Recently updated</option>
          <option value="status"   ${sortBy == 'status'   ? 'selected' : ''}>Status</option>
          <option value="priority" ${sortBy == 'priority' ? 'selected' : ''}>Priority</option>
          <option value="due"      ${sortBy == 'due'      ? 'selected' : ''}>Due date</option>
          <option value="title"    ${sortBy == 'title'    ? 'selected' : ''}>Title</option>
          <option value="id"       ${sortBy == 'id'       ? 'selected' : ''}>ID</option>
        </select>
        <noscript><button type="submit">Apply</button></noscript>
      </form>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/tasks/action">
      <input type="hidden" name="action" value="bulkClose"/>
      <table class="grid">
        <tr>
          <th></th><th>ID</th><th>Title</th><th>Project</th><th>Assignee</th>
          <th>Status</th><th>Priority</th><th>Due</th><th>Updated</th><th>Transitions</th>
        </tr>
        <c:forEach var="t" items="${tasks}">
          <tr>
            <td><input type="checkbox" name="ids" value="${t.id}"/></td>
            <td>${t.id}</td>
            <td><a href="${pageContext.request.contextPath}/tasks/form?id=${t.id}">${t.title}</a></td>
            <td>${t.projectName}</td>
            <td>${t.assigneeName}</td>
            <td class="status">${t.status}</td>
            <td>${t.priority}</td>
            <td>${t.dueDate}</td>
            <td>${t.updatedAt}</td>
            <td>
              <c:if test="${t.status == 'OPEN'}">
                <button form="act-${t.id}" name="action" value="start">Start</button>
              </c:if>
              <c:if test="${t.status == 'IN_PROGRESS'}">
                <button form="act-${t.id}" name="action" value="submit">Submit for approval</button>
              </c:if>
              <c:if test="${t.status == 'PENDING_APPROVAL'}">
                <button form="act-${t.id}" name="action" value="approve">Approve</button>
                <button form="act-${t.id}" name="action" value="reject">Reject</button>
              </c:if>
              <c:if test="${t.status == 'DONE'}">
                <button form="act-${t.id}" name="action" value="reopen">Reopen</button>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </table>

      <div class="toolbar below">
        <button type="submit">Close selected (bulk)</button>
      </div>
    </form>

    <%-- Per-row forms; the buttons above point at them by id with the HTML form="" attribute. --%>
    <c:forEach var="t" items="${tasks}">
      <form id="act-${t.id}" method="post"
            action="${pageContext.request.contextPath}/tasks/action">
        <input type="hidden" name="id" value="${t.id}"/>
      </form>
    </c:forEach>
  </div>

  <div class="footer">Example.com Project Tracker &middot; v1.0 &middot; internal use only</div>
</body>
</html>
