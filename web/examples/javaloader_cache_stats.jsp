<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<jsp:include page="inc/config.jsp" />
<%! YUILoaderCacheStatistics stats = new YUILoaderCacheStatistics();%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title>YUI Java Loader Cache Statistics</title>

</head>

<body class="yui-skin-sam">
    <%=stats.getStats()%>
</body>
</html>
