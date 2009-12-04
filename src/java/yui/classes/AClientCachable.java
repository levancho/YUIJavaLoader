/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author leo
 */
public class AClientCachable implements Comparable<AClientCachable>, Cloneable, Externalizable {
   private static final Logger logger =LoggerFactory.getLogger(HTTPUtils.class);

   private String eTagPrefix;
   private long fileSize;

   private long  modified;

  public String getEtag() {
       return eTagPrefix+fileSize;
   }

   public long getModified() {
       return modified;
   }

  public  AClientCachable () {
      this(0, System.currentTimeMillis());
      logger.warn("[Default Constructor Should not Be used");
  }

   public  AClientCachable (long filesizeParam,long modifiedParam   ) {
        this(filesizeParam,modifiedParam,HTTPUtils.getDefaultEtagPrefix());
   }

    public  AClientCachable (long filesizeParam,long modifiedParam ,String eTagPrefixParam) {
    this.fileSize = filesizeParam;
    this.modified = modifiedParam;
    this.eTagPrefix = eTagPrefixParam;
   }

  
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
