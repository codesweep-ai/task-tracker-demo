<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Sign in &middot; Example.com</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css"/>
  <style>
    .login { max-width: 420px; }
  </style>
</head>
<body>
  <div class="masthead">
    <div class="brand">Example<span class="tld">.com</span></div>
    <div class="tagline">Project Tracker &mdash; Internal Tool</div>
  </div>

  <div class="content login">
    <h1>Sign in</h1>
    <c:if test="${not empty error}"><p class="error">${error}</p></c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
      <div class="formrow">
        <label>Username</label>
        <input type="text" name="username" autofocus/>
      </div>
      <div class="formrow">
        <label>Password</label>
        <input type="password" name="password"/>
      </div>
      <button type="submit">Sign in</button>
    </form>
  </div>

  <div class="footer">Example.com Project Tracker &middot; v1.0 &middot; internal use only</div>
</body>
</html>
