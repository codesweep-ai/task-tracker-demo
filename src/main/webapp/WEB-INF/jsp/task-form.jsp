<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title><c:choose><c:when test="${not empty task}">Task #${task.id}</c:when><c:otherwise>New task</c:otherwise></c:choose> &middot; Example.com</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css"/>
  <style>
    .saved { background:#e8f5e9; border:1px solid #a5d6a7; color:#2e7d32; padding:9px 11px; margin:14px 0; }
    .detail { border:1px solid #c3ccd5; background:#fafbfc; padding:16px 18px; margin:16px 0; }
    .detail .statusbadge { display:inline-block; padding:5px 14px; font-weight:bold; font-size:15px;
      letter-spacing:.5px; color:#fff; border:1px solid rgba(0,0,0,.25); }
    .status-OPEN             { background:#607d8b; }
    .status-IN_PROGRESS      { background:#1976d2; }
    .status-PENDING_APPROVAL { background:#f57c00; }
    .status-DONE             { background:#388e3c; }
    table.kv { border-collapse:collapse; margin:14px 0 4px; }
    table.kv th { text-align:left; color:#5b6770; font-weight:bold; padding:3px 16px 3px 0; vertical-align:top; }
    table.kv td { padding:3px 0; }
    .detail .transitions { margin-top:12px; }
  </style>
</head>
<body>
  <%@ include file="_header.jspf" %>

  <div class="content">
    <h1><c:choose><c:when test="${not empty task}">Task #${task.id}: ${task.title}</c:when><c:otherwise>New task</c:otherwise></c:choose></h1>
    <c:if test="${not empty error}"><p class="error">${error}</p></c:if>
    <c:if test="${param.saved == '1'}"><p class="saved">&#10003; Saved.</p></c:if>

    <%-- Focused single-task detail: current state + transitions for THIS task --%>
    <c:if test="${not empty task}">
      <div class="detail">
        <span class="statusbadge status-${task.status}">${task.status}</span>
        <table class="kv">
          <tr><th>Project</th><td>${task.projectName}</td>
              <th>Assignee</th><td>${empty task.assigneeName ? '(unassigned)' : task.assigneeName}</td></tr>
          <tr><th>Priority</th><td>${task.priority}</td>
              <th>Due</th><td>${empty task.dueDate ? '—' : task.dueDate}</td></tr>
          <tr><th>Updated</th><td colspan="3">${task.updatedAt}</td></tr>
        </table>
        <div class="transitions">
          <form method="post" action="${pageContext.request.contextPath}/tasks/action" style="display:inline;margin:0;">
            <input type="hidden" name="id" value="${task.id}"/>
            <input type="hidden" name="return" value="detail"/>
            <c:if test="${task.status == 'OPEN'}">
              <button name="action" value="start">Start</button>
            </c:if>
            <c:if test="${task.status == 'IN_PROGRESS'}">
              <button name="action" value="submit">Submit for approval</button>
            </c:if>
            <c:if test="${task.status == 'PENDING_APPROVAL'}">
              <button name="action" value="approve">Approve</button>
              <button name="action" value="reject">Reject</button>
            </c:if>
            <c:if test="${task.status == 'DONE'}">
              <button name="action" value="reopen">Reopen</button>
            </c:if>
          </form>
        </div>
      </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/tasks/form">
      <c:if test="${not empty task}"><input type="hidden" name="id" value="${task.id}"/></c:if>

      <div class="formrow">
        <label>Title *</label>
        <input type="text" name="title" value="${task.title}"/>
      </div>

      <div class="formrow">
        <label>Description</label>
        <textarea name="description" rows="3">${task.description}</textarea>
      </div>

      <div class="formrow">
        <label>Project *</label>
        <select name="projectId">
          <c:forEach var="p" items="${projects}">
            <option value="${p.id}" ${task.projectId == p.id ? 'selected' : ''}>${p.name}</option>
          </c:forEach>
        </select>
      </div>

      <div class="formrow">
        <label>Assignee</label>
        <select name="assigneeId">
          <option value="">(unassigned)</option>
          <c:forEach var="person" items="${people}">
            <option value="${person.id}" ${task.assigneeId == person.id ? 'selected' : ''}>${person.fullName}</option>
          </c:forEach>
        </select>
      </div>

      <div class="formrow">
        <label>Priority</label>
        <select name="priority">
          <c:forEach var="pr" items="LOW,MEDIUM,HIGH">
            <option value="${pr}" ${task.priority == pr ? 'selected' : ''}>${pr}</option>
          </c:forEach>
        </select>
      </div>

      <div class="formrow">
        <label>Due date</label>
        <input type="date" name="dueDate" value="${task.dueDate}"/>
      </div>

      <div class="toolbar below">
        <a href="${pageContext.request.contextPath}/tasks">Cancel</a>
        &nbsp;
        <button type="submit">Save</button>
      </div>
    </form>
  </div>

  <div class="footer">Example.com Project Tracker &middot; v1.0 &middot; internal use only</div>
</body>
</html>
