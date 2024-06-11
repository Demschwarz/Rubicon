package org.consul.cube;

import java.util.Arrays;

public class Parameter {
    public String name;
    public int index;
    public int cellTypeId;
    public int innerIndex;

    public Parameter(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public void setValue(DataBlock db, int index, Object value) {
        float realValue = (float) value;
        db.setValue(index, this.index, realValue);
    }

    public float getValue(DataBlock db, int index){
        try{
            return ((float[])db.getData(this.index))[db.getRealInnerIndex(index)];
        }
        catch(IndexOutOfBoundsException e){
            return 0;
        }
    }

    public void setSpace(DataBlockType dbt, DataBlock db){
        if (!db.hasData(this.index)){
            db.setData(this.index, new float[dbt.dataLendth]);
        }
    }

    public void addValue(DataBlock db, int index, Object value) {
        float val = (float) value;
        this.setValue(db, index, this.getValue(db, index) + val);
    }

    public boolean hasThisParameterValue(DataBlock db, int cellIndex){
        return !(((float[])db.getData(this.index))[cellIndex] == 0f);
    }

    @Override
    public String toString() {return this.name;}

    public void reduceArraySize(DataBlock db, int neededSize){
        db.setData(this.index, Arrays.copyOfRange((float[])db.getData(this.index), 0, neededSize));
    }

    public void setZeroValue(DataBlock db, int cellIndex){this.setValue(db, cellIndex, 0f);}
}
