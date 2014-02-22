package com.dynious.blex.block;

import com.dynious.blex.item.ItemBlockExtender;
import com.dynious.blex.item.ItemBuffer;
import com.dynious.blex.item.ItemFilteringIronChest;
import com.dynious.blex.lib.BlockIds;
import com.dynious.blex.lib.Names;
import com.dynious.blex.lib.Settings;
import com.dynious.blex.mods.IronChestHelper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ModBlocks
{
    public static BlockExtender blockExtender;
    public static BlockBuffer buffer;
    public static BlockFilteringChest filteringChest;
    public static BlockFilteringIronChest filteringIronChest;

    public static void init()
    {
        blockExtender = new BlockExtender(BlockIds.BLOCK_EXTENDER);
        buffer = new BlockBuffer(BlockIds.BUFFER);
        filteringChest = new BlockFilteringChest(BlockIds.FILTERING_CHEST);

        GameRegistry.registerBlock(blockExtender, ItemBlockExtender.class, Names.blockExtender);
        GameRegistry.registerBlock(buffer, ItemBuffer.class, Names.buffer);
        GameRegistry.registerBlock(filteringChest, Names.filteringChest);

        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 4, 0), "igi", "geg", "ioi", 'i', Item.ingotIron, 'o', Block.obsidian, 'g', Block.thinGlass, 'e', Item.enderPearl);
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 1), "r r", " b ", "r r", 'r', Block.blockRedstone, 'b', new ItemStack(blockExtender, 1, 0));
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 2), "g g", " b ", "g g", 'g', Item.ingotGold, 'b', new ItemStack(blockExtender, 1, 0));
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 3), "g g", " b ", "g g", 'g', Item.ingotGold, 'b', new ItemStack(blockExtender, 1, 1));
        GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 3), "r r", " b ", "r r", 'r', Block.blockRedstone, 'b', new ItemStack(blockExtender, 1, 2));

        GameRegistry.addShapedRecipe(new ItemStack(buffer, 4, 0), "igi", "geg", "igi", 'i', Item.ingotIron, 'g', Block.thinGlass, 'e', Item.enderPearl);
        GameRegistry.addShapedRecipe(new ItemStack(buffer, 1, 1), "r r", " b ", "r r", 'r', Block.blockRedstone, 'b', new ItemStack(buffer, 1, 0));
        GameRegistry.addShapedRecipe(new ItemStack(buffer, 1, 2), "g g", " b ", "g g", 'g', Item.ingotGold, 'b', new ItemStack(buffer, 1, 0));

        if (!Settings.DISABLE_WIRELESS_BLOCK_EXTENDER)
        {
            GameRegistry.addShapedRecipe(new ItemStack(blockExtender, 1, 4), "d d", " b ", "d d", 'd', Item.diamond, 'b', new ItemStack(blockExtender, 1, 3));
        }

        GameRegistry.addShapedRecipe(new ItemStack(filteringChest, 1, 0), "g g", " b ", "g g", 'g', Item.ingotGold, 'b', new ItemStack(Block.chest));

        if (Loader.isModLoaded("IronChest"))
        {
            filteringIronChest = new BlockFilteringIronChest(BlockIds.FILTERING_IRON_CHEST);
            GameRegistry.registerBlock(filteringIronChest, ItemFilteringIronChest.class, Names.filteringIronChest);
            IronChestHelper.addIronChestRecipes();
        }
    }
}
