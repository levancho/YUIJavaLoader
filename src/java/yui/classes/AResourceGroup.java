/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

                this.contentType = (yuiFiles[0].indexOf(".js") != -1) ? "application/x-javascript" : "text/css";
                this.metaInfo = yuiFiles[0].split("/");
                this._group = Arrays.asList(yuiFiles);
                logger.info("metainfo:  " + Arrays.toString(this.getMetaInfo()));
                this.version = this.getMetaInfo()[0];
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
