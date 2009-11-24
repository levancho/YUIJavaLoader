<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<%

  YAHOO_util_Loader  loader = new YAHOO_util_Loader("2.7.0",pageContext);

//Turn off rollups
    loader.allowRollups = false;

    //Specify YUI components to load
    loader.load("yahoo", "dom", "event", "grids", "fonts", "reset","tabview");


      YUI_util_Loader  loader2= new YUI_util_Loader("2.8.0",pageContext);

//Turn off rollups
    loader2.allowRollups = false;

    //Specify YUI components to load
    loader2.load("yahoo", "dom", "event", "grids", "fonts", "reset","tabview");

    //Output the tags (this call would most likely be placed in the document head)

 %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <!-- Loader 1 -->
        <%=loader.tags(null,false)%>

        <!-- Loader 2 -->
         <%=loader2.tags(null,false)%>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
