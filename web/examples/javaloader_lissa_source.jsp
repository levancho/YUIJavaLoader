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

            CustomModule c = new CustomModule("customJS","js");
            c.setFullpath("includes/js/example.js");
            c.addRequires("event","dom","json");
            customModules.put(c.getName(), c);

            c = new CustomModule("sampleData","js");
            c.setFullpath("includes/js/sample_data.js");
            c.addRequires("customJS");
            customModules.put(c.getName(), c);

            c = new CustomModule("customCSS","css");
            c.setFullpath("includes/css/example.css");
            customModules.put(c.getName(), c);


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
         <a href="http://github.com/levancho/YUIJavaLoader/blob/master/web/examples/javaloader_lissa_source.jsp"   target="_blank" > Source</a>
        <h1>Lissa</h1>
        <h2>YUI Java Loader + Minify: Keep the combo!</h2>
        <h4 style="color:red">NOTE: Implementation of minify Servlet is not done yet, so urls will not work. this demo just shows functionality of Lissa</h4>

        <p>The YUI Java Loader Utility is designed, of course, to
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

        <p>The combo urls built by Lissa for this page:</p>
        <form><div style="font-weight: bold" >CSS:</div>
            <textarea class="ta" rows="4" cols="100"><%=css%></textarea>
            <div style="font-weight: bold" >JS:</div>
            <textarea  class="ta" rows="4" cols="100"><%=js%></textarea>
        </form>

        <br>
            <p><em>NOTES:</em></p>

            <ul>
                <li>This example is very similar to one  with the YUI Java Loader.  The major diference is the use of Lissa instead of using the
                    YUILoader class directly.  Doing so allows us to create combo urls which mix YUI resources with local ones.</li>
                <li>Lissa class is Part of YUILoader project, so difference is just instantiating Lissa instead of YUILoader</li>
                <li>Enjoy!</li>
            </ul>

            <%=js%>
    </body>
</html>

