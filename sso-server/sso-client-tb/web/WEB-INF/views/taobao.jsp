
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>淘宝首页</title>
</head>
<body>

<h1>淘宝首页</h1>
<hr>
<p>
    当前登录的用户  <span> admin </span>
    <%--本质调用了服务器端的注销操作！--%>
    <a href="${servetLogouUrl}">注销</a>
</p>

</body>
</html>
