<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<jsp:include page="inc/config.jsp" />
    <%
      YUILoader  loader= new YUILoader("2.8.0");
    //Specify YUI components to load
loader.allowRollups= true;
loader.filter = YUILoader.YUI_RAW;
    loader.load("yahoo", "dom","calendar", "event", "tabview", "grids", "fonts", "reset","logger");
    //Output the tags (this call would most likely be placed in the document head)
 %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<html>
<head>
	<title>YUI Java  Loader Utility Advanced Example: Loading YUI Calendar with the YUI Java Loader Utility</title>


         <%=loader.css()%>
</head>

<body class="yui-skin-sam">
    <div style="margin:20px;padding:20px; background-color:#eeeeee">
 source outlook:
    <pre>
       YUILoader  loader= new YUILoader("2.8.0");
    //Specify YUI components to load
    loader.allowRollups= true;
    loader.filter = YUILoader.YUI_RAW;
    loader.load("yahoo", "dom","calendar", "event", "tabview", "grids", "fonts", "reset","logger");
    //Output the tags (this call would most likely be placed in the document head)

    </pre>
     insde of the head we insert css  scriplet  :
    <pre>loader.css()</pre>

       bottom of the page we insert javascript  scriplet  :
    <pre>loader.script()</pre>
    
    </div>

<h1>YUI Java Loader Utility Advanced Example: Loading the YUI Calendar Control with the YUI Java Loader Utility</h1>

<p>In this example, we bring a YUI component onto the page using the YUI Java Loader Utility This example implements YUI Java Loader via a <code>YUILoader</code> instance.
<div id="calendar_container"></div>

  <%=loader.script()%>
<script type="text/javascript">
    YAHOO.util.Event.onAvailable("calendar_container", function() {
		var myCal = new YAHOO.widget.Calendar("mycal_id", "calendar_container");
		myCal.render();
	})
</script>
</body>
</html>
