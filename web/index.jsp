<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">


<%




      YUILoader  loader2= new YUILoader("2.7.0");

//Turn off rollups
    loader2.allowRollups = true;

    //Specify YUI components to load
    loader2.load("yahoo", "dom", "event", "tabview", "grids", "fonts", "reset");

    //Output the tags (this call would most likely be placed in the document head)

 %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <!-- Loader 1 -->


        <!-- Loader 2 -->
         <%=loader2.tags(null,false)%>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
