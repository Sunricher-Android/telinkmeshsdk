package com.sunricher.telinkblemeshlib;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import androidx.annotation.Nullable;

public class MeshOtaFile {

    private String name;
    private int resourceId;
    private String version;
    private int versionCode;

    private MeshOtaFile() {

    }

    private static MeshOtaFile makeWithField(Field field) throws Exception {

        MeshOtaFile otaFile = new MeshOtaFile();
        otaFile.name = field.getName();
        otaFile.resourceId = field.getInt(field);

        String[] components = otaFile.name.split("_");
        if (components.length != 3) {
            return null;
        }

        String versionString = components[2];
        int versionValue = Integer.parseInt(versionString);
        otaFile.versionCode = versionValue;
        otaFile.version = "V" + String.format("%.1f", otaFile.versionCode * 0.1);

        Log.i("MeshOtaFile", "make MeshOtaFile "
                + otaFile.name + ", " + otaFile.version + ", " + otaFile.versionCode);

        return otaFile;
    }

    static MeshOtaFile getOtaFile(MeshDeviceType deviceType) throws Exception {

        int rawValue1 = deviceType.getRawValue1();
        int rawValue2 = deviceType.getRawValue2();

        if (deviceType.getCategory() == MeshDeviceType.Category.light) {

            if (rawValue2 >= 0x30 && rawValue2 <= 0x36) {

                rawValue2 = 0x30;

            } else if (rawValue2 >= 0x60 && rawValue2 <= 0x66) {

                rawValue2 = 0x60;
            }
        }

        String otaFilePrefix = String.format("ota_%02X%02X", rawValue1, rawValue2);

        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {

            if (field.getName().contains(otaFilePrefix)) {

                return MeshOtaFile.makeWithField(field);
            }
        }

        return null;
    }

    private static int getVersionCode(String version) {

        if (version == null) {
            return 0;
        }

        if (version.contains("V")) {

            String valueString = version.replace("V", "");
            valueString = valueString.replace(".", "");
            return Integer.parseInt(valueString, 10);
        }
        return 0;
    }

    public String getName() {
        return name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    byte[] getData(Context context) throws Exception {

        InputStream inputStream = context.getResources().openRawResource(resourceId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toByteArray();
    }

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        MeshOtaFile other = (MeshOtaFile) obj;
        return name.equals(other.name);
    }

    public boolean isNeedUpdate(String version) {

        return getVersionCode(version) < this.versionCode;
    }
}
