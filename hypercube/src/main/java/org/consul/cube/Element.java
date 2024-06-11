package org.consul.cube;

import java.util.ArrayList;
import java.util.List;

public class Element {
    public String elemName;
    public int id;
    public int parentId = -1;
    public int dimId;
    List<Integer> childrenIdList = new ArrayList<>();
    public int layer = -1;

    public Element(String elemName) {
        this.elemName = elemName;
    }

    @Override
    public String toString() {return this.elemName;}
}
