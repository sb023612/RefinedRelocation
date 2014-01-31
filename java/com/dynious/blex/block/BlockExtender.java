package com.dynious.blex.block;

import com.dynious.blex.BlockExtenders;
import com.dynious.blex.gui.GuiAdvancedBlockExtender;
import com.dynious.blex.gui.GuiAdvancedFilteredBlockExtender;
import com.dynious.blex.gui.GuiFiltered;
import com.dynious.blex.gui.GuiWirelessBlockExtender;
import com.dynious.blex.helper.DistanceHelper;
import com.dynious.blex.helper.GuiHelper;
import com.dynious.blex.item.ModItems;
import com.dynious.blex.lib.Names;
import com.dynious.blex.lib.Settings;
import com.dynious.blex.tileentity.*;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.List;
import java.util.Random;

public class BlockExtender extends BlockContainer
{
    public BlockExtender(int id)
    {
        super(id, Material.rock);
        this.setUnlocalizedName(Names.blockExtender);
		this.setHardness(3.0F);
        this.setCreativeTab(BlockExtenders.tabBlEx);
    }


    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        switch (metadata)
        {
            case 0:
                return new TileBlockExtender();
            case 1:
                return new TileAdvancedBlockExtender();
            case 2:
                return new TileFilteredBlockExtender();
            case 3:
                return new TileAdvancedFilteredBlockExtender();
            case 4:
                return new TileWirelessBlockExtender();
            default:
                return null;
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs,
                             List par3List)
    {
        for (int j = 0; j < (Settings.DISABLE_WIRELESS_BLOCK_EXTENDER ? 4 : 5); ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        if (player.isSneaking())
        {
            if (player.getCurrentEquippedItem() == null)
            {
                TileEntity tile = world.getBlockTileEntity(x, y, z);
                if (tile != null && tile instanceof TileBlockExtender)
                {
                    ((TileBlockExtender)tile).setRedstoneEnabled( !((TileBlockExtender)tile).isRedstoneEnabled );
                    return true;
                }
            }
            return false;
        }
        else
        {
            TileEntity tile = world.getBlockTileEntity(x, y, z);
            if (tile != null)
            {
                if (tile instanceof TileWirelessBlockExtender)
                {
                    if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == ModItems.linker && player.getCurrentEquippedItem().hasTagCompound())
                    {
                        NBTTagCompound tag = player.getCurrentEquippedItem().getTagCompound();
                        int tileX = tag.getInteger("tileX");
                        int tileY = tag.getInteger("tileY");
                        int tileZ = tag.getInteger("tileZ");

                        if (DistanceHelper.getDistanceSq(x, y, z, tileX, tileY, tileZ) <= Settings.MAX_RANGE_WIRELESS_BLOCK_EXTENDER * Settings.MAX_RANGE_WIRELESS_BLOCK_EXTENDER)
                        {
                            ((TileWirelessBlockExtender) tile).setConnection(tileX, tileY, tileZ);
                            if (world.isRemote)
                            {
                                player.sendChatToPlayer(new ChatMessageComponent()
                                        .addText("This Wireless Block Extender is now link with the TileEntity at: " + tileX + ":" + tileY + ":" + tileZ));
                            }
                        }
                        else
                        {
                            if (world.isRemote)
                            {
                                player.sendChatToPlayer(new ChatMessageComponent()
                                        .addText("This Wireless Block Extender too far from the TileEntity at: " + tileX + ":" + tileY + ":" + tileZ));
                                player.sendChatToPlayer(new ChatMessageComponent()
                                        .addText("This Wireless Block Extender max range is: " + Settings.MAX_RANGE_WIRELESS_BLOCK_EXTENDER));
                            }
                        }
                        return true;
                    }
                }
                GuiHelper.openGui(tile);
            }
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int par5)
    {
        super.onNeighborBlockChange(world, x, y, z, par5);
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (tile != null && tile instanceof TileBlockExtender)
        {
            ((TileBlockExtender) tile).blocksChanged = true;
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (tile != null && tile instanceof TileBlockExtender)
        {
            return ((TileBlockExtender) tile).canConnectRedstone( side );
        }
        return false;
    }

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        return isProvidingWeakPower(world, x, y, z, side);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (tile != null && tile instanceof TileBlockExtender)
        {
            return ((TileBlockExtender) tile).isPoweringTo(side);
        }
        return 0;
	}

    @SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		TileBlockExtender tile = (TileBlockExtender) world.getBlockTileEntity(x, y, z);

		if (!tile.isRedstonePowered)
			return;

		float f = (float) x + 0.5F;
		float f1 = (float) y + 0.5F + (random.nextFloat() * 6F) / 16F;
		float f2 = (float) z + 0.5F;
		float f3 = 0.6F;
		float f4 = random.nextFloat() * 0.6F - 0.3F;

		world.spawnParticle("reddust", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
    {
        return true;
    }

    @Override
    public int getRenderBlockPass()
    {
        return 1;
    }

    @Override
    protected String getTextureName()
    {
        return "obsidian";
    }
    
    @Override
	public int damageDropped (int metadata) {
		return metadata;
	}


}
