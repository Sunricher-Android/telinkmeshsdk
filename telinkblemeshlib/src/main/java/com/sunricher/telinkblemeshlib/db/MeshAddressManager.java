package com.sunricher.telinkblemeshlib.db;

import android.content.Context;

import com.sunricher.telinkblemeshlib.MeshNetwork;

import java.util.List;

public class MeshAddressManager {

    private MeshAddressManager() {

    }

    public static MeshAddressManager getInstance() {
        return SingletonHolder.instance;
    }

    public List<Integer> getExistsAddressList(MeshNetwork network, Context context) {
        return MeshAddressDatabase.getInstance(context).getExistsAddressList(network);
    }

    public List<Integer> getAvailableAddressList(MeshNetwork network, Context context) {
        return MeshAddressDatabase.getInstance(context).getAvailableAddressList(network);
    }

    public void append(int address, MeshNetwork network, Context context) {
        if (address > 255 || address < 1) {
            return;
        }
        MeshAddressDatabase.getInstance(context).append(address, network);
    }

    public void remove(int address, MeshNetwork network, Context context) {
        MeshAddressDatabase.getInstance(context).remove(address, network);
    }

    public void clear(MeshNetwork network, Context context) {
        MeshAddressDatabase.getInstance(context).clear(network);
    }

    public boolean isExists(int address, MeshNetwork network, Context context) {
        return MeshAddressDatabase.getInstance(context).isExists(address, network);
    }

    private static class SingletonHolder {
        private static final MeshAddressManager instance = new MeshAddressManager();
    }

}
