import org.junit.Test;
import org.consul.cube.*;

public class CubeTest {


    @Test
    public void dataBlockReductionTest() throws Exception{
        Cube cube = new Cube();

        Dimension whsDim = new Dimension("Склады");
        whsDim.addNullElem("Все склады");
        whsDim.addChild(whsDim.findElement("Все склады").id, "Ташкент 1");
        whsDim.addChild(whsDim.findElement("Все склады").id, "Краснодар");
        whsDim.addChild(whsDim.findElement("Краснодар").id, "Краснодар 1");
        whsDim.addChild(whsDim.findElement("Краснодар").id, "Краснодар 2");
        whsDim.addChild(whsDim.findElement("Все склады").id, "Ростов");
        whsDim.addChild(whsDim.findElement("Ростов").id, "Ростов 1");

        cube.addDimension(whsDim);

        Dimension skuDim = new Dimension("Товары");
        skuDim.addNullElem("Все товары");
        skuDim.addChild(skuDim.findElement("Все товары").id, "Ювелирные изделия");
        skuDim.addChild(skuDim.findElement("Ювелирные изделия").id, "Кольца");
        skuDim.addChild(skuDim.findElement("Ювелирные изделия").id, "Ожерелье");
        skuDim.addChild(skuDim.findElement("Кольца").id, "Золотое кольцо");
        skuDim.addChild(skuDim.findElement("Кольца").id, "Платиновое кольцо");
        skuDim.addChild(skuDim.findElement("Все товары").id, "Бижутерия");
        skuDim.addChild(skuDim.findElement("Бижутерия").id, "Заколка");

        cube.addDimension(skuDim);

        Dimension dayDim = new Dimension("Дни");
        dayDim.addNullElem("Все дни");
        dayDim.addChild(dayDim.findElement("Все дни").id, "День 1");
        dayDim.addChild(dayDim.findElement("Все дни").id, "День 2");

        cube.addDimension(dayDim);

        DataBlockType dbt = new DataBlockType(cube.getDimensionSizeList());

        Parameter[] paramArr1 = new Parameter[]{dbt.par("Приход"), dbt.par("Расход"), dbt.par("Остаток")};

        DataBlock db1 = dbt.createDataBlock();
        dbt.addCellType("Ноль", 0, paramArr1);

        cube.setDbt(dbt);

        dbt.initializeDataBlock(db1);

        db1.setAllCellTypeId(0, 4);
    }
}
