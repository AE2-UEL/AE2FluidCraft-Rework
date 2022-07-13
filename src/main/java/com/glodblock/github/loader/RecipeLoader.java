package com.glodblock.github.loader;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemBasicFluidStorageCell;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.util.ModAndClassUtil;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static com.glodblock.github.loader.ItemAndBlockHolder.*;

public class RecipeLoader implements Runnable {

    public final static ItemStack AE2_INTERFACE = GameRegistry.findItemStack("appliedenergistics2", "tile.BlockInterface", 1);
    public final static ItemStack AE2_PROCESS_ENG = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 24);
    public final static ItemStack AE2_STORAGE_BUS = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"), 1, 220);
    public final static ItemStack AE2_CONDENSER = GameRegistry.findItemStack("appliedenergistics2", "tile.BlockCondenser", 1);
    public final static ItemStack AE2_GLASS_CABLE = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"), 1, 16);
    public final static ItemStack AE2_PROCESS_CAL = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 23);
    public final static ItemStack AE2_WORK_BENCH = GameRegistry.findItemStack("appliedenergistics2", "tile.BlockCellWorkbench", 1);
    public final static ItemStack AE2_PATTERN_TERM = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"), 1, 340);
    public final static ItemStack AE2_PROCESS_LOG = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 22);
    public final static ItemStack AE2_PURE_CERTUS = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 10);
    public final static ItemStack AE2_QUARTZ_GLASS = GameRegistry.findItemStack("appliedenergistics2", "tile.BlockQuartzGlass", 1);
    public final static ItemStack AE2_LAMP_GLASS = GameRegistry.findItemStack("appliedenergistics2", "tile.BlockQuartzLamp", 1);
    public final static ItemStack AE2_CELL_HOUSING = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 39);
    public final static ItemStack AE2_CELL_1K = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 35);
    public final static ItemStack AE2_CORE_ANN = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 44);
    public final static ItemStack AE2_CORE_FOM = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 43);
    public final static ItemStack PISTON = new ItemStack(Blocks.piston, 1);
    public final static ItemStack AE2_BLANK_PATTERN = new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 52);
    public final static ItemStack BUCKET = new ItemStack(Items.bucket, 1);
    public final static ItemStack IRON_BAR = new ItemStack(Blocks.iron_bars, 1);

    @Override
    public void run() {
        GameRegistry.addRecipe(new ShapedOreRecipe(INTERFACE.stack(), "IPI", "GEG", "IPI", 'I', "ingotIron", 'P', "dyeBlue", 'G', "blockGlass", 'E', AE2_INTERFACE));
        GameRegistry.addShapelessRecipe(FLUID_INTERFACE.stack(), INTERFACE.stack());
        GameRegistry.addShapelessRecipe(INTERFACE.stack(), FLUID_INTERFACE.stack());
        GameRegistry.addRecipe(new ShapedOreRecipe(DISCRETIZER.stack(), "IPI", "TMT", "IPI", 'I', "ingotIron", 'P', AE2_PROCESS_ENG, 'T', AE2_STORAGE_BUS, 'M', AE2_CONDENSER));
        GameRegistry.addRecipe(new ShapedOreRecipe(DECODER.stack(), "IHI", "CFC", "IPI", 'I', "ingotIron", 'H', Blocks.hopper, 'C', AE2_GLASS_CABLE, 'F', INTERFACE, 'P', AE2_PROCESS_CAL));
        GameRegistry.addRecipe(new ShapedOreRecipe(ENCODER.stack(), "LPL", "IWI", "III", 'I', "ingotIron", 'L', "blockLapis", 'P', AE2_PROCESS_ENG, 'W', AE2_WORK_BENCH));
        GameRegistry.addShapelessRecipe(FLUID_TERMINAL.stack(), AE2_PATTERN_TERM, ENCODER);
        GameRegistry.addShapelessRecipe(FLUID_TERMINAL_EX.stack(), FLUID_TERMINAL.stack(), AE2_PROCESS_CAL, AE2_PROCESS_ENG, AE2_PROCESS_LOG);
        GameRegistry.addRecipe(new ShapedOreRecipe(BUFFER.stack(), "ILI", "AGF", "IBI", 'I', "ingotIron", 'G', AE2_QUARTZ_GLASS, 'L', AE2_CELL_1K, 'A', AE2_CORE_ANN, 'F', AE2_CORE_FOM, 'B', BUCKET));
        GameRegistry.addRecipe(new ShapedOreRecipe(LARGE_BUFFER.stack(), "BGB", "GEG", "BGB", 'B', BUFFER.stack(), 'G', AE2_QUARTZ_GLASS, 'E', AE2_PROCESS_ENG));

        if (Config.fluidCells) {
            OreDictionary.registerOre("anyCertusCrystal", AE2_PURE_CERTUS);
            for (ItemStack it : OreDictionary.getOres("crystalCertusQuartz"))
                OreDictionary.registerOre("anyCertusCrystal", it);

            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell1kPart.stack(1), "DCD", "CEC", "DCD", 'D', "dyeBlue", 'C', "anyCertusCrystal", 'E', AE2_PROCESS_ENG));
            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell4kPart.stack(1), "DPD", "CGC", "DCD", 'D', "dyeBlue", 'C', CellType.Cell1kPart.stack(1), 'P', AE2_PROCESS_CAL, 'G', AE2_QUARTZ_GLASS));
            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell16kPart.stack(1), "DPD", "CGC", "DCD", 'D', "dyeBlue", 'C', CellType.Cell4kPart.stack(1), 'P', AE2_PROCESS_LOG, 'G', AE2_QUARTZ_GLASS));
            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell64kPart.stack(1), "DPD", "CGC", "DCD", 'D', "dyeBlue", 'C', CellType.Cell16kPart.stack(1), 'P', AE2_PROCESS_ENG, 'G', AE2_QUARTZ_GLASS));
            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell256kPart.stack(1), "DPD", "CGC", "DCD", 'D', "dyeBlue", 'C', CellType.Cell64kPart.stack(1), 'P', AE2_PROCESS_CAL, 'G', AE2_LAMP_GLASS));
            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell1024kPart.stack(1), "DPD", "CGC", "DCD", 'D', "dyeBlue", 'C', CellType.Cell256kPart.stack(1), 'P', AE2_PROCESS_LOG, 'G', AE2_LAMP_GLASS));
            GameRegistry.addRecipe(new ShapedOreRecipe(CellType.Cell4096kPart.stack(1), "DPD", "CGC", "DCD", 'D', "dyeBlue", 'C', CellType.Cell1024kPart.stack(1), 'P', AE2_PROCESS_ENG, 'G', AE2_LAMP_GLASS));

            ItemBasicFluidStorageCell[] cells = new ItemBasicFluidStorageCell[]{
                CELL1K, CELL4K, CELL16K, CELL64K, CELL256K, CELL1024K, CELL4096K
            };

            for (ItemBasicFluidStorageCell cell : cells) {
                GameRegistry.addRecipe(new ShapedOreRecipe(cell, "GDG", "DCD", "III", 'D', "dustRedstone", 'G', AE2_QUARTZ_GLASS, 'C', cell.getComponent(), 'I', "ingotIron"));
                GameRegistry.addRecipe(new ShapelessOreRecipe(cell, AE2_CELL_HOUSING, cell.getComponent()));
            }
        }

        if (Config.fluidIOBus) {
            GameRegistry.addRecipe(new ShapedOreRecipe(FLUID_EXPORT_BUS, "ICI", "BPB", 'B', "dyeBlue", 'I', "ingotIron", 'P', PISTON, 'C', AE2_CORE_FOM));
            GameRegistry.addRecipe(new ShapedOreRecipe(FLUID_IMPORT_BUS, "BCB", "IPI", 'B', "dyeBlue", 'I', "ingotIron", 'P', PISTON, 'C', AE2_CORE_ANN));
        }

        if (ModAndClassUtil.OC) {
            ItemStack CHIP_T1 = new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24);
            GameRegistry.addRecipe(new ShapedOreRecipe(OC_EDITOR, "IMI", "CBC", "IPI", 'I', IRON_BAR, 'M', CHIP_T1, 'C', "oc:cable", 'B', BUCKET, 'P', AE2_BLANK_PATTERN));
        }

    }
}
