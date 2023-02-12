package ca.diro.javadocindexer;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * http://www.devx.com/getHelpOn/10MinuteSolution/20447
 * @author devx.com
 *
 */
public class Unzip {

  public static final void copyInputStream(InputStream in, OutputStream out)
  throws IOException
  {
    byte[] buffer = new byte[1024];
    int len;

    while((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }

  public static final void main(String arg, String basePath) {
    Enumeration entries;
    ZipFile zipFile;

    try {
      zipFile = new ZipFile(arg);

      entries = zipFile.entries();

      while(entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry)entries.nextElement();

        if(entry.isDirectory()) {
          // Assume directories are stored parents first then children.
          System.err.println("Extracting directory: " + basePath+Settings.sep+entry.getName());
          // This is not robust, just for demonstration purposes.
          (new File(basePath+Settings.sep+entry.getName())).mkdir();
          continue;
        }

        System.err.println("Extracting file: " + entry.getName());
		new File(new File(basePath+Settings.sep+entry.getName()).getParent()).mkdirs();
        copyInputStream(zipFile.getInputStream(entry),
           new BufferedOutputStream(new FileOutputStream(basePath+Settings.sep+entry.getName())));
      }

      zipFile.close();
    } catch (IOException ioe) {
      System.err.println("Unhandled exception:");
      ioe.printStackTrace();
      return;
    }
  }

}
