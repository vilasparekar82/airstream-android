package com.video.airstream.service;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class LogInformation {
    public static void appendLog(String ipAddress, String text)
    {
        BufferedWriter buf = null;
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD");
        String todayString = formatter.format(todayDate);
        File logFile = new File("sdcard/log_airstream_" + ipAddress + "_" +todayString + ".file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            if(null != buf) {
                try{
                    buf.close();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }

            }

        }
    }
}
