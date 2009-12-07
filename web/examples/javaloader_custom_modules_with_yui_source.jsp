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

    _sub = new java.util.LinkedHashMap();
    _sub.put("name", "customJS");
    _sub.put("type", "js");
    _sub.put("fullpath", "./assets/custom/data.js");
        java.util.List req = new java.util.ArrayList();
        req.add("event");
        req.add("dom");
        req.add("json");
     _sub.put("requires",req);
     customModules.put("customJS", _sub);

     _sub = new java.util.LinkedHashMap();
    _sub.put("name", "customCSS");
    _sub.put("type", "css");
    _sub.put("fullpath", "./assets/custom/custom.css");
     customModules.put("customCSS", _sub);


//Get a new YAHOO_util_Loader instance which includes just our custom metadata (No YUI metadata)
//Note: rand is used here to help cache bust the example
YUILoader  loader = new YUILoader("2.8.0", null, customModules);
loader.allowRollups=true;
loader.load("JSONModule", "customJS", "customCSS");
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>YUI Java Loader Utility Example: Adding Custom Modules with YUI Dependencies</title>
	<%=loader.css()%>
</head>
<body>
    <a href="http://github.com/levancho/YUIJavaLoader/blob/master/web/examples/javaloader_custom_modules_with_yui_source.jsp"   target="_blank" > Source</a>
    <h1>YUI Java Loader Utility Example: Adding Custom Modules with YUI Dependencies</h1>

    <p><a href="http://developer.yahoo.com/yui/Javaloader/">The YUI Java Loader Utility</a> is designed, of course, to
    help you put YUI components on the page.  While the YUI Java Loader is great at loading YUI resources it is important
    to point out that it can also be a great resource for loading custom non-YUI JavaScript and CSS resources on the page
    as well.  These can be mixed in with YUI dependencies and/or be all custom modules.</p>

    <p>This example shows you how to create a set of custom (non-YUI) modules that have YUI component dependencies and
    load them via YUI Java Loader.</p>

    <p>For this example we will load some local JSON data and a custom CSS module via the
    <a href="http://developer.yahoo.com/yui/Javaloader/">YUI Java Loader Utility</a>.  The custom JavaScript module, <em>customJS</em>,
    defines dependencies on the YUI DOM, Event, and JSON components so the YUI Java loader will load these for us as well. When the document
    is loaded we will process the JSON data with the JSON utility, create additional unordered list items with that data, and apply a CSS class
    to the last item which will use custom styles defined in our custom CSS module.</p>

    <p>An example without YUI dependencies can be found <a href="Javaloader-custom-modules.Java">here</a>.  To make the examples easier to follow
    the code preforms the exact same functionality.  The biggest difference between these two examples is the usage of YUI Components.  The
    source for this example, including the server-side Java code, can be seen <a href="Javaloader-custom-modules-with-yui.Java">here</a>.</p>

    <ul id="sample-list">
        <li class="first">This list starts with one static list item</li>
    </ul>

    <%=loader.script()%>
    
    <script type="text/javascript">
        YAHOO.util.Event.on(window, "load", function() {
            var i,
                tmpLi,
                sampleList = YAHOO.util.Dom.get("sample-list"),
                JSONObject = YAHOO.lang.JSON.parse(JSONString),
                itemCount  = JSONObject.length;

            //Look over the JSON data and output a new li for each record
            for(i = 0; i < itemCount; i++) {
                tmpLi = document.createElement("li");

                //We'll highlight the last item red using a class from our custom css module
                if (i === itemCount - 1) {
                    YAHOO.util.Dom.addClass(tmpLi, "last");
                }

                tmpLi.innerHTML = JSONObject[i].itemText;
                sampleList.appendChild(tmpLi);
            }
        });
    </script>
</body>
</html>
