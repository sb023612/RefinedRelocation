package com.dynious.blex.item;

import com.dynious.blex.BlockExtenders;
import com.dynious.blex.block.ModBlocks;
import com.dynious.blex.lib.Names;
import com.dynious.blex.lib.Resources;
import com.dynious.blex.tileentity.TileFilteringChest;
import com.dynious.blex.tileentity.TileFilteringIronChest;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.ironchest.IronChest;
import cpw.mods.ironchest.TileEntityIronChest;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

public class ItemFilteringUpgrade extends Item
{
    public ItemFilteringUpgrade(int par1)
    {
        super(par1);
        setMaxStackSize(1);
        setUnlocalizedName(Names.filteringUpgrade);
        setCreativeTab(BlockExtenders.tabBlEx);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int X, int Y, int Z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote) return false;
        TileEntity te = world.getBlockTileEntity(X, Y, Z);
        ItemStack[] chestContents = null;
        if (te != null)
        {
            if (te instanceof TileEntityChest)
            {
                TileEntityChest tec = (TileEntityChest) te;
                if (tec.numUsingPlayers > 0)
                {
                    return false;
                }
                // Force old TE out of the world so that adjacent chests can update
                TileFilteringChest newchest = new TileFilteringChest();
                ItemStack[] chestInventory = ObfuscationReflectionHelper.getPrivateValue(TileEntityChest.class, tec, 0);
                chestContents = chestInventory.clone();
                newchest.setFacing((byte) tec.getBlockMetadata());
                for (int i = 0; i < chestInventory.length; i++)
                {
                    chestInventory[i] = null;
                }
                // Clear the old block out
                world.setBlock(X, Y, Z, 0, 0, 3);
                // Force the Chest TE to reset it's knowledge of neighbouring blocks
                tec.updateContainingBlockInfo();
                // Force the Chest TE to update any neighbours so they update next
                // tick
                tec.checkForAdjacentChests();
                // And put in our block instead
                world.setBlock(X, Y, Z, ModBlocks.filteringChest.blockID, 0, 3);

                world.setBlockTileEntity(X, Y, Z, newchest);
                world.setBlockMetadataWithNotify(X, Y, Z, 0, 3);
                System.arraycopy(chestContents, 0, newchest.inventory, 0, newchest.getSizeInventory());
            }
            else if (Loader.isModLoaded("IronChest") && te instanceof TileEntityIronChest)
            {
                TileEntityIronChest teic = (TileEntityIronChest) te;
                int numUsers = ObfuscationReflectionHelper.getPrivateValue(TileEntityIronChest.class, teic, "numUsingPlayers");
                if (numUsers > 0)
                {
                    return false;
                }
                TileFilteringIronChest chest = new TileFilteringIronChest(teic.getType());
                chestContents = teic.chestContents.clone();
                chest.setFacing(teic.getFacing());
                for (int i = 0; i < teic.chestContents.length; i++)
                {
                    teic.chestContents[i] = null;
                }
                // Clear the old block out
                world.setBlock(X, Y, Z, 0, 0, 3);
                // And put in our block instead
                world.setBlock(X, Y, Z, ModBlocks.filteringIronChest.blockID, teic.getType().ordinal(), 3);

                world.setBlockTileEntity(X, Y, Z, chest);
                world.setBlockMetadataWithNotify(X, Y, Z, chest.getType().ordinal(), 3);
                System.arraycopy(chestContents, 0, chest.chestContents, 0, chest.getSizeInventory());
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
        stack.stackSize--;
        return true;
    }


    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        itemIcon = par1IconRegister.registerIcon(Resources.MOD_ID + ":"
                + Names.filteringUpgrade);
    }
}
