package org.consul.cube;

public class DataBlock {
    private Object[] data;
    public DataBlock(int len, int depth) {
        this.data = new Object[len+2];

        this.setData(-2, new int[depth]);
        for (int idx = 0; idx < depth; idx++) {
            this.setValue(idx, -2, -1);
        }
        this.setData(-1, new int[depth]);
        for (int innerIdx = 0; innerIdx < depth; innerIdx++) {
            this.setValue(innerIdx, -1, innerIdx);
        }
    }

    public boolean hasData(int paramIndex, int cellIndex){
        return ((Object[])this.data[paramIndex + 1])[cellIndex] != null;
    }

    public boolean hasData(int paramIndex){
        return this.data[paramIndex + 2] != null;
    }

    public void setData(int index, Object whatToSet){this.data[index+2] = whatToSet;}

    public int getCellTypeId(int index) {return ((int[])this.data[0])[index]%256;}

    public void setCellTypeId(int index, int id){
        ((int[])this.data[0])[index] -= ((int[])this.data[0])[index] % 256;
        ((int[])this.data[0])[index] += id;
    }

    public Object getData(int paramIndex){
        return this.data[paramIndex+2];
    }

    public void setValue(int cellIndex, int paramIndex, float value){
        if (this.hasData(paramIndex)) {
            ((float[])this.data[paramIndex + 2])[cellIndex] = value;
        }
    }

    public void setValue(int cellIndex, int paramIndex, int value){
        if (this.hasData(paramIndex)) {
            ((int[])this.data[paramIndex + 2])[cellIndex] = value;
        }
    }


    public int getDataLength(){return this.data.length;}

    public void setAllCellTypeId(int id, int border){
        int res = id  + border * 256;
        for (int i = 0; i < ((int[])this.data[0]).length; i++) {
            this.setValue(i, -2, res);
        }
    }

    public int getTotalSize(){return ((int[])this.data[0]).length;}

    public int getRealInnerIndex(int index) {return ((int[])this.data[1])[index];}

    public void addDataBlock(Cube cube, DataBlock toAdd, DataBlockType dbt,  long ... params) throws Exception{
        for (int paramIdx = 0; paramIdx < dbt.paramList.length; paramIdx++) {
            Parameter param = dbt.paramList[paramIdx];

            if (params[param.cellTypeId] == (params[param.cellTypeId] | 1L << param.innerIndex)){
                for (int cellIdx = 0; cellIdx < dbt.dataLength; cellIdx++) {
                    int ct1 = this.getCellTypeId(cellIdx);
                    int ct2 = toAdd.getCellTypeId(cellIdx);
                    if (ct1 != ct2) {
                        for (int newCellIdx = cellIdx - 1; newCellIdx > -1; newCellIdx--) {
                            param.addValue(this, this.getRealInnerIndex(cellIdx), -1 * param.getValue(toAdd, toAdd.getRealInnerIndex(cellIdx)));
                        }
                        for (int newParamIdx = paramIdx; newParamIdx > -1; newParamIdx++) {
                            Parameter newParam = dbt.paramList[newParamIdx];
                            for (int newCellIdx = 0; newCellIdx < dbt.dataLength; newCellIdx++) {
                                param.addValue(this, this.getRealInnerIndex(cellIdx), -1 * param.getValue(toAdd, toAdd.getRealInnerIndex(cellIdx)));
                            }
                        }
                        throw new Exception("Не совпадают типы ячеек");
                    }
                    else {
                        if (ct2 == param.cellTypeId){
                            param.addValue(this, cellIdx, param.getValue(toAdd, cellIdx));
                        }
                    }
                }
            }

        }
    }
}
