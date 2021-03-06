package com.dynious.refinedrelocation.mods.waila;

import com.dynious.refinedrelocation.helper.StringHelper;
import com.dynious.refinedrelocation.lib.Strings;
import com.dynious.refinedrelocation.tileentity.TilePowerLimiter;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import java.util.List;

public class PowerLimiterHUDHandler implements IWailaDataProvider
{

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> strings, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof TilePowerLimiter)
        {
            NBTTagCompound compound = accessor.getNBTData();
            TilePowerLimiter tile = (TilePowerLimiter) accessor.getTileEntity();

            strings.add(StatCollector.translateToLocalFormatted(Strings.CONNECTED_DIRECTION, StringHelper.getLocalizedDirection(compound.getByte("side"))));
            strings.add(StatCollector.translateToLocalFormatted(Strings.WAILA_ENABLED, StringHelper.getLocalizedBoolean(!compound.getBoolean("disablePower"))));
            strings.add(StatCollector.translateToLocal(compound.getBoolean("toggle") ? Strings.RS_PULSE : Strings.RS_ON));
        }
        return strings;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }
}
