package org.consul.cube;

import java.util.List;

public class CellType {
    public String typeName;
    private int id;
    private int border;
    public int currentSize = -1;
    public List<Parameter> paramList;

    public CellType(int id, String typeName, List<Parameter> paramList) {
        this.id = id;
        this.typeName = typeName;
        this.paramList = paramList;
    }

    public int getSpecificParamsCount(){
        int counter = 0;
        for (Parameter param : this.paramList) {
            if (param instanceof BitMaskType) {
                counter++;
            }
        }
        return counter;
    }

    public Parameter findParam(String paramName) throws Exception {
        for (Parameter param : this.paramList) {
            if (param.name.equals(paramName)) {
                return param;
            }
        }
        throw new CubeCellTypeException("У CellType " + this.id + " нет параметра " + paramName);
    }

    private int getBitMaskLength(){
        int counter = 0;
        for (Parameter param : this.paramList) {
            if (param instanceof BitMaskType){
                BitMaskType newParam = (BitMaskType) param;
                counter += newParam.length;
            }
        }
        return counter;
    }

    public void setBorder(int borderindex){
        this.border = borderindex;
    }

    public float getBorderValue(){return (float) this.border * 256;}

    public String getTypeName(){return this.typeName;}

    public int getId(){return this.id;}

    public int getBorder(){return this.border;}
}
