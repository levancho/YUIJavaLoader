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

import java.util.Arrays;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.utils.HTTPUtils;

/**
 *  This Class lets you combine files without relying
 *   on a remote combo-service.  The key use case here would be someone
 *   writing their own website/application in Java.
 *
 * Combo Class is called from ComboServlet and does not implement anything
 * Servlet related, this way it can easily
 * be used directly as API provider instead of just being a Servlet.
 *
 * @See yui.classes.servlets.ComboServlet
 *
 * @author leo
 */
public class Combo {

    Logger logger = LoggerFactory.getLogger(Combo.class);
    private AResourceGroup resourceGroup;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String cacheKey = "yuiconfigLFU";
    private CacheManager cacheManager;
    public String serverURI;
    private String crtResourceBase;

//    public void alphaImageLoaderPathCorrection(String... matches) {
//        // TODO
////    $matchedFile  = substr($matches[1], strrpos($matches[1], "/") + 1);
////    $newFilePath = 'AlphaImageLoader(src=\'' . $crtResourceBase . $matchedFile . '\'';
////
////    return $newFilePath;
//    }

    public Combo(HttpServletRequest _request, HttpServletResponse _response) {

        this.request = _request;
        this.response = _response;
        serverURI = HTTPUtils.getServerURL(_request);
        cacheManager = CacheManager.create();
        //resourceGroup = new AResourceGroup(request.getQueryString());
        resourceGroup = new AResourceGroup(request);
        logger.info("Calling init");
        init();
        logger.info("after init");
    }

    private String getLibPath (){
        //TODO
        return null;
    }

    //AlphaImageLoader\(src=[\'"](.*?)[\'"]
    private String alphaImageLoaderPathCorrection(Matcher matches){
        String matchOne = matches.group(1);
        String matchedFile = matchOne.substring((matchOne.lastIndexOf("/") + 1));
        String newPath = "AlphaImageLoader(src='"+ crtResourceBase +matchedFile +"'";
        return newPath;
    }

