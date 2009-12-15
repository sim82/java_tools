/*
 * Copyright (C) 2009 Simon A. Berger
 * 
 *  This program is free software; you may redistribute it and/or modify its
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 */
package ml;

/**
 * shamelessly ripped from the interweb: http://www.cs.technion.ac.il/~imaman/programs/dirscanner.html
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DirScanner
{
   private static final String SEP = System.getProperty("file.separator");
   private final List<File> files = new ArrayList<File>();
   private final List<String> patts = new ArrayList<String>();

   public static Iterable<File> scan(String... patterns) {
      return scan(new File(System.getProperty("user.dir")), patterns);
   }

   public static Iterable<File> scan(Collection<String> patterns) {
      return scan(patterns.toArray(new String[0]));
   }
   
   private static boolean isSubtract(String patt) {
      return patt.startsWith("-");
   }
   
   private static String rawPatt(String patt) {
      if(!isSubtract(patt))
         return patt;
      return patt.substring(1);
   }
   
   private static String finalizePatt(String patt, boolean isSubtract) {
      if(isSubtract)
         patt = "-" + patt;
      return patt;
   }
   
   public static Iterable<File> scan(File dir, String... patterns) {
      DirScanner s = new DirScanner();
      for(String p : patterns) {
         p = p.replace(SEP, "/");
         p = p.replace(".", "\\.");
         p = p.replace("*", ".*");
         p = p.replace("?", ".?");
         s.patts.add(p);
      }
      
      s.scan(dir, new File("/"));
      return s.files;
   }
      
   private void scan(File dir, File path) {
      File[] fs = dir.listFiles();
      for(File f : fs) {
         File rel = new File(path, f.getName());
         if(f.isDirectory()) {
            scan(f, rel);
            continue;
         }
           
         if(match(patts, rel)) 
            files.add(rel);
      }
   }


   private static boolean match(Iterable<String> patts, File rel) {
      
      boolean ok = false;
      for(String p : patts) {
         boolean subtract = isSubtract(p);
         p = rawPatt(p);
         
         boolean b = match(p, rel);
         if(b && subtract)
            return false;
         
         if(b)
            ok = true;
      }
      
      return ok;
   }
   
   private static boolean match(String p, File rel) {          
      String s = rel.getName();
      if(p.indexOf('/') >= 0)
         s = rel.toString();
      
      s = s.replace(SEP, "/");
      return s.matches(p);
   }   
}



