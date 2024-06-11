package org.consul.cube;

import java.text.SimpleDateFormat;
import java.util.*;

public class Cube {

    public List<Dimension> dimList = new ArrayList<>();

    DataBlockType dbt;

    public DataBlockType getDbt(){return dbt;}

    public void setDbt(DataBlockType dbt) {this.dbt = dbt;}

    public void addDimension(Dimension dim){
        dim.id = this.dimList.size();
        this.dimList.add(dim);
    }

    public Dimension findDimension(String name) {
        for (Dimension dim : this.dimList){
            if (dim.dimName.equals(name)){
                return dim;
            }
        }
        return null;
    }

    public int getDimensionCount(){
        return this.dimList.size();
    }

    public int[] getDimensionSizeList() {
        int dimCount = this.getDimensionCount();
        int[] result = new int[dimCount];
        for (Dimension dim : this.dimList){
            result[dim.id] = dim.getElemCount();
        }
        return result;
    }

    public int getTotalSize(){
        int[] localSizeList = this.getDimensionSizeList();
        int res = 1;
        for (int localSize : localSizeList){
            res *= localSize;
        }
        return res;
    }

    public int getTotalFactsCount(){
        int mul = 1;
        for (Dimension dim : this.dimList){
            mul *= dim.getLeaves().size();
        }
        return mul;
    }

    public ArrayList<Element> getElementsByIndices(int... indices){
        ArrayList<Element> result = new ArrayList<>();
        for (int i = 0; i < indices.length; i++) {
            result.add(this.dimList.get(i).elemList.get(indices[i]));
        }
        return result;
    }

    public boolean isFact(int index) {
        int[] indices = this.dbt.decomposyteIndex(index);
        for (int dimIndex = 0; dimIndex < this.dimList.size(); dimIndex++) {
            if (!this.dimList.get(dimIndex).elemList.get(indices[dimIndex]).childrenIdList.isEmpty()){
                return false;
            }
        }
        return true;
    }

    public Element[] getByIndices(int[] indices){
        int length = indices.length;
        Element[] result = new Element[length];
        for (int dimIndex = 0; dimIndex < length; dimIndex++) {
            result[dimIndex] = this.dimList.get(dimIndex).elemList.get(indices[dimIndex]);
        }
        return result;
    }

    public void getConnectedSubTotals(int currentCellIndex, ArrayList<Integer> currentResult) throws Exception {
        int[] indices = this.dbt.decomposyteIndex(currentCellIndex);
        if (!this.isFact(currentCellIndex)){
            currentResult.add(currentCellIndex);
        }
        Element[] currentPos = this.getByIndices(indices);

        for (int elemIndex = 0; elemIndex < currentPos.length; elemIndex++) {
            int toChange = indices[elemIndex];
            Element elem = currentPos[elemIndex];
            if (elem.parentId != -1) {
                indices[elemIndex] = elem.parentId;
                int newIndex = this.dbt.computeIndex(indices);
                this.getConnectedSubTotals(newIndex, currentResult);
                indices[elemIndex] = toChange;
            }
        }
    }

    public ArrayList<Element[]> getParentalSubTotals(int cellIndex){
        ArrayList<Element[]> parents = new ArrayList<>();
        Element[] currentPos = this.getByIndices( this.getDbt().decomposyteIndex(cellIndex));
        for (int idx = 0; idx < currentPos.length; idx++) {
            Element elemToChange = currentPos[idx];
            if (elemToChange.parentId != -1){
                currentPos[idx] = this.dimList.get(idx).elemList.get(elemToChange.parentId);
                Element[] copy = new Element[currentPos.length];
                for (int i = 0; i < currentPos.length; i++) {
                    copy[i] = currentPos[i];
                }
                parents.add(copy);
                currentPos[idx] = elemToChange;
            }
        }
        return parents;
    }

    public ArrayList<Element[]> getChildrenSubTotalsPerAxis(int cellIndex, int axisIndex){
        ArrayList<Element[]> result = new ArrayList<>();
        int[] dimCellIndex = this.getDbt().decomposyteIndex(cellIndex);
        List<Integer> children = this.dimList.get(axisIndex).elemList.get(dimCellIndex[axisIndex]).childrenIdList;
        for (Integer child : children){
            dimCellIndex[axisIndex] = child;
            result.add(this.getByIndices(dimCellIndex));
        }
        return result;
    }

    public int[] getIndexByElems(Element[] elems){
        int[] result = new int[elems.length];
        for (int idx = 0; idx < elems.length; idx++) {
            result[idx] = elems[idx].id;
        }
        return result;
    }

    public int getComputableAxis(DataBlock db, int cellIndex) throws Exception{
        for (int dimIndex = 0; dimIndex < this.dimList.size(); dimIndex++) {
            ArrayList<Element[]> children = this.getChildrenSubTotalsPerAxis(cellIndex, dimIndex);
            boolean flag = true;
            if (!children.isEmpty()){
                for (Element[] child : children){
                    if (this.getDbt().isEmpty(db, this.getDbt().computeIndex(this.getIndexByElems(child)))){
                        flag = false;
                    }
                }
                if (flag){
                    return dimIndex;
                }
            }
        }
        return -1;
    }

