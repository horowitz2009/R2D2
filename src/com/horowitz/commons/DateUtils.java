package com.horowitz.commons;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DateUtils {

  public static String fancyTime(long time) {
    String res = "";
    long s = time / 1000;

    long m = s / 60;
    long ss = s % 60;
    long h = m / 60;
    long mm = m % 60;
    long d = h / 24;
    long hh = h % 24;

    res = d + "d " + formatNumber(hh, 2) + ":" + formatNumber(mm, 2) + ":" + formatNumber(ss, 2);

    return res;
  }

  public static String fancyTime2(long time) {
   return fancyTime2(time, false); 
  }
  
  public static String fancyTime2(long time, boolean showSeconds) {
    String res = "";
    long s = time / 1000;

    long m = s / 60;
    long ss = s % 60;
    long h = m / 60;
    long mm = m % 60;
    long d = h / 24;
    long hh = h % 24;
    if (d > 0) {
      res = res + (d + "d ");
    }

    if (hh > 0) {
      res = res + (hh + "h ");
    }

    if (mm > 0) {
      res = res + (mm + "m ");
    }
    if (showSeconds || res.trim().isEmpty())
      res = res + (ss + "s");

    return res;
  }

  public static String formatNumber(long number, int leadingZeros) {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMinimumIntegerDigits(leadingZeros);
    return nf.format(number);
  }

  public static String formatDateForFile(long time) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH mm ss");
    return df.format(time);
  }
  
  public static String formatDateForFileMS(long time) {
	  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH mm ss.SSS");
	  return df.format(time);
  }
  
  public static String formatDateToISO(long time) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return df.format(time);
  }
  
  public static Date parseFromISO(String isoDate) throws ParseException {
	  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	  return df.parse(isoDate);
  }
  
  public static String formatDateForFile2(long time) {
    SimpleDateFormat df = new SimpleDateFormat("MM-dd  HH mm");
    return df.format(time);
  }

  public static void main(String[] args) {
    Calendar cal = Calendar.getInstance();
    long t1 = cal.getTimeInMillis();
    // cal.add(Calendar.MINUTE, 13);
    cal.add(Calendar.MILLISECOND, 980);
    // cal.add(Calendar.DAY_OF_MONTH, 3);
    long t2 = cal.getTimeInMillis();
    System.out.println(fancyTime(t2 - t1));
    System.out.println(fancyTime2(t2 - t1));

    File f = new File(".");
    File[] files = f.listFiles();
    List<File> pingFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith("ping")) {
        System.err.println(file.getName() + " - " + file.lastModified());
        pingFiles.add(file);
        cnt++;
      }
    }

    if (cnt > 5) {
      // delete some files
      System.err.println();
      Collections.sort(pingFiles, new Comparator<File>() {
        public int compare(File o1, File o2) {
          if (o1.lastModified() > o2.lastModified())
            return 1;
          else if (o1.lastModified() < o2.lastModified())
            return -1;
          return 0;
        };
      });

      for (Iterator<File> it = pingFiles.iterator(); it.hasNext();) {
        File file = it.next();
        System.err.println(file.getName() + " " + file.lastModified());
      }
      int c = cnt - 5;
      for (int i = 0; i < c; i++) {
        File fd = pingFiles.get(i);
        fd.delete();
      }

    }

    File fn = new File("ping" + formatDateForFile(System.currentTimeMillis()) + ".png");
    try {
      fn.createNewFile();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  public static long parse(String s) {
    long res = 0;

    try {
      String[] ss = s.trim().split(" ");

      for (int i = 0; i < ss.length; i++) {
        res += parseSimple(ss[i]);
      }
    } catch (Exception e) {
    }

    return res;

  }

  public static long parseSimple(String s) {
    long res = 0;

    Set<String> letters = new HashSet<>();
    letters.add("d");
    letters.add("h");
    letters.add("m");
    letters.add("s");
    long multiplier = 0;
    String ch = s.substring(s.length() - 1).toLowerCase();
    if (letters.contains(ch)) {
      String sn = s.substring(0, s.length() - 1);
      res = Long.parseLong(sn);
      if (ch.equalsIgnoreCase("d")) {
        multiplier = 24 * 60 * 60 * 1000; // a day
      } else if (ch.equalsIgnoreCase("h")) {
        multiplier = 60 * 60 * 1000; // an hour
      } else if (ch.equalsIgnoreCase("m")) {
        multiplier = 60 * 1000; // a minute
      } else {
        multiplier = 1000; // a second
      }

      res *= multiplier;
    }

    return res;
  }


}
