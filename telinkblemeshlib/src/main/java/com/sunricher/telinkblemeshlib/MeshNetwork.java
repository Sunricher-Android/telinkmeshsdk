package com.sunricher.telinkblemeshlib;

import com.sunricher.telinkblemeshlib.telink.Strings;

import androidx.annotation.Nullable;

public class MeshNetwork {

    private String name;
    private String password;

    private byte[] meshName;
    private byte[] meshPassword;

    public MeshNetwork(String name, String password) {

        assert(name.length() >= 2 && name.length() <= 16);
        assert(password.length() >= 2 && password.length() <= 16);

        this.name = name;
        this.password = password;

        this.meshName = Strings.stringToBytes(name, 16);
        this.meshPassword = Strings.stringToBytes(password, 16);
    }

    public static final MeshNetwork factory = new MeshNetwork("Srm@7478@a", "475869");

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {

        if (obj == null) {
            return false;
        }

        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }

        MeshNetwork another = (MeshNetwork)obj;
        return this.name.equals(another.name) && this.password.equals(another.password);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getMeshName() {
        return meshName;
    }

    public byte[] getMeshPassword() {
        return meshPassword;
    }
}
