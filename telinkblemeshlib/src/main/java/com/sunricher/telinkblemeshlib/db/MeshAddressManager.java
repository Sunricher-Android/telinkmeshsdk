package com.sunricher.telinkblemeshlib.db;

import android.content.Context;

import com.sunricher.telinkblemeshlib.MeshNetwork;

import java.util.List;

public class MeshAddressManager {

    private MeshAddressDatabase database;

    private MeshAddressManager() {

    }

    public static MeshAddressManager getInstance(Context context) {
        
        SingletonHolder.instance.database = MeshAddressDatabase.getInstance(context);
        return SingletonHolder.instance;
    }

    public List<Integer> getExistsAddressList(MeshNetwork network) {
        return database.getExistsAddressList(network);
    }

    public List<Integer> getAvailableAddressList(MeshNetwork network) {
        return database.getAvailableAddressList(network);
    }

    public void append(int address, MeshNetwork network) {
        if (address > 255 || address < 1) {
            return;
        }
        database.append(address, network);
    }

    public void remove(int address, MeshNetwork network) {
        database.remove(address, network);
    }

    public void clear(MeshNetwork network) {
        database.clear(network);
    }

    public boolean isExists(int address, MeshNetwork network) {
        return database.isExists(address, network);
    }

    private static class SingletonHolder {
        private static final MeshAddressManager instance = new MeshAddressManager();
    }

}
