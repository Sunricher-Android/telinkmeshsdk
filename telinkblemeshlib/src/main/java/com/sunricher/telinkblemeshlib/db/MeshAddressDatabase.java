package com.sunricher.telinkblemeshlib.db;

import android.content.Context;

import com.sunricher.telinkblemeshlib.MeshNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MeshAddress.class}, version = 1)
abstract class MeshAddressDatabase extends RoomDatabase {

    private static volatile MeshAddressDatabase instance;

    private ArrayList<Integer> totalAddresses;

    static MeshAddressDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (MeshAddressDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context, MeshAddressDatabase.class, "MeshAddress").allowMainThreadQueries().build();

                    instance.totalAddresses = new ArrayList<>();
                    for (int i = 1; i <= 255; i++) {
                        instance.totalAddresses.add(i);
                    }
                }
            }
        }
        return instance;
    }

    abstract MeshAddressDao meshAddressDao();

    List<Integer> getExistsAddressList(MeshNetwork network) {

        List<Integer> addressList = new ArrayList<>();
        List<MeshAddress> savedList = meshAddressDao().selectAll(network.getName(), network.getPassword());
        for (MeshAddress address : savedList) {
            addressList.add(address.address);
        }

        return addressList;
    }

    List<Integer> getAvailableAddressList(MeshNetwork network) {

        HashSet<Integer> totalSet = new HashSet<>(totalAddresses);
        List<Integer> savedList = this.getExistsAddressList(network);
        totalSet.removeAll(savedList);

        ArrayList<Integer> addressList = new ArrayList<>(totalSet);
        Integer[] addresses = new Integer[addressList.size()];
        addresses = (Integer[]) addressList.toArray(addresses);
        Arrays.sort(addresses);

        return new ArrayList<>(Arrays.asList(addresses));
    }

    void append(int address, MeshNetwork network) {

        MeshAddress meshAddress = new MeshAddress(address, network);
        meshAddressDao().insert(meshAddress);
    }

    void remove(int address, MeshNetwork network) {

        meshAddressDao().delete(address, network.getName(), network.getPassword());
    }

    void clear(MeshNetwork network) {

        meshAddressDao().deleteAll(network.getName(), network.getPassword());
    }

    boolean isExists(int address, MeshNetwork network) {

        return meshAddressDao().select(address, network.getName(), network.getPassword()).size() > 0;
    }

}
