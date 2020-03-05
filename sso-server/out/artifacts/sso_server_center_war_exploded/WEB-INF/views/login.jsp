<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Sso-Server：登录</title>
    <link href="${pageContext.request.contextPath}/asserts/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/asserts/css/signin.css" rel="stylesheet">
</head>

<body class="text-center">
<form class="form-signin" action="${pageContext.request.contextPath}/login">
    <%--代表我从哪里来--%>
    <input type="hidden" name="redirectUrl" value="${redirectUrl}">
    <img class="mb-4" src="${pageContext.request.contextPath}/asserts/img/logo.jpg" alt="" width="72" height="72">
    <h3 class="h3 mb-3 font-weight-normal">统一登录中心</h3>
    <input type="text" class="form-control" placeholder="Username" name="username" required="" autofocus="">
    <input type="password" class="form-control" placeholder="Password" name="password" required="">

    <div class="checkbox mb-3">
        <label><input type="checkbox" value="remember-me"> 记住我 </label>
    </div>
    <button class="btn btn-lg btn-primary btn-block" type="submit">登录</button>
    <p class="mt-5 mb-3 text-muted">艾编程教育</p>
</form>

</body>

</html>