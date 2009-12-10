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
            _sub.put("fullpath", "includes/js/example.js");
            java.util.List req = new java.util.ArrayList();
            req.add("event");
            req.add("dom");
            req.add("json");
            _sub.put("requires", req);
            customModules.put("customJS", _sub);

            _sub = new java.util.LinkedHashMap();
            _sub.put("name", "sampleData");
            _sub.put("type", "js");
            _sub.put("fullpath", "includes/js/sample_data.js");
            req = new java.util.ArrayList();
            req.add("customJS");
            _sub.put("requires", req);
            customModules.put("sampleData", _sub);

            _sub = new java.util.LinkedHashMap();
            _sub.put("name", "customCSS");
            _sub.put("type", "css");
            _sub.put("fullpath", "includes/css/example.css");
            customModules.put("customCSS", _sub);



            Lissa loader = new Lissa("2.8.0r4", "lissa", customModules);
            loader.allowRollups = true;
            loader.cacheBuster = false;
            loader.load("fonts", "sampleData", "customCSS");

            String css = loader.css();
            String js = loader.script();
            
            //Output the tags (this call would most likely be placed in the document head)
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>YUI Java Loader + Minify: Keep the combo!</title>
        <%=css%>
    </head>
    <body>
        <h1>Lissa</h1>
        <h2>YUI Java Loader + Minify: Keep the combo!</h2>

        <p><a href="http://developer.yahoo.com/yui/phploader/">The YUI Java Loader Utility</a> is designed, of course, to
            help you put YUI components on the page.  While the YUI Java Loader is great at loading YUI resources it is important
            to point out that it can also be a great resource for loading custom non-YUI JavaScript and CSS resources on the page
            as well.  These can be mixed in with YUI dependencies and/or be all custom modules.</p>

        <p>This example shows you how to create a set of custom (non-YUI) modules that have YUI component dependencies and
            load them via YUI Java Loader.</p>

        <p>For this example we will load some local JSON data and a custom CSS module via the
            <a href="http://developer.yahoo.com/yui/phploader/">YUI Java Loader Utility</a>.  The custom JavaScript module, <em>customJS</em>,
            defines dependencies on the YUI DOM, Event, and JSON components so the YUI Java loader will load these for us as well. When the document
            is loaded we will process the JSON data with the JSON utility, create additional unordered list items with that data, and apply a CSS class
            to the last item which will use custom styles defined in our custom CSS module.</p>

        <ul id="sample-list">
            <li class="first">This list starts with one static list item</li>
        </ul>

        <p>The combo urls built by Lissa for this page where:</p>
        <form>
            <textarea class="ta" rows="10" cols="100"><%=css%> \n\r <%=js%></textarea>
        </form>

        <br>
            <p><em>NOTES:</em></p>

            <ul>
                <li>This example is very similar to one shipped with the YUI Java Loader.  The major diference is the use of Lissa instead of using the
                    YAHOO_util_Loader class directly.  Doing so allows us to create combo urls which mix YUI resources with local ones.</li>
                <li>Lissa is open source.  Get the code on <a href="http://github.com/cauld/lissa">Github</a>.</li>
                <li>Enjoy!</li>
            </ul>

            <%=js%>
    </body>
</html>

