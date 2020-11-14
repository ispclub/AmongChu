/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author hoang
 */
public class UserTable implements Serializable{
    private ArrayList<UserTableData> listUTD;
    public void Sort()
    {
        Collections.sort(listUTD, new Comparator<UserTableData>() {
            @Override
            public int compare(UserTableData o1, UserTableData o2) {
                if (o1.getStatus() != o2.getStatus())
                return o1.getStatus().compareTo(o2.getStatus());
            if (o1.getPoint() != o2.getPoint())
                return (o1.getPoint() > o2.getPoint() ? -1 : 1);
            return o1.getUsername().compareTo(o2.getUsername());
            }
        });
    }

    public UserTable(ArrayList<UserTableData> listUTD) {
        this.listUTD = listUTD;
    }

    public UserTable() {
        this.listUTD = new ArrayList<>();
    }

    public ArrayList<UserTableData> getListUTD() {
        return listUTD;
    }

    public void setListUTD(ArrayList<UserTableData> listUTD) {
        this.listUTD = listUTD;
        this.Sort();
    }
    public void addElement(UserTableData utd)
    {
        listUTD.add(utd);
    }
}
