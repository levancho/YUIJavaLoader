/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.utils.HTTPUtils;

/**
 *
 * @author leo
 */
public class AResourceGroup {

    Logger logger = LoggerFactory.getLogger(AResourceGroup.class);
    private List<String> _group;
    private String[] metaInfo;
    private String version;
    private String filterType;
    
    //TODO  should be ENUM
    private String contentType;
    private String queryString = "";

    AResourceGroup(String queryString) {
        parseUrl(queryString);
    }


    AResourceGroup(HttpServletRequest request) {
        parseRequest(request);
    }

    private void extractmetaInfo (String item) {

                this.contentType =(item.indexOf(".js") != -1) ? HTTPUtils.CONTENT_TYPE.JAVASCRIPT+"": HTTPUtils.CONTENT_TYPE.CSS+"";
                this.metaInfo = item.split("/");
                logger.info("metainfo:  " + Arrays.toString(this.getMetaInfo()));
                this.version = this.getMetaInfo()[0];
    }

     private void parseRequest(HttpServletRequest request) {
        try {
            queryString = URLDecoder.decode(request.getQueryString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
               logger.error("error occured getting query String" + ex );
        }

        _group = new ArrayList(request.getParameterMap().size());
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            String i =  e.nextElement();
            logger.info("Adding Item: " +  i);
            _group.add( i);
        }

        extractmetaInfo(_group.get(0));

//        Map m = request.getParameterMap();
//
//         for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
//                 Map.Entry pairs = (Map.Entry) it.next();
//                 String name = (String) pairs.getKey();
//                 String[] value = (String[]) pairs.getValue();
//                     logger.info("name: " + name+" value "+Arrays.toString(value));
//         }

    }

    private void parseUrl(String _q) {
        String[] yuiFiles = null;

        try {
            if (_q != null && !_q.trim().equals("")) {
                queryString = URLDecoder.decode(_q, "UTF-8");
            }

            logger.info("queryStringis " + getQueryString());

            if (!queryString.equals("")) {
                yuiFiles = getQueryString().split("&");
                if (yuiFiles == null || yuiFiles.length == 0) {
                    logger.info("thre is nothing in query?" + getQueryString());
                    return;
                }

                extractmetaInfo(yuiFiles[0]);
                _group= Arrays.asList(yuiFiles);
            }

        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }

    }

    AResourceGroup(Enumeration<String> params) {
    }

    public boolean isDebug() {
        return (getQueryString().indexOf("-debug.js") != -1);
    }

    public boolean isMin() {
        return (getQueryString().indexOf("-min.js") != -1);
    }

    /**
     * @return the _group
     */
    public List<String> getGroup() {
        return _group;
    }

    /**
     * @return the metaInfo
     */
    public String[] getMetaInfo() {
        return metaInfo;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the filterType
     */
    public String getFilterType() {
        return filterType;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the queryString
     */
    public String getQueryString() {
        return queryString;
    }
}
