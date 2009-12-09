/*
 *  Copyright (c) 2009, Amostudio,inc
 *  All rights reserved.
 *  Code licensed under the BSD License:
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *    * Neither the name of the Amostudio,inc  nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY Amostudio,inc ''AS IS'' AND ANY
 *   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL Amostudio,inc  BE LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package yui.classes.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.AResourceGroup;

import yui.classes.utils.HTTPUtils;

/**
 *
 * @author leo
 */
public class MinifyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MinifyServlet.class);

    private String defaultResourceRoot;

    private boolean compressionEnabled = true;
    private boolean debugEnabled = false;
    private String comboFileDelimeter=",;";

    private long lastRefresh = 0;

    private Map requestParameters = new HashMap();
    
    public static boolean forceLoad = false;

    /**
     * params f=,b=,g=,debug=1(works if debugEnabled is set to true in servlet config.)
     * Initializes parameters and starts a task which periodically checks the jiveHome directory for
     * updated resources
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        // Basing standarts off a minify project
        //@http://code.google.com/p/minify/source/browse/tags/release_2.1.3/min/README.txt

        // TODO once working make this  more elastic/configurable  .
        //sevletConfigParameters.put("f", new ArrayList<String>());
        //sevletConfigParameters.put("b", "js");

        String resourceRootPath = config.getInitParameter("resourceRootPath");
        lastRefresh = System.currentTimeMillis();
        defaultResourceRoot = resourceRootPath != null ? resourceRootPath : "/";

        debugEnabled= config.getInitParameter("debugEnabled") != null
                ? Boolean.valueOf(config.getInitParameter("debugEnabled")) : debugEnabled;

        comboFileDelimeter =config.getInitParameter("comboFileDelimeter") != null
                ?config.getInitParameter("comboFileDelimeter"):comboFileDelimeter ;

        Enumeration<?> e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String paramValue = config.getInitParameter(paramName);
            // predefined groups might go here
        }
        
    }

    /**
     * Not implemented, returns @{link HttpServletResponse#SC_METHOD_NOT_ALLOWED}
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Post is not Allowed");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        if (!HTTPUtils.isGoodRequest(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
            return;
        }

        

        InputStream in = null;
        ServletOutputStream out = null;
        boolean compress = !compressionEnabled ? false : HTTPUtils.isCompressionPossible(request, response);

        parseParams(request);

    }



    private boolean parseParams (HttpServletRequest req){

        if(req.getParameter("f")==null){
            // too harsh?
            //throw new RuntimeException("Requred Parameter 'f' is missing");
            return false;
        }

        String files[] = req.getParameter("f").split("["+comboFileDelimeter+"]");
        if(files==null || !(files.length>0)){
            return false;
        }
//        requestParameters.put("f", new AResourceGroup(req.getQueryString(),
//                                        Arrays.asList(files)));
        requestParameters.put("f", new AResourceGroup(req.getParameter("f"),comboFileDelimeter));

        return true;
    }
}