    public void computeCell(DataBlock db, int cellIndex) throws Exception {
        int computableDimIndex = this.getComputableAxis(db, cellIndex);
        if (computableDimIndex != -1){
            ArrayList<Element[]> children = this.getChildrenSubTotalsPerAxis(cellIndex, computableDimIndex);
            for (Element[] child : children){
                int childIndex = this.getDbt().computeIndex(this.getIndexByElems(child));
                this.getDbt().addCell(db, cellIndex, childIndex);
            }
        }
    }

    public int computeCellLevel(int cellIndex){
        int result = 0;
        Element[] coordinates = this.getByIndices(this.getDbt().decomposyteIndex(cellIndex));
        for (Element elem : coordinates){
            result += elem.layer;
        }
        return result;
    }

    public Element[] getElementsByNames(String[] names){
        Element[] result = new Element[names.length];
        for (int idx = 0; idx < names.length; idx++) {
            result[idx] = this.dimList.get(idx).findElement(names[idx]);
        }
        return result;
    }

    public void settleDimensions(int dayCount) throws Exception{
        Dimension whsDim = new Dimension("Склады");
        whsDim.addNullElem("Все склады");
        whsDim.addChild(whsDim.findElement("Все склады").id, "Калининград");

        whsDim.addChild(whsDim.findElement("Все склады").id, "ЦФО");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Москва");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Белгород");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Старый оскол");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Брянск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Владимир");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Воронеж");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Иваново");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Калуга");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Обнинск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Кострома");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Курск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Железногорск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Липецк");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Елец");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Балашиха");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Подольск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Химки");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Мытищи");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Королёв");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Люберцы");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Красногорск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Электросталь");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Коломна");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Орёл");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Рязань");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Смоленск");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Тамбов");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Тверь");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Тула");
        whsDim.addChild(whsDim.findElement("ЦФО").id, "Ярославль");

        whsDim.addChild(whsDim.findElement("Все склады").id, "СЗФО");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Архангельск");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Северодвинск");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Вологда");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Череповец");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Петрозаводск");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Сыктывкар");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Мурманск");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Великий Новгород");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Псков");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Великие Луки");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Санкт-Петербург");
        whsDim.addChild(whsDim.findElement("СЗФО").id, "Колпино");

        whsDim.addChild(whsDim.findElement("Все склады").id, "ПФО");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Уфа");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Стерлитамак");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Нефтекамск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Октябрьский");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Салават");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Киров");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Йошкар-Ола");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Саранск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Нижний Новгород");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Арзамас");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Дзержинск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Оренбург");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Орск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Пенза");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Пермь");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Березники");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Самара");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Тольятти");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Сызрань");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Новокуйбышевск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Саратов");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Балаково");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Энгельс");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Казань");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Набережные Челны");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Алметьевск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Нижнекамск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Ижевск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Воткинск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Сарапул");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Ульяновск");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Чебоксары");
        whsDim.addChild(whsDim.findElement("ПФО").id, "Новочечебоксарск");

        whsDim.addChild(whsDim.findElement("Все склады").id, "ЮФО");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Ростов-на-Дону");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Краснодар");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Севастополь");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Астрахань");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Сочи");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Симферополь");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Волжский");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Новороссийск");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Таганрог");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Шахты");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Армавир");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Волгодонск");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Новочеркасск");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Керчь");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Майкоп");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Батайск");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Камышин");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Евпатория");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Новошахтинск");
        whsDim.addChild(whsDim.findElement("ЮФО").id, "Элиста");


        this.addDimension(whsDim);


        Dimension skuDim = new Dimension("Товары");
        skuDim.addNullElem("Все товары");
        skuDim.addChild(skuDim.findElement("Все товары").id, "Спички");

        skuDim.addChild(skuDim.findElement("Все товары").id, "Бакалея");
        skuDim.addChild(skuDim.findElement("Бакалея").id, "Вермишель");
        skuDim.addChild(skuDim.findElement("Бакалея").id, "Рис длиннозерный");
        skuDim.addChild(skuDim.findElement("Бакалея").id, "Овсяная каша");

        skuDim.addChild(skuDim.findElement("Все товары").id, "Молочная продукция");
        skuDim.addChild(skuDim.findElement("Молочная продукция").id, "Молоко");
        skuDim.addChild(skuDim.findElement("Молочная продукция").id, "Кефир");
        skuDim.addChild(skuDim.findElement("Молочная продукция").id, "Простокваша");
        skuDim.addChild(skuDim.findElement("Молочная продукция").id, "Сливки");

        this.addDimension(skuDim);

        String str = "2023-12-21";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date today = format.parse(str);
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);

        Dimension dayDim = new Dimension("Дни");
        dayDim.addNullElem("Все дни");
        cal.add(Calendar.DATE, -1*dayCount);

        for (int dayIdx = 0; dayIdx < dayCount; dayIdx++) {
            cal.add(Calendar.DATE, 1);
            String dayName = format.format(cal.getTime());
            dayDim.addChild(dayDim.findElement("Все дни").id, dayName);
        }

        this.addDimension(dayDim);

    }

}
