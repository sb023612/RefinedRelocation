package com.dynious.blex.network;

import com.dynious.blex.BlockExtenders;
import com.dynious.blex.gui.*;
import com.dynious.blex.gui.container.ContainerAdvanced;
import com.dynious.blex.gui.container.ContainerAdvancedFiltered;
import com.dynious.blex.gui.container.ContainerFiltered;
import com.dynious.blex.gui.container.ContainerFilteringChest;
import com.dynious.blex.lib.GuiIds;
import com.dynious.blex.gui.GuiFilteringIronChest;
import com.dynious.blex.temp.OldTileIronFilteringChest;
import com.dynious.blex.tileentity.*;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.ironchest.IronChestType;
import cpw.mods.ironchest.client.GUIChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{
    public GuiHandler()
    {
        NetworkRegistry.instance().registerGuiHandler(BlockExtenders.instance, this);
    }

    @Override
    public Object getServerGuiElement(int GuiId, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);

        switch (GuiId)
        {
            //case GuiIds.BLOCK_EXTENDER:
            //case GuiIds.BUFFER:
            case GuiIds.FILTERED:
                return new ContainerFiltered((IFilterTile) tile);
            case GuiIds.ADVANCED_BLOCK_EXTENDER:
            case GuiIds.ADVANCED_BUFFER:
                return new ContainerAdvanced((IAdvancedTile) tile);
            case GuiIds.ADVANCED_FILTERED_BLOCK_EXTENDER:
                return new ContainerAdvancedFiltered((IAdvancedFilteredTile) tile);
            case GuiIds.FILTERING_CHEST:
                if (Loader.isModLoaded("IronChest") && tile != null && tile instanceof TileFilteringIronChest)
                {
                    return GuiFilteringIronChest.makeContainer(GUIChest.GUI.values()[((TileFilteringIronChest)tile).getType().ordinal()], player, (TileFilteringIronChest) tile);
                }
                return new ContainerFilteringChest(player, (TileFilteringChest) tile);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int GuiId, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);

        switch (GuiId)
        {
            case GuiIds.ADVANCED_BLOCK_EXTENDER:
                if (tile != null && tile instanceof TileAdvancedBlockExtender)
                {
                    return new GuiAdvancedBlockExtender(player.inventory, (TileAdvancedBlockExtender) tile);
                }
                break;
            case GuiIds.FILTERED:
                if (tile != null && tile instanceof IFilterTile)
                {
                    return new GuiFiltered(player.inventory, (IFilterTile) tile);
                }
                break;
            case GuiIds.ADVANCED_FILTERED_BLOCK_EXTENDER:
                if (tile != null && tile instanceof TileAdvancedFilteredBlockExtender)
                {
                    return new GuiAdvancedFilteredBlockExtender(player.inventory, (TileAdvancedFilteredBlockExtender) tile);
                }
                break;
            case GuiIds.ADVANCED_BUFFER:
                if (tile != null && tile instanceof TileAdvancedBuffer)
                {
                    return new GuiAdvancedBuffer(player.inventory, (TileAdvancedBuffer) tile);
                }
                break;
            case GuiIds.FILTERING_CHEST:
                if (Loader.isModLoaded("IronChest") && tile != null && tile instanceof TileFilteringIronChest)
                {
                    return new GuiFilteringIronChest(GUIChest.GUI.values()[((TileFilteringIronChest)tile).getType().ordinal()], player, (TileFilteringIronChest) tile);
                }

                if (tile != null && tile instanceof TileFilteringChest)
                {
                    return new GuiFilteringChest(player, (TileFilteringChest) tile);
                }
                break;
        }

        return null;
    }

}
