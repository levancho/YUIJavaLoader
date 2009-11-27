<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<jsp:include page="inc/config.jsp" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title>YUI PHP Loader Utility Basic Example: Loading YUI Calendar with the YUI PHP Loader Utility</title>
	
        <%
      YUI_util_Loader  loader= new YUI_util_Loader("2.7.0",pageContext);
    //Specify YUI components to load

      //Turn off rollups
    loader.allowRollups = false;
    loader.load("calendar");
    //Output the tags (this call would most likely be placed in the document head)
 %>
 <%=loader.tags()%>
</head>

<body class="yui-skin-sam">

<h1>YUI PHP Loader Utility Basic Example: Loading the YUI Calendar Control with the YUI PHP Loader Utility</h1>

<p>In this example, we bring a YUI component onto the page using the <a href="http://developer.yahoo.com/yui/phploader/">YUI PHP Loader Utility</a>. This example implements YUI PHP Loader via a <code>YAHOO_util_Loader</code> instance.  We include the YUI PHP Loader class file, then specify in configuration which component we want loaded on the page &mdash; and what we want to do once it <em>is</em> loaded.</p>

<div id="calendar_container"></div>

<script type="text/javascript">
    YAHOO.util.Event.onAvailable("calendar_container", function() {
		var myCal = new YAHOO.widget.Calendar("mycal_id", "calendar_container");
		myCal.render();
	})
</script>
</body>
</html>
