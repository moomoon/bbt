package org.ddrr.bbt.persistent;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PhoebeHuyi on 2014.12.25.
 */
public abstract class DIParser {

    private FileInputStream fis;
    private BufferedReader br;
    private List<String[]> parsedEntries;


    public void start(String filePath, int startLine) {
        try {
            fis = new FileInputStream(Environment.getExternalStorageDirectory() + filePath);

//            UniversalDetector detector = new UniversalDetector(null);
//            int nread;
//            byte[] buf = new byte[4096];
//
//            while ((nread = fis.read()) > 0 && !detector.isDone()) {
//                detector.handleData(buf, 0, nread);
//            }
//            detector.dataEnd();
//
//            String encoding = detector.getDetectedCharset();
//            if (encoding != null) {
//                System.out.println("Detected encoding = " + encoding);
//            } else {
//                System.out.println("No encoding detected.");
//            }
//            detector.reset();
            br = new BufferedReader(new InputStreamReader(fis));
            int i = startLine;
            while (i > 0) {
                i--;
                br.readLine();
            }
            parsedEntries = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] read() {
        if (parsedEntries.size() > 0) {
            String[] result = parsedEntries.get(0);
            parsedEntries.remove(0);
            return result;
        } else {
            try {
                String b;
                if (null != (b = br.readLine())) {
                    Log.e("read line", "line = " + b);
                    parsedEntries.addAll(parseLine(b));
                    return read();
                } else {
                    fis.close();
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    protected abstract List<String[]> parseLine(String line);
}
