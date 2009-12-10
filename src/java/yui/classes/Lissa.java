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
package yui.classes;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;

/**
 *
 * @author leo
 */
public class Lissa extends YUILoader {

    public final static  String   URI_TO_MIN ="/minify?";
    public final static  String   MINIFY_BASE ="lib";



    public Lissa(String yuiVersion) {
        this(yuiVersion,  "");
    }

    public Lissa(String yuiVersion, String cacheKey) {
        this(yuiVersion,  cacheKey, null);
    }

    public Lissa(String yuiVersion, String cacheKey, Map modules) {
        this(yuiVersion, cacheKey, modules, false);
    }

    protected String minifyBasePath=null;

    public Lissa(String yuiVersion,String cacheKey, Map modules, boolean noYUI) {
        super(yuiVersion,cacheKey,modules,noYUI);
        this.base = "/includes/js/yui/lib/"+yuiVersion+"/build/";
      this.minifyBasePath = URI_TO_MIN+"b="+MINIFY_BASE+"&f=";

    }

    protected String buildComboUrl(Map dependencyData, String type ){
        String resource="";


            if(dependencyData.size() >0 ){
                String comboUrl=this.minifyBasePath;
                logger.info("comboUrl  "+comboUrl);
                Map depData =(Map) dependencyData.get(type);

                Iterator it = depData.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String key = (String) pairs.getKey();
                    Object value = pairs.getValue();
                    comboUrl+=key+",";
                    logger.info("comboUrl Loop"+comboUrl);
                }
                logger.info("comboUrl Before Trim"+comboUrl);
                comboUrl = comboUrl.substring(0, comboUrl.lastIndexOf(","));
                logger.info("comboUrl After Trim"+comboUrl);

                resource="";


                if(type.equalsIgnoreCase("css")){
                    resource="<link rel=\"stylesheet\" type=\"text/css\" href=\""+ comboUrl +" \" />";
                }else if(type.equalsIgnoreCase("js")){
                    resource="<script type=\"text/javascript\" src=\""+ comboUrl +"\"></script> ";
                }
            }

        return resource;

    }

    @Override
    public String script() {
       Map s = this.script_data();
        logger.info("script Map"+s);
        logger.info("----------------------");
        return this.buildComboUrl(s, "js");
    }

    @Override
    public String css() {
        Map c= this.css_data();

        logger.info("css Map"+c);
        logger.info("----------------------");
        return this.buildComboUrl(c, "css");
    }

    @Override
    public String tags() {
        String jsNode = script();
        String cssNode = css();
        logger.info("tags jsNode"+jsNode);
        logger.info("----------------------");
        logger.info("tags cssNode"+cssNode);
        return jsNode+""+cssNode;
    }

}
