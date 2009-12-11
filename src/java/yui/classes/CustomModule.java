/*
 *  Copyright (c) 2009, Amostudio,inc
 *  All rights reserved.
 *  Code licensed under the BSD License:
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Amostudio,inc  nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY Amostudio,inc ''AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL Amostudio,inc  BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package yui.classes;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author leo
 */
public class CustomModule implements Map<Object, Object> {



    private Map _backing;
    private List requires;

    public CustomModule (String _name) {
        this();
        this.setName(_name);

    }

     public CustomModule (String _name,String _type) {
        this(_name);
        this.setType(_type);
    }


    public CustomModule () {
          this._backing = new LinkedHashMap();
          this.requires = new LinkedList();
    }

    public int size() {
        return _backing.size();
    }

    public boolean isEmpty() {
        return _backing.isEmpty();
    }

    public boolean containsKey(Object key) {
        return _backing.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return _backing.containsValue(value);
    }

    public Object get(Object key) {
        return _backing.get(key);
    }

    public Object put(Object key, Object value) {
        return _backing.put(key, value);
    }

    public Object remove(Object key) {
        return _backing.remove(key);
    }

    public void putAll(Map<? extends Object, ? extends Object> m) {
        _backing.putAll(m);
    }

    public void clear() {
        _backing.clear();
    }

    public Set<Object> keySet() {
        return _backing.keySet();
    }

    public Collection<Object> values() {
        return _backing.values();
    }

    public Set<Entry<Object, Object>> entrySet() {
        return _backing.entrySet();
    }

    /**
     * @return the name
     */
    public String getName() {
      return  (String)this._backing.get("name");
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
       this._backing.put("name", name);
    }

    /**
     * @return the type
     */
    public String getType() {
         return  (String)this._backing.get("type");
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this._backing.put("type", type);
    }

    /**
     * @return the fullpath
     */
    public String getFullpath() {
         return  (String)this._backing.get("fullpath");
    }

    /**
     * @param fullpath the fullpath to set
     */
    public void setFullpath(String fullpath) {
        this._backing.put("fullpath", fullpath);
    }

  

//    /**
//     * @return the requires
//     */
//    public List getRequires() {
//        return requires;
//    }
//
//    /**
//     * @param requires the requires to set
//     */
//    public void setRequires(List requires) {
//        this.requires = requires;
//    }

    public void addRequires (String... reqs){
         for (String req : reqs) {
            this.requires.add(req);
        }
        
        this._backing.put("requires", this.requires);
    }

    public void removeRequires (String... reqs){
         for (String req : reqs) {
            this.requires.remove(req);
        }
         if(this.requires.size()==0){
            this._backing.remove("requires");
         }
    }

    public void clearRequires () {
        this.requires.clear();
        this._backing.remove("requires");
    }
}
