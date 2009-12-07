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
	<title>YUI Java Loader Utility Basic Example: Loading YUI Calendar with the YUI Java Loader Utility</title>
	
        <%
      YUILoader  loader= new YUILoader("2.7.0");
    //Specify YUI components to load

      //Turn off rollups
    loader.allowRollups = true;
    loader.load("calendar");
    //Output the tags (this call would most likely be placed in the document head)
 %>
 <%=loader.tags()%>
</head>

<body class="yui-skin-sam">
       <div style="margin:20px;padding:20px; background-color:#eeeeee">
 source outlook:
    <pre>
   YUILoader  loader= new YUILoader("2.7.0");
    //Specify YUI components to load

    loader.allowRollups = true;
    loader.load("calendar");

    </pre>

         inside scriplet we do :
    <pre> loader.tags()</pre>
       </div>


<h1>YUI Java Loader Utility Basic Example: Loading the YUI Calendar Control with the YUI Java Loader Utility</h1>


<div id="calendar_container"></div>

<script type="text/javascript">
    YAHOO.util.Event.onAvailable("calendar_container", function() {
		var myCal = new YAHOO.widget.Calendar("mycal_id", "calendar_container");
		myCal.render();
	})
</script>
</body>
</html>
