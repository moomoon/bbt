package org.ddrr.bbt.persistent;

import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.25.
 */
public abstract class DIPersister {
    public boolean write(String filePath, String header, List<Map<String, String>> rawData) {
        try {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + filePath);
            fos.write(header.getBytes());
            for (Map<String, String> entry : rawData) {
                fos.write("\r\n".getBytes());
                fos.write(createLine(entry).getBytes());
            }
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected abstract String createLine(Map<String, String> entry);
}
