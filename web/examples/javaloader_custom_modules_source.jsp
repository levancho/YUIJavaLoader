<%-- 
    Document   : index
    Created on : Nov 21, 2009, 6:14:51 PM
    Author     : leo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="yui.classes.*" %>
<jsp:include page="inc/config.jsp" />
    <%
//Create a custom module metadata set
    java.util.Map customModules = new java.util.LinkedHashMap();
    java.util.Map _sub = new java.util.LinkedHashMap();

    _sub.put("name", "JSONModule");
    _sub.put("type", "js");
    _sub.put("fullpath", "http://www.json.org/json2.js");
    customModules.put("JSONModule", _sub);

    _sub = new java.util.LinkedHashMap();
    _sub.put("name", "customJS");
    _sub.put("type", "js");
    _sub.put("fullpath", "./assets/custom/data.js");
        java.util.List req = new java.util.ArrayList();
        req.add("JSONModule");
     _sub.put("requires",req);
     customModules.put("customJS", _sub);

     _sub = new java.util.LinkedHashMap();
    _sub.put("name", "customCSS");
    _sub.put("type", "css");
    _sub.put("fullpath", "./assets/custom/custom.css");
     customModules.put("customCSS", _sub);


//Get a new YAHOO_util_Loader instance which includes just our custom metadata (No YUI metadata)
//Note: rand is used here to help cache bust the example
YUI_util_Loader  loader = new YUI_util_Loader("2.8.0", "my_custom_config_2", customModules, true);

loader.load("JSONModule", "customJS", "customCSS");
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>YUI Java Loader Utility Example: Adding Custom (Non-YUI) Content with YUI Java Loader</title>
	<%=loader.css()%>
</head>
<body>
    <a href="http://github.com/levancho/YUIJavaLoader/blob/master/web/examples/javaloader_custom_modules_source.jsp"   target="_blank" > Source</a>
    <h1>YUI Java Loader Utility Example: Adding Custom (Non-YUI) Content with YUI Java Loader</h1>

    <p><a href="http://developer.yahoo.com/yui/Javaloader/">The YUI Java Loader Utility</a> is designed, of course, to
    help you put YUI components on the page.  While the YUI Java Loader is great at loading YUI resources it is important
    to point out that it can also be a great resource for loading custom non-YUI JavaScript and CSS resources on the page
    as well.  These can be mixed in with YUI dependencies and/or be all custom modules.</p>

    <p>This example shows you how to create a set of custom (non-YUI) modules and load them via YUI Java Loader.</p>

    <p>For this example we will load a remote JavaScript resource (i.e.) Douglas Crockford's JSON utility from
    <a href="http://json.org/">JSON.org</a>, some local JSON data, and a custom CSS module via the
    <a href="http://developer.yahoo.com/yui/Javaloader/">YUI Java Loader Utility</a>.  When the document is loaded will
    process the JSON data with the JSON utility, create additional unordered list items with that data, and apply a CSS class
    to the last item which will use custom styles defined in our custom CSS module.  The source for this example, including
    the server-side Java code, can be seen <a href="Javaloader-custom-modules.Java">here</a>.</p>

    <ul id="sample-list">
        <li class="first">This list starts with one static list item</li>
    </ul>

    <%=loader.script()%>
    <script type="text/javascript">
        window.onload = function() {
            var i,
                tmpLi,
                sampleList = document.getElementById("sample-list"),
                JSONObject = JSON.parse(JSONString),
                itemCount  = JSONObject.length;

            //Look over the JSON data and output a new li for each record
            for(i = 0; i < itemCount; i++) {
                tmpLi = document.createElement("li");

                //We'll highlight the last item red using a class from our custom css module
                if (i === itemCount - 1) {
                    tmpLi.className = 'last';
                }

                tmpLi.innerHTML = JSONObject[i].itemText;
                sampleList.appendChild(tmpLi);
            }
        };
    </script>
</body>
</html>
