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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.utils.HTTPUtils;

/**
 * Abstract Class to be used by extendor to provide cline side cachability, where client side
 * means Most of the times User's Browser, it provides custom implementations of equals and compareTo
 * methods comparing two resources on their midification date, and etag values
 * @author leo
 */
public abstract class AClientCachable implements Comparable<AClientCachable>, Cloneable, Externalizable {
   private static final Logger logger =LoggerFactory.getLogger(HTTPUtils.class);

   private String eTagPrefix;
   private long fileSize;

   private long  modified;

  /**
   * Etag representation of this resource
   * @return String etag value
   */
   public String getEtag() {
       return eTagPrefix+fileSize;
   }

  /**
   * Modified date of this resource.
   * @return
   */
   public long getModified() {
       return modified;
   }

  /**
   * This constructor will set modified date of this resource
   * to be same as time of its execution ussing:
   * System.currentTimeMillis()
   */
  public  AClientCachable () {
      this(0, System.currentTimeMillis());
      logger.warn("[Default Constructor Should not Be used");
  }

   public  AClientCachable (long filesizeParam,long modifiedParam ) {
        this(filesizeParam,modifiedParam,HTTPUtils.getDefaultEtagPrefix());
   }

    public  AClientCachable (long filesizeParam,long modifiedParam ,String eTagPrefixParam) {
    this.fileSize = filesizeParam;
    this.modified = modifiedParam;
    this.eTagPrefix = eTagPrefixParam;
   }

      /**
       * Compares this to o based on their, modified Date objects,
       * (truncating milliseconds to 0). and their Etag Values compared as pure Strings.
       *
       * @param o
       * @return
       */
      public int compareTo(AClientCachable o) {
          logger.trace("[compareTo] this "+this + "  to" +o);
            String oTag = o.getEtag();
            String myTag = getEtag();

            Date  oModified =   HTTPUtils.getNormalizedDate(o.getModified());
            Date myModified =  HTTPUtils.getNormalizedDate(getModified());
            
            int diff = myModified.compareTo(oModified);
            if(myTag.equalsIgnoreCase(oTag)){
                return diff;
            }else {
                // considering not equal
                logger.trace("[compareTo] Etags this=["+this.getEtag() + "] o= ["+o.getEtag()+ "]  do not match");
                return -1;
            }
    }

    /**
     *   checks for equality between this and o, based on their, modified Date objects,
       * (truncating milliseconds to 0); and their Etag Values compared as pure Strings.
     * @param o
     * @return bollean
     */
    @Override
      public boolean equals(Object o){
        logger.trace("[equals]  this=["+this + "] o= ["+o+ "] ");
        if(!(o instanceof AClientCachable) ){
             logger.trace("[equals]  Incorrect Instance o ["+o.getClass()+"]");
            return false;
        }
        AClientCachable other = (AClientCachable)o;
        int diff = this.compareTo(other);
         logger.trace("[equals]  diff is"+diff);
          return (diff==0);
      }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (int) (this.fileSize ^ (this.fileSize >>> 32));
        hash = 59 * hash + (int) (this.modified ^ (this.modified >>> 32));
        return hash;
    }

// TODO       public int hashCode2() {
//
//          String s =MD5.asHex(getEtag().getBytes());
//       return  0;
//    }

    @Override
    public String toString () {

        return this.fileSize+"|"+this.getModified()+"|"+this.getEtag();
    }
    



    public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

}
