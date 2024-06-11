package org.consul.cube;

import java.util.Arrays;

public class BitMaskType extends Parameter {
    int length;

    public BitMaskType(String name, int index, int length) {
        super(name, index);
        this.length = length;
    }

    public void setValue(DataBlock db, int cellIndex, Object value){
        int bitNum = (int) value;

        byte[] byteArr = ((byte[][]) db.getData(this.index))[cellIndex];

        int intVal = Math.abs((int)bitNum) - 1;

        if (bitNum > 0){
            int pos = this.length - (intVal) / 8 - 1;
            int res = byteArr[pos];
            res |= 1 << (intVal % 8);
            byteArr[pos] = (byte) res;
        } else if (bitNum < 0){
            int pos = this.length - (intVal) / 8;
            int res = byteArr[pos];
            res &= ~(1 << (intVal % 8));
            byteArr[pos] = (byte) res;
        } else {
            for (int pos = 0; pos < this.length; pos++) {
                byteArr[pos + 1] = 0;
            }
        }
    }

    public float getValue(DataBlock db, int cellIndex){
        float result = 0;
        byte[] byteArray = ((byte[][])db.getData(this.index))[cellIndex];

        byte carriage = 1;
        for (int currentIdx = byteArray.length - 1; currentIdx > -1; currentIdx--) {
            byte cur = byteArray[currentIdx];
            for (int cnt = 0; cnt < 8; cnt++) {
                result += (cur == (cur | carriage)) ? 1 : 0;
                carriage <<= 1;
            }
            carriage = 1;
        }
        return result;
    }

    public void setSpace(DataBlockType dbt, DataBlock db){
        if (!db.hasData(this.index)){
            db.setData(this.index, new byte[dbt.dataLength][this.length]);
        }
    }

    public boolean hasThisParameterValue(DataBlock db, int cellIndex){
        return ((byte[])db.getData(this.index))[cellIndex] == 0;
    }

    public void reduceArraySize(DataBlock db, int neededSize){
        db.setData(this.index, Arrays.copyOfRange((byte[][]) db.getData(this.index), 0, neededSize));
    }

    public void setZeroValue(DataBlock db, int cellIndex){
        this.setValue(db, cellIndex, 0);
    }
}
