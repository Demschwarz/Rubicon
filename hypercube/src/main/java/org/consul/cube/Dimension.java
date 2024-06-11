package org.consul.cube;

import java.util.ArrayList;
import java.util.List;

public class Dimension {

    String dimName;
    public List<Element> elemList = new ArrayList<Element>();
    public int id;

    public Dimension(String dimName) {
        this.dimName = dimName;
    }

    public Element findElement(String name){
        for (Element elem : this.elemList){
            if (elem.elemName.equals(name)){
                return elem;
            }
        }
        return null;
    }

    public Element findElement(int id){return this.elemList.get(id);}

    public void addElement(Element elem){
        elem.id = this.elemList.size();
        elem.dimId = this.id;
        this.elemList.add(elem);
    }

    public void addNullElem(String newElemName){
        Element elem = new Element(newElemName);
        elem.parentId = -1;
        this.addElement(elem);
    }

    public void updateParentalLayers(int elemInnerId){
        Element parent = this.elemList.get(this.elemList.get(elemInnerId).parentId);
        int potentialParentalLayer = this.elemList.get(elemInnerId).layer + 1;
        if (parent.layer < potentialParentalLayer){
            parent.layer = potentialParentalLayer;
            if (parent.parentId != -1){
                this.updateParentalLayers(parent.id);
            }
        }
    }

    public void addChild(int parentId, String newElemName){
        Element parentElem = this.findElement(parentId);
        Element childElem = new Element(newElemName);
        childElem.parentId = parentId;
        childElem.layer = 0;
        this.addElement(childElem);
        int childId = childElem.id;
        parentElem.childrenIdList.add(childId);
        this.updateParentalLayers(childId);
    }

    public int getElemCount (){return this.elemList.size();}

    public ArrayList<Element> getLeaves(){
        ArrayList<Element> leaves = new ArrayList<>();
        for (Element elem : this.elemList){
            if (elem.childrenIdList.size() > 0){
                leaves.add(elem);
            }
        }
        return leaves;
    }

    public int getLastLeafId(){
        ArrayList<Element> leaves = this.getLeaves();
        return leaves.get(leaves.size()-1).id;
    }

    @Override
    public String toString(){return this.dimName;}
}
