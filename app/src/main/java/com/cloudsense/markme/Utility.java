package com.cloudsense.markme;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {

    public static String dateGet()
    {
        Date c = Calendar.getInstance().getTime();
        Calendar c1 = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c1.getTime());
        Log.d("Date","DATE : " + strDate);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        return formattedDate;
    }
    public static String prevDate()
    {
        Calendar c1=Calendar.getInstance();
        c1.add(Calendar.DATE,-1);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String da=df.format(c1.getTime());
        return da;
    }
    public static String styleDate()
    {
        Calendar c1=Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        String da=df.format(c1.getTime());
        return da+", ";
    }
    public static String convDate(int mon,int year,int dat)
    {
        String date=new String();
        if(dat/10 ==0)
            date+="0"+dat;
        else
            date+=dat;
        switch(mon)
        {
            case 1:
                date+="-Jan-";
                break;
            case 2:
                date+="-Feb-";
                break;
            case 3:
                date+="-Mar-";
                break;
            case 4:
                date+="-Apr-";
                break;
            case 5:
                date+="-May-";
                break;
            case 6:
                date+="-Jun-";
                break;
            case 7:
                date+="-Jul-";
                break;
            case 8:
                date+="-Aug-";
                break;
            case 9:
                date+="-Sep-";
                break;
            case 10:
                date+="-Oct-";
                break;
            case 11:
                date+="-Nov-";
                break;
            case 12:
                date+="-Dec-";
                break;
        }
        date+=year;
        Log.d("SELEC DATE",date);
        return date;
    }
}