    private void init() {

        logger.info("starting init");
        if (!resourceGroup.getQueryString().equals("")) {
            if (!cacheManager.cacheExists(cacheKey)) {
                cacheManager.addCache(cacheKey);
            }

            HttpServletResponse res = (HttpServletResponse) response;

            HTTPUtils.setCacheExpireDate(res, 315360000);
            res.setHeader(HTTPUtils.Headers.CONTENT_TYPE + "", resourceGroup.getContentType());

            Cache c = cacheManager.getCache(cacheKey);
            if (c.isKeyInCache(serverURI + resourceGroup.getContentType())) {
                logger.debug("we found cache " + (serverURI + resourceGroup.getContentType()));
                // TODO when we turn this into tag we send this  puppy to client.
                logger.debug(c.get(serverURI + resourceGroup.getContentType()).toString());
            } else {
                logger.debug("we dont have cache for  " + serverURI);
                YUILoader loader = new YUILoader(resourceGroup.getVersion());
                // todo do we need this? dont think so
//                    $base   = PATH_TO_LIB . $yuiVersion . "/build/";
//                    $loader->base = $base;


                //Detect and set a filter as needed (defaults to minified version)
                if (resourceGroup.isDebug()) {
                    logger.debug("Found debug files ");
                    loader.filter = YUILoader.YUI_DEBUG;
                } else if (!resourceGroup.isDebug() && !resourceGroup.isMin()) {
                    logger.debug("assuming raw files");
                    loader.filter = YUILoader.YUI_RAW;
                }

                //Verify this version of the library exists locally
                // TODO later
                //        $localPathToBuild = "../lib/" . $yuiVersion . "/build/";
                //        if (file_exists($localPathToBuild) === false || is_readable($localPathToBuild ) === false) {
                //            die('<!-- Unable to locate the YUI build directory! -->');
                //        }
                String raw = "";
                String yuiComponent = "";
                logger.debug("Iterating through yuiFiles: " + resourceGroup.getGroup());
                for (String aResource : resourceGroup.getGroup()) {
                    String yuiFile = aResource;
                    logger.debug("for yuiFile: " + yuiFile);
                    String parts[] = yuiFile.split("/");

                    if (parts != null && parts.length >= 3) {
                        logger.debug("for yuiFile Parts : " + Arrays.toString(parts));
                        yuiComponent = parts[2];

                    } else {
                        logger.error("<!-- Unable to determine module name! -->");
                        throw new RuntimeException("<!-- Unable to determine module name! -->");
                    }


                    logger.debug("loading following Components :  " + yuiComponent);
                    loader.loadSingle(yuiComponent);
                    if (resourceGroup.getContentType().equals(HTTPUtils.CONTENT_TYPE.JAVASCRIPT + "")) {
                        raw += loader.script_raw();
                        logger.trace("fetching raw from loader :  " + raw);
                        // TODO display
                    } else {
                        Map cssResourceList = loader.css_data();

                        logger.debug("fetching css_data from loader :  " + cssResourceList);

                        Map cssResourceListCSS = (Map) cssResourceList.get("css");

                        logger.debug("cssResourceListCSS is :  " + cssResourceListCSS);
                        if (cssResourceListCSS != null) {
                            for (String key : (Set<String>) cssResourceListCSS.keySet()) {
                                // TODO finish
                                logger.debug("key  is :  " + key);
                                crtResourceBase = key.substring(0, (key.lastIndexOf("/") + 1));
                                logger.info("crtResourceBase  is :  " + crtResourceBase);

                                // TODO need test case for this regexps
                                String crtResourceContent = loader.getRemoteContent(key);

                                // just filename or subdirs/filename (e.g) url(foo.png), url(foo/foo.png)
                                // Groups:
                                // 1   2        3                    4
                                // (  ( url\( ) ( [^\.\.|^http]\S+ ) ( \) )  )
                                crtResourceContent = crtResourceContent.replaceAll("((url\\()([^\\.\\.|^http]\\S+)(\\)))",
                                        "'$2'"+crtResourceBase+"'$3'"+"'$4'");

                                // slash filename (e.g.) url(/whatever)
                                crtResourceContent = crtResourceContent.replaceAll("url\\(/","url("+crtResourceBase);

                                // relative paths (e.g.) url(../../foo.png)
                                crtResourceContent = crtResourceContent.replaceAll("(url\\()(\\.\\.\\/)+","url("+getLibPath());

                                // AlphaImageLoader relative paths (e.g.) AlphaImageLoader(src='../../foo.png')
                                Pattern alpaImagePattern = Pattern.compile("AlphaImageLoader\\(src=['\"](.*?)['\"]");
                                Matcher matcher = alpaImagePattern.matcher(crtResourceContent);
                                if(matcher.find(1)){
                                    crtResourceContent =matcher.replaceAll(alphaImageLoaderPathCorrection(matcher));
                                }
                                raw += crtResourceContent;
                            }
                        }
                        logger.trace("rawCSS before: " + raw);
                        raw = raw.replace("/build/build/", "/build/");
                        logger.trace("rawCSS after: " + raw);
                    }
                }
                logger.debug("[putting in Cache]  key " + serverURI + resourceGroup.getContentType());
                c.put(new Element(serverURI + resourceGroup.getContentType(), raw));
//                        YUIcompressorAPI api =  new YUIcompressorAPI();
//                        YUIcompressorAPI.Config  conf= api .new Config(null);

            }
        }

    }

    /**
     * Call this method to get output generated by this combo handler, based of query String if current request.
     * at thismoment this method is called from ComboServlet and directly flushed into Response.
     *
     * 
     *
     * @See yui.classes.servlets.ComboServlet
     * @return raw (js or css)  output generated off query String on current request
     */
    public String getRaw() {
        logger.info("[getRaw] checking cache");
        if (serverURI == null || resourceGroup == null || resourceGroup.getContentType() == null || !cacheManager.cacheExists(cacheKey)) {
            logger.info("[getRaw] we have to ReInit, something was wrong");
            init();
        }

        Cache c = cacheManager.getCache(cacheKey);

        if (c.isKeyInCache(serverURI + resourceGroup.getContentType())) {
            logger.info("[getRaw] we found cache for " + serverURI + resourceGroup.getContentType());
            return (String) ((Element) c.get(serverURI + resourceGroup.getContentType())).getValue();
        } else {
            logger.info("[getRaw] cache was not found, something is wrong" + serverURI + resourceGroup.getContentType());
            throw new RuntimeException("[getRaw] cache was not found, something is wrong" + serverURI + resourceGroup.getContentType());
        }
    }
}
