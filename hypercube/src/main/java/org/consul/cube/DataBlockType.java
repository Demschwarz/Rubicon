package org.consul.cube;

import java.util.ArrayList;
import java.util.List;

public class DataBlockType {
    private int[] dimensionSizeList;
    public List<CellType> cellTypeList;

    private int[] mulResult;

    public int dataLength;

    public Parameter[] paramList;

    public DataBlockType(int... dimensionSizeList){
        this.dimensionSizeList = dimensionSizeList;
        this.mulResult = new int[dimensionSizeList.length];
        int mul = 1;
        for (int idx = 0; idx < dimensionSizeList.length; idx++) {
            this.mulResult[idx] = mul;
            mul *= this.dimensionSizeList[idx];
        }
        this.dataLength = mul;
        this.cellTypeList = new ArrayList<>();
    }

    public void addCellType(String typeName, int id, Parameter... params){
        List<Parameter> usualParamList = new ArrayList<>();
        List<Parameter> specificParamList = new ArrayList<>();
        for (Parameter param : params){
            param.cellTypeId = id;
            if (param instanceof  BitMaskType){
                specificParamList.add(param);
            } else {
                usualParamList.add(param);
            }
        }
        List<Parameter> resultList = new ArrayList<>();
        int usualCount = usualParamList.size();
        resultList.addAll(usualParamList);
        resultList.addAll(specificParamList);
        int counter = 0;
        for (int index = 0; index < resultList.size(); index++) {
            Parameter currentParam = resultList.get(index);
            currentParam.innerIndex = counter;
            if (currentParam instanceof BitMaskType){
                counter += ((BitMaskType) ((BitMaskType) currentParam)).length;
            } else {
                counter++;
            }
        }
        CellType cellType = new CellType(id, typeName, resultList);
        cellType.setBorder(usualCount);
        this.cellTypeList.add(cellType);
    }

    public DataBlock createDataBlock(){
        return new DataBlock(this.getTotalParameterSize(), this.dataLength);
    }

    public void setValue(DataBlock db, float value, String cellTypeName, String paramName, int index) throws Exception {
        index = db.getRealInnerIndex(index);
        CellType cellType = this.findCellType(cellTypeName);
        Parameter param = cellType.findParam(paramName);
        param.setValue(db, index, value);
    }

    public void setValue(DataBlock db, float value, String name, String paramName, int... indices) throws Exception{
        int index = db.getRealInnerIndex(this.computeIndex(indices));
        CellType cellType = this.findCellType(name);
        Parameter param = cellType.findParam(paramName);
        param.setValue(db, index, value);
    }

    public float getValue(DataBlock db, String paramName, int cellIndex) throws Exception {
        int realIndex = db.getRealInnerIndex(cellIndex);
        int cellTypeId = (int) db.getCellTypeId(realIndex);
        Parameter param = this.cellTypeList.get(cellTypeId).findParam(paramName);
        return param.getValue(db, realIndex);
    }

    public float getValue(DataBlock db, String paramName, int... indices) throws Exception {
        int cellIndex = this.computeIndex(indices);
        int realIndex = db.getRealInnerIndex(cellIndex);
        int cellTypeId = (int) db.getCellTypeId(realIndex);
        Parameter param = this.cellTypeList.get(cellTypeId).findParam(paramName);
        return param.getValue(db, realIndex);
    }

    public int computeIndex(int... indices) throws Exception{
        int index = 0;
        for (int idx = 0; idx < indices.length; idx++) {
            if (dimensionSizeList[idx] < indices[idx]){
                throw new Exception("Индекс по измерению " + idx + " (" + (indices[idx] + 1) + ") больше количества элементов (" + dimensionSizeList[idx] + ")");
            }
            index += indices[idx] * this.mulResult[idx];
        }
        return index;
    }

    public int[] decomposyteIndex(int index){
        int[] resultArr = new int[this.mulResult.length];
        for (int idx = this.mulResult.length - 1; idx > -1; idx--) {
            resultArr[idx] = index / this.mulResult[idx];
            index %= this.mulResult[idx];
        }
        return resultArr;
    }

    public Parameter par(String name) {
        return new Parameter(name, -3);
    }
    public Parameter par(String name, int maskSize){
        int floatCount = maskSize / 8 + ((maskSize % 8 != 0) ? 1 : 0);
        return new BitMaskType(name, -3, floatCount);
    }

    public void addCell(DataBlock db, int whereTo, int whereFrom) throws Exception{
        whereFrom = db.getRealInnerIndex(whereFrom);
        CellType cellTypeWhereFrom = this.cellTypeList.get(db.getCellTypeId(whereFrom));
        List<Parameter> params = cellTypeWhereFrom.paramList;
        for (Parameter param : params) {
            float value1 = param.getValue(db, whereFrom);
            float value2 = param.getValue(db, whereTo);
            this.setValue(db, value1 + value2, cellTypeWhereFrom.typeName, param.name, whereTo);
        }
    }

    public void initializeDataBlock(DataBlock db){
        for (CellType ct : this.cellTypeList){
            for (Parameter param : ct.paramList){
                param.setSpace(this, db);
            }
        }
    }

    public boolean isEmpty(DataBlock db, int cellIndex){
        int cellTypeId = db.getCellTypeId(cellIndex);
        if (cellTypeId == -1 ) return true;
        CellType cellType = this.cellTypeList.get(cellTypeId);
        for (Parameter par : cellType.paramList){
            if (par.hasThisParameterValue(db, cellIndex)){
                return false;
            }
        }
        return true;
    }

    public void commit(DataBlock db){
        for (CellType ct : this.cellTypeList){
            ct.currentSize = -1;
        }

        int totalSize = db.getTotalSize();
        for (int innerIdx = 0; innerIdx < totalSize; innerIdx++) {
            if (!this.isEmpty(db, innerIdx)){
                CellType cellType = this.cellTypeList.get(db.getCellTypeId(innerIdx));
                cellType.currentSize += 1;
                int currentSize = cellType.currentSize;
                for (Parameter param : cellType.paramList){
                    param.setValue(db, currentSize, param.getValue(db, innerIdx));
                    if (innerIdx != currentSize){
                        param.setZeroValue(db, innerIdx);
                    }
                }
                db.setValue(innerIdx, -1, currentSize);
            }
        }
        for (CellType cellType : this.cellTypeList){
            for (Parameter param : cellType.paramList){
                param.reduceArraySize(db, cellType.currentSize + 1);
            }
        }
    }

    public void setParamInnerIndices(){
        int idx = 0;
        for (CellType cellType : this.cellTypeList){
            for (Parameter param : cellType.paramList){
                param.index = idx;
                idx += 1;
            }
        }
    }


    public int getTotalParameterSize(){
        int result = 0;
        for (CellType cellType : this.cellTypeList){
            result += cellType.paramList.size();
        }
        return result;
    }

    public CellType findCellType(String name){
        for (CellType cellType : this.cellTypeList) {
            if (cellType.typeName.equals(name)){
                return cellType;
            }
        }
        return null;
    }

    public int getTotalParamCount(){
        int result = 0;
        for (CellType ct : this.cellTypeList){
            result += ct.paramList.size();
        }
        return result;
    }


    public void fillParamList() {
        this.paramList = new Parameter[this.getTotalParamCount()];
        int idx = 0;
        for (CellType ct : this.cellTypeList){
            for (Parameter par : ct.paramList){
                this.paramList[idx] = par;
                idx++;
            }
        }
    }
}
