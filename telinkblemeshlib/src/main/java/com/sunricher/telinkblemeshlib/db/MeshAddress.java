package com.sunricher.telinkblemeshlib.db;

import com.sunricher.telinkblemeshlib.MeshNetwork;

import org.jetbrains.annotations.NotNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"address", "name", "password"})
class MeshAddress {

    int address;

    @NotNull
    String name;

    @NotNull
    String password;

//    MeshAddress() {
//
//    }

    MeshAddress(int address, String name, String password) {
        this.address = address;
        this.name = name;
        this.password = password;
    }

    MeshAddress(int address, MeshNetwork network) {
        this.address = address;
        this.name = network.getName();
        this.password = network.getPassword();
    }

}
