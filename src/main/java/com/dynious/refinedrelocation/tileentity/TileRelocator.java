package com.dynious.refinedrelocation.tileentity;

import com.dynious.refinedrelocation.api.filter.IRelocatorModule;
import com.dynious.refinedrelocation.api.item.IItemRelocatorModule;
import com.dynious.refinedrelocation.api.tileentity.IRelocator;
import com.dynious.refinedrelocation.grid.relocator.RelocatorFilterRegistry;
import com.dynious.refinedrelocation.grid.relocator.RelocatorGridLogic;
import com.dynious.refinedrelocation.grid.relocator.TravellingItem;
import com.dynious.refinedrelocation.helper.BlockHelper;
import com.dynious.refinedrelocation.helper.DirectionHelper;
import com.dynious.refinedrelocation.helper.IOHelper;
import com.dynious.refinedrelocation.helper.LogHelper;
import com.dynious.refinedrelocation.lib.Settings;
import com.dynious.refinedrelocation.network.PacketTypeHandler;
import com.dynious.refinedrelocation.network.packet.PacketItemList;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileRelocator extends TileEntity implements IRelocator, ISidedInventory
{
    public boolean blocksChanged = true;

    private TileEntity[] inventories = new TileEntity[ForgeDirection.VALID_DIRECTIONS.length];
    private IRelocator[] relocators = new IRelocator[ForgeDirection.VALID_DIRECTIONS.length];
    private IRelocatorModule[] filters = new IRelocatorModule[ForgeDirection.VALID_DIRECTIONS.length];
    private TravellingItem[] stuffedItems = new TravellingItem[ForgeDirection.VALID_DIRECTIONS.length];

    private boolean[] isConnected = new boolean[6];

    private TravellingItem cachedTravellingItem;
    private int maxStackSize = 64;

    private List<TravellingItem> items = new ArrayList<TravellingItem>();
    private List<TravellingItem> itemsToAdd = new ArrayList<TravellingItem>();

    private byte ticker = 0;

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (blocksChanged)
        {
            inventories = new TileEntity[ForgeDirection.VALID_DIRECTIONS.length];
            relocators = new IRelocator[ForgeDirection.VALID_DIRECTIONS.length];

            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tile = DirectionHelper.getTileAtSide(this, direction);
                if (tile != null)
                {
                    if (!(tile instanceof IRelocator))
                    {
                        if (IOHelper.canInterfaceWith(tile, direction.getOpposite()))
                            inventories[direction.ordinal()] = tile;
                    }
                    else
                    {
                        relocators[direction.ordinal()] = (IRelocator) tile;
                    }
                }
            }
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            blocksChanged = false;
        }

        for (int i = 0; i < filters.length; i++)
        {
            if (filters[i] != null)
            {
                filters[i].onUpdate(this, i);
            }
        }

        if (worldObj.isRemote)
        {
            clientSideUpdate();
        }
        else
        {
            serverSideUpdate();
        }
    }

    private void serverSideUpdate()
    {
        if (!itemsToAdd.isEmpty())
        {
            items.addAll(itemsToAdd);
            //TODO: More efficient client syncing
            PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 64, worldObj.provider.dimensionId, PacketTypeHandler.populatePacket(new PacketItemList(this, itemsToAdd)));
            itemsToAdd.clear();
        }

        for (Iterator<TravellingItem> iterator = items.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            item.counter++;
            if (item.counter > TravellingItem.timePerRelocator)
            {
                iterator.remove();
                outputToSide(item, item.onOutput());
            }
        }

        ticker++;
        if (ticker >= Settings.RELOCATOR_MIN_TICKS_PER_EXTRACTION)
        {
            ticker = 0;
            for (byte i = 0; i < stuffedItems.length; i++)
            {
                TravellingItem travellingItem = stuffedItems[i];
                if (travellingItem != null)
                {
                    stuffedItems[i] = null;
                    outputToSide(travellingItem, i);
                }
            }
        }

        cachedTravellingItem = null;
        maxStackSize = 64;
    }

    private void clientSideUpdate()
    {
        for (Iterator<TravellingItem> iterator = items.iterator(); iterator.hasNext(); )
        {
            TravellingItem item = iterator.next();
            item.counter++;
            if (item.counter > TravellingItem.timePerRelocator)
            {
                iterator.remove();
            }
        }
    }

    public boolean onActivated(EntityPlayer player, MovingObjectPosition hit, ItemStack stack)
    {
        if (hit.subHit < 6)
        {
            return sideHit(player, hit.subHit, stack);
        }
        else
        {
            //Middle was hit
        }
        return false;
    }

    public boolean sideHit(EntityPlayer player, int side, ItemStack stack)
    {
        if (stack != null && stack.getItem() instanceof IItemRelocatorModule && filters[side] == null)
        {
            IRelocatorModule filter = ((IItemRelocatorModule) stack.getItem()).getRelocatorFilter(stack);
            if (filter != null)
            {
                filters[side] = filter;
                stack.stackSize--;
                return true;
            }
        }
        else if (filters[side] != null)
        {
            if (player.isSneaking())
            {
                List<ItemStack> list = filters[side].getDrops(this, side);
                if (list != null)
                {
                    for (ItemStack stack1 : list)
                    {
                        IOHelper.spawnItemInWorld(worldObj, stack1, xCoord, yCoord, zCoord);
                    }
                }
                filters[side] = null;
                return true;
            }
            else
            {
                return filters[side].onActivated(this, player, side, stack);
            }
        }
        return false;
    }

    public GuiScreen getGUI(int side)
    {
        return filters[side].getGUI(this);
    }

    public Container getContainer(int side)
    {
        return filters[side].getContainer(this);
    }

    @Override
    public IRelocator[] getConnectedRelocators()
    {
        return relocators;
    }

    @Override
    public boolean passesFilter(ItemStack itemStack, int side, boolean input)
    {
        return stuffedItems[side] == null && (filters[side] == null || filters[side].passesFilter(itemStack, input));
    }

    @Override
    public TileEntity[] getConnectedInventories()
    {
        return inventories;
    }

    public List<TravellingItem> getItems(boolean includeItemsToAdd)
    {
        if (includeItemsToAdd)
        {
            List<TravellingItem> list = new ArrayList<TravellingItem>(items);
            list.addAll(itemsToAdd);
            return list;
        }
        return items;
    }

    @Override
    public TileEntity getTileEntity()
    {
        return this;
    }

    @Override
    public void receiveTravellingItem(TravellingItem item)
    {
        if (worldObj.isRemote)
        {
            if (!item.getPath().isEmpty())
                items.add(item);
        }
        else
        {
            if (item.getPath().isEmpty())
            {
                retryOutput(item, item.input);
            }
            itemsToAdd.add(item);
        }
    }

    public void outputToSide(TravellingItem item, byte side)
    {
        if (getConnectedRelocators()[side] != null)
        {
            getConnectedRelocators()[side].receiveTravellingItem(item);
        }
        else if (getConnectedInventories()[side] != null)
        {
            ItemStack stack = IOHelper.insert(getConnectedInventories()[side], item.getItemStack().copy(), ForgeDirection.getOrientation(side).getOpposite(), false);
            if (stack != null)
            {
                item.getItemStack().stackSize = stack.stackSize;
                retryOutput(item, side);
            }
        }
        else
        {
            retryOutput(item, side);
        }
    }

    public void retryOutput(TravellingItem item, byte side)
    {
        ItemStack stack = item.getItemStack();
        TravellingItem travellingItem = RelocatorGridLogic.findOutput(item.getItemStack(), this, side);
        if (travellingItem != null)
        {
            travellingItem.setStartingPoint(item.getStartingPoint());
            receiveTravellingItem(travellingItem);
            stack.stackSize -= item.getStackSize();
            if (stack.stackSize <= 0)
            {
                stack = null;
            }
        }
        if (stack != null)
        {
            stuffedItems[side] = item;
        }
    }

    @Override
    public ItemStack insert(ItemStack itemStack, int side, boolean simulate)
    {
        if (passesFilter(itemStack, side, true))
        {
            TravellingItem item = RelocatorGridLogic.findOutput(itemStack.copy(), this, side);
            if (item != null)
            {
                if (!simulate)
                {
                    receiveTravellingItem(item);
                }
                itemStack.stackSize -= item.getStackSize();
                if (itemStack.stackSize <= 0)
                {
                    return null;
                }
            }
        }
        return itemStack;
    }

    @Override
    public boolean connectsToSide(int side)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
            return relocators[side] != null || inventories[side] != null;
        else
            return isConnected[side];
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("Items"))
        {
            NBTTagList nbttaglist = compound.getTagList("Items");
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
                items.add(TravellingItem.createFromNBT(nbttagcompound1));
            }
        }
        if (compound.hasKey("ItemsToAdd"))
        {
            NBTTagList nbttaglist = compound.getTagList("ItemsToAdd");
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
                itemsToAdd.add(TravellingItem.createFromNBT(nbttagcompound1));
            }
        }

        readFilters(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (!items.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();
            for (TravellingItem item : this.items)
            {
                if (item != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    item.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }
            compound.setTag("Items", nbttaglist);
        }

        if (!itemsToAdd.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();
            for (TravellingItem item : this.itemsToAdd)
            {
                if (item != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    item.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }
            compound.setTag("ItemsToAdd", nbttaglist);
        }

        saveFilters(compound);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++)
        {
            if (connectsToSide(i))
                tag.setBoolean("c" + i, true);
        }
        saveFilters(tag);

        return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
    {
        NBTTagCompound tag = pkt.data;
        for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++)
        {
            isConnected[i] = tag.hasKey("c" + i);
        }
        readFilters(pkt.data);
    }

    public void saveFilters(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < filters.length; i++)
        {
            if (filters[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setString("clazzIdentifier", RelocatorFilterRegistry.getIdentifier(filters[i].getClass()));
                nbttagcompound1.setByte("place", (byte) i);
                filters[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        compound.setTag("filters", nbttaglist);
    }

    public void readFilters(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = compound.getTagList("filters");
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
            byte place = nbttagcompound1.getByte("place");
            IRelocatorModule filter = RelocatorFilterRegistry.getFilter(nbttagcompound1.getString("clazzIdentifier"));
            if (filter != null)
            {
                filters[place] = filter;
                filters[place].readFromNBT(nbttagcompound1);
            }
        }
    }

    /*
    ISidedInventory implementation
     */

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[] { side };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int side)
    {
        if (passesFilter(itemStack, side, true))
        {
            cachedTravellingItem = RelocatorGridLogic.findOutput(itemStack, this, side);
            if (cachedTravellingItem != null)
            {
                maxStackSize = cachedTravellingItem.getStackSize();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int side)
    {
        return false;
    }

    @Override
    public int getSizeInventory()
    {
        return 6;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int side, ItemStack itemstack)
    {
        if (side == cachedTravellingItem.input && cachedTravellingItem.isItemSameAs(itemstack))
        {
            receiveTravellingItem(cachedTravellingItem);
        }
        else
        {
            TravellingItem travellingItem = RelocatorGridLogic.findOutput(itemstack, this, side);
            if (travellingItem != null)
            {
                itemstack.stackSize -= travellingItem.getStackSize();
                receiveTravellingItem(travellingItem);
            }
            if (itemstack != null && itemstack.stackSize > 0)
            {
                TileEntity tile = DirectionHelper.getTileAtSide(this, ForgeDirection.getOrientation(side).getOpposite());
                LogHelper.warning(String.format("%s at %s:%s:%s inserted ItemStack wrongly into Relocator!!", BlockHelper.getTileEntityDisplayName(tile), tile.xCoord, tile.yCoord, tile.zCoord));
                IOHelper.spawnItemInWorld(worldObj, itemstack, xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public String getInvName()
    {
        return "tile.relocator.name";
    }

    @Override
    public boolean isInvNameLocalized()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return maxStackSize;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public void openChest()
    {
    }

    @Override
    public void closeChest()
    {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack)
    {
        return true;
    }
}
