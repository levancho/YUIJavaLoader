/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Class represents API wrapper around YUICompressor library
 * 
 * @author leo
 */
public class YUIcompressorAPI {

   public  enum InputType {
    JS, CSS
};

    private static final String CHARSET_UTF8 = "UTF-8";

    private Config config;

    public class Config {

        private InputType type;
        private String charset;
        private boolean nomunge;
        private boolean preserveAllSemiColons;
        private boolean disableOptimizations;
        private int linebreakpos;
        private boolean verbose;

         public Config() {
             this(CHARSET_UTF8);
        }

          public Config(String charset) {
              this(charset,false,false,false,10);
        }

           public Config(String charset, boolean nomunge, boolean preserveAllSemiColons,
                boolean disableOptimizations,
                 int linebreakpos) {
                this(charset,nomunge,preserveAllSemiColons,disableOptimizations,linebreakpos,false);
               
        }

        public Config(String charset, boolean nomunge, boolean preserveAllSemiColons,
             boolean disableOptimizations,  int linebreakpos, boolean verbose) {
            this.charset = charset;
            this.nomunge = nomunge;
            this.preserveAllSemiColons = preserveAllSemiColons;
            this.disableOptimizations = disableOptimizations;
            this.linebreakpos = linebreakpos;
            this.verbose = verbose;
            logger.info("Constructing Config Object");
        }

      
        /**
         * @return the charset
         */
        public String getCharset() {
            return charset;
        }

        /**
         * @return the nomunge
         */
        public boolean isNomunge() {
            return nomunge;
        }

        /**
         * @return the preserveAllSemiColons
         */
        public boolean isPreserveAllSemiColons() {
            return preserveAllSemiColons;
        }

        /**
         * @return the disableOptimizations
         */
        public boolean isDisableOptimizations() {
            return disableOptimizations;
        }

      

        /**
         * @return the linebreakpos
         */
        public int getLinebreakpos() {
            return linebreakpos;
        }

        /**
         * @return the verbose
         */
        public boolean isVerbose() {
            return verbose;
        }



        /**
         * @param charset the charset to set
         */
        public void setCharset(String charset) {
            this.charset = charset;
        }

        /**
         * @param nomunge the nomunge to set
         */
        public void setNomunge(boolean nomunge) {
            this.nomunge = nomunge;
        }

        /**
         * @param preserveAllSemiColons the preserveAllSemiColons to set
         */
        public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
            this.preserveAllSemiColons = preserveAllSemiColons;
        }

        /**
         * @param disableOptimizations the disableOptimizations to set
         */
        public void setDisableOptimizations(boolean disableOptimizations) {
            this.disableOptimizations = disableOptimizations;
        }

 

        /**
         * @param linebreakpos the linebreakpos to set
         */
        public void setLinebreakpos(int linebreakpos) {
            this.linebreakpos = linebreakpos;
        }

        /**
         * @param verbose the verbose to set
         */
        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }
        
    }


    public YUIcompressorAPI(){
        this.config = new Config();
    }

    public YUIcompressorAPI(String charset){
        this.config = new Config(charset);
    }

    public YUIcompressorAPI(String charset, boolean nomunge, boolean preserveAllSemiColons,
             boolean disableOptimizations,  int linebreakpos, boolean verbose){
        this.config = new Config(charset,nomunge,preserveAllSemiColons,disableOptimizations,linebreakpos,verbose);
    }

    Logger logger = LoggerFactory.getLogger(YUIcompressorAPI.class);


 public void compressJS(Reader in, Writer out) throws IOException {
     this.compressJS(in, out,this. config);
 }

public void compressJS(Reader in, Writer out,Config conf) throws IOException {
        logger.info("Compressing JS");
        try {
            preProccess(in, out);

            JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

                public void warning(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
                    if (line < 0) {
                        logger.warn("\n[WARNING] " + message);
                    } else {
                        logger.error("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
                    }
                }

                public void error(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
                    if (line < 0) {
                        logger.error("\n[ERROR] " + message);
                    } else {
                        logger.error("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
                    }
                }

                public EvaluatorException runtimeError(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
                    error(message, sourceName, line, lineSource, lineOffset);
                    return new EvaluatorException(message);
                }
            });

            // Close the input stream first, and then open the output stream,
            // in case the output file should override the input file.
            in.close();
            in = null;



//
//        boolean munge = parser.getOptionValue(nomungeOpt) == null;
//        boolean preserveAllSemiColons = parser.getOptionValue(preserveSemiOpt) != null;
//        boolean disableOptimizations = parser.getOptionValue(disableOptimizationsOpt) != null;

            compressor.compress(out, conf.getLinebreakpos(), conf.isNomunge(), conf.isVerbose(),
                    conf.isPreserveAllSemiColons(), conf.isDisableOptimizations());

        } catch (EvaluatorException e) {
            e.printStackTrace();
            // Return a special error code used specifically by the web front-end.
            throw new RuntimeException(e);

        }
    }

public void compress(InputType aType,Reader in,Writer out) throws IOException{

    switch (aType){
        case JS:
              compressJS(in, out);
         break;
         case CSS:
             compressCSS(in, out);
         break;
    }

}


    public void compressCSS(Reader in, Writer out) throws IOException {
            this.compressCSS(in, out,this.config);
    }

    public void compressCSS(Reader in, Writer out,Config conf) throws IOException {

       logger.info("Compressing CSS");
        preProccess(in, out);
        CssCompressor compressor = new CssCompressor(in);
        // Close the input stream first, and then open the output stream,
        // in case the output file should override the input file.
        in.close();
        in = null;
        compressor.compress(out, conf.getLinebreakpos());
    }

    private void preProccess(Reader in, Writer out) throws EvaluatorException {
        if (in == null) {
            throw new EvaluatorException("Can not compress when Input file is Null");
        }
        if (out == null) {
            throw new EvaluatorException("Can not compress when Writer is Null or it does not ");
        }
    }
    
}