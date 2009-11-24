/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package yui.classes;

/**
 *
 * @author leo
 */
class YUIFilter {

    private String search;
    private String replace;


    public YUIFilter  (String _name,String _value){
        this.search = _name;
        this.replace = _value;
    }

    /**
     * @return the name
     */
    public String getSearch() {
        return search;
    }

    /**
     * @return the value
     */
    public String getReplace() {
        return replace;
    }

    

}
