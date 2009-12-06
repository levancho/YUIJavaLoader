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
 *     * Neither the name of the Amostudio,inc  nor the
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
package yui.classes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class IOUtils {

    private static final Logger logger = LoggerFactory.getLogger(HTTPUtils.class);

    public static void fastCopy(File source, File dest) throws IOException {

        FileInputStream fi = new FileInputStream(source);
        FileChannel fic = fi.getChannel();
        MappedByteBuffer mbuf = fic.map(
                FileChannel.MapMode.READ_ONLY, 0, source.length());
        fic.close();
        fi.close();


        FileOutputStream fo = new FileOutputStream(dest);
        FileChannel foc = fo.getChannel();
        foc.write(mbuf);
        foc.close();
        fo.close();

    }

    public static String readFileContent(String path) {
        try {
            if (!new File(path).exists()) {
                return "";
            }

            FileInputStream fis = new FileInputStream(path);
            int x = fis.available();
            byte b[] = new byte[x];

            fis.read(b);

            return new String(b);
        } catch (IOException e) {
            // Ignore
        }

        return "";
    }

    public static void main(String args[]) {
        if (args.length != 1) {
            logger.debug("Usage: java FastStreamCopy filename");
            System.exit(1);
        }

        NumberFormat digits = NumberFormat.getInstance();
        digits.setMaximumFractionDigits(3);

        long before;
        long after;
        double slowTime;
        double fastTime;
        double speedUp;

        String filename = args[0];
        String contents;

// Slow method
        logger.debug("Reading file " + args[0] + " using slow method");
        before = System.currentTimeMillis(); // Start timing
// contents = slowStreamCopy(filename);
        contents = readFile(filename);
        after = System.currentTimeMillis(); // End timing
        slowTime = after - before;
// logger.debug("File's contents:\n" + contents);

// Fast method
        logger.debug("Reading file " + args[0] + " using fast method");
        before = System.currentTimeMillis(); // Start timing
        contents = readFile(filename);
        after = System.currentTimeMillis(); // End timing
        fastTime = after - before;
// logger.debug("File's contents:\n" + contents);

// Comparison
        speedUp = 100d * slowTime / fastTime;
        logger.debug("Slow method required " + slowTime + " ms.");
        logger.debug("Fast method required " + fastTime + " ms.");
        logger.debug("Speed up = " + digits.format(speedUp) + "% ");
        logger.debug(speedUp > 100 ? "Good!" : "Bad!");
    }

    /**
     *
     * good for Large Files >2Mb
     * @param filename
     * @return
     */
    private static byte[] largeFileReader(String filename) {
        byte[] bytes = null;
        FileChannel fc = null;
        try {
            fc = new FileInputStream(filename).getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            int size = byteBuffer.capacity();
            if (size > 0) {
                // Retrieve all bytes in the buffer
                byteBuffer.clear();
                bytes = new byte[size];
                byteBuffer.get(bytes, 0, bytes.length);
            }
            fc.close();
        } catch (FileNotFoundException fnf) {
            System.err.println("" + fnf);
        } catch (IOException io) {
            System.err.println("" + io);
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
        return bytes;
    }

    public static String readFile(String filename) {
        return readFile(filename, false);
    }

    public static String readFile(String filename, boolean useNIO) {
        String s = "";
        if (useNIO) {
            s = new String(largeFileReader(filename));
        } else {
            s = new String(smallFileReader(filename));
        }
        return s;
    }

    public static byte[] smallFileReader(String filename) {
        byte[] buffer = null;
        FileInputStream in = null;
        try {
            File file = new File(filename);
            int size = (int) file.length();
            if (size > 0) {
                buffer = new byte[size];
                in = new FileInputStream(file);
                in.read(buffer, 0, size);
                //s = new String(buffer, 0, size);
                in.close();
            }
        } catch (FileNotFoundException fnfx) {
            System.err.println("File not found: " + fnfx);
        } catch (IOException iox) {
            System.err.println("I/O problems: " + iox);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        return buffer;
    }

    public static byte[] fileReadNIO(String name) {
        FileInputStream f = null;
        ByteBuffer bb = null;
        try {
            f = new FileInputStream(name);
            FileChannel ch = f.getChannel();
            bb = ByteBuffer.allocateDirect(1024);
            long checkSum = 0L;
            int nRead;
            while ((nRead = ch.read(bb)) != -1) {
                bb.position(0);
                bb.limit(nRead);
                while (bb.hasRemaining()) {
                    checkSum += bb.get();
                }
                bb.clear();
            }
        } catch (FileNotFoundException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        return bb.array();

    }
}
