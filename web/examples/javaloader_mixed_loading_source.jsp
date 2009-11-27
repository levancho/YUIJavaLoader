<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<jsp:include page="inc/config.jsp" />
   <%
      YUI_util_Loader  loader= new YUI_util_Loader("2.7.0",pageContext);
    //Specify YUI components to load

    loader.load("calendar");
    //Output the tags (this call would most likely be placed in the document head)
 %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title>YUI Java Loader Utility Advanced Example: Mixed Loading Methods</title>
	     <%=loader.css()%>
</head>

<body class="yui-skin-sam">
  <div style="margin:20px;padding:20px; background-color:#eeeeee">
 source outlook:
    <pre>
     YUI_util_Loader  loader= new YUI_util_Loader("2.7.0",pageContext);
    loader.load("calendar");

    </pre>
     insde of the head we insert css  scriplet  :
    <pre>loader.css()</pre>

       bottom of the page we excluded staticly included JS files :
    <pre>loader.setLoaded("yahoo", "dom", "event");</pre>

      and we insert javascript scriplet  :
    <pre>loader.script()</pre>
    
    </div>

    <h1>YUI Java Loader Utility Advanced Example: Mixed Loading Methods</h1>

    <p>The main difference between
    this example and the previous advanced example is that we have mixed the component loading methods.  The Calender component requires the Yahoo, Dom,
    and Event modules.  In the previous example we let Java Loader bring these into the document for us.  However, in this case we placed a static
    script include into the document that brings in these resources.  We do not wish to have Java Loader duplicate the loading of these components so
    we utilize the <em>setLoaded</em> method to notify Java Loader that we already have these components in the document and to skip loading them a second time.</p>

    <div id="calendar_container"></div>

    <script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <%
    //Output the script tags (but don't re-include yahoo-dom-event)
    loader.setLoaded("yahoo", "dom", "event");
     %>
       <%=loader.script()%>
    <script type="text/javascript">
        YAHOO.util.Event.onAvailable("calendar_container", function() {
    		var myCal = new YAHOO.widget.Calendar("mycal_id", "calendar_container");
    		myCal.render();
    	})
    </script>
</body>
</html>
