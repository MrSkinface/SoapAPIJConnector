package org.exite.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by levitskym on 18.09.19
 */
public class ZipContainer extends HashMap<String, Object> {

    public byte[] getBin() {
        for (String file : this.keySet()) {
            if (file.endsWith(".bin"))
                return (byte[]) this.get(file);
        }
        return null;
    }

    public byte[] getXml() {
        for (String file : this.keySet()) {
            if (file.endsWith(".xml"))
                return (byte[]) this.get(file);
        }
        return null;
    }

    public void unzip(byte[] zip) throws Exception {
        ZipInputStream zip_in = new ZipInputStream(new ByteArrayInputStream(zip));
        unzip(zip_in, true);
    }

    private void unzip(ZipInputStream zip_in, boolean recursive) throws Exception {
        ZipEntry z_entry;
        while ((z_entry = zip_in.getNextEntry()) != null) {
            String file_name = z_entry.getName();
            ByteArrayOutputStream b_out = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = zip_in.read(buffer)) != -1) {
                b_out.write(buffer, 0, len);
            }
            b_out.close();
            zip_in.closeEntry();
            byte[] content = b_out.toByteArray();
            if (recursive) {
                ZipContainer inner_zip = new ZipContainer();
                inner_zip.unzip(content);
                if (inner_zip.size() > 0 && !file_name.endsWith("xlsx") && !file_name.endsWith("xls") && !file_name.endsWith("doc") && !file_name.endsWith("docx")) {
                    put(file_name, inner_zip);
                } else {
                    put(file_name, content);
                }
            } else
                put(file_name, content);
        }
        zip_in.close();
    }
}
