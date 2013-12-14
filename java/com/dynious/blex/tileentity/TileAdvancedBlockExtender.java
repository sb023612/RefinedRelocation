package com.dynious.blex.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class TileAdvancedBlockExtender extends TileBlockExtender
{
    public boolean spreadItems = false;
    private byte[] insertDirection;
    private int bestSlot;
    private boolean shouldUpdateBestSlot = true;
    private int lastSlotSide;

    public TileAdvancedBlockExtender()
    {
        insertDirection = new byte[ForgeDirection.values().length];
        for (byte i = 0; i < insertDirection.length; i++)
        {
            insertDirection[i] = i;
        }
    }

    public byte[] getInsertDirection()
    {
        return insertDirection;
    }

    public void setInsertDirection(int from, int value)
    {
        if (value > 5) value = 0;
        insertDirection[from] = (byte)value;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemStack, int i2)
    {
        if (spreadItems)
        {
            if (lastSlotSide != i || shouldUpdateBestSlot)
            {
                updateBestSlot(i);
                shouldUpdateBestSlot = false;
            }
            if (!super.canInsertItem(bestSlot, itemStack, i2) || i != bestSlot)
            {
                return false;
            }
            shouldUpdateBestSlot = true;
            return true;
        }
        else
        {
            return super.canInsertItem(i, itemStack, i2);
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack)
    {
        if (spreadItems)
        {
            if (lastSlotSide != i || shouldUpdateBestSlot)
            {
                updateBestSlot(i);
                shouldUpdateBestSlot = false;
            }
            if (!super.isItemValidForSlot(bestSlot, itemStack) || i != bestSlot)
            {
                return false;
            }
            shouldUpdateBestSlot = true;
            return true;
        }
        else
        {
            return super.isItemValidForSlot(i, itemStack);
        }
    }

    private void updateBestSlot(int side)
    {
        int bestSize = Integer.MAX_VALUE;
        for (int slot = 0; slot < getSizeInventory(); slot++)
        {
            ItemStack stack = getStackInSlot(slot);
            if (stack == null)
            {
                bestSlot = slot;
                break;
            }
            if (stack.stackSize < bestSize)
            {
                bestSlot = slot;
                bestSize = stack.stackSize;
            }
        }
        lastSlotSide = side;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int i)
    {
        if (inventory != null)
        {
            if (inventory instanceof ISidedInventory)
            {
                return ((ISidedInventory)inventory).getAccessibleSlotsFromSide(insertDirection[i]);
            }
            return accessibleSlots;
        }
        return new int[0];
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        spreadItems = compound.getBoolean("spreadItems");
        insertDirection = compound.getByteArray("insertDirection");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setBoolean("spreadItems", spreadItems);
        compound.setByteArray("insertDirection", insertDirection);
    }
}
