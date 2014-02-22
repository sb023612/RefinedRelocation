package com.dynious.blex.proxy;

import com.dynious.blex.lib.BlockIds;
import com.dynious.blex.renderer.*;
import com.dynious.blex.renderer.ItemRendererFilteringIronChest;
import com.dynious.blex.tileentity.*;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
    @Override
    public void initTileEntities()
    {
        super.initTileEntities();
        ClientRegistry.bindTileEntitySpecialRenderer(TileBlockExtender.class, new RendererBlockExtender());
        ClientRegistry.bindTileEntitySpecialRenderer(TileWirelessBlockExtender.class, new RendererWirelessBlockExtender());
        ClientRegistry.bindTileEntitySpecialRenderer(TileBuffer.class, new RendererBuffer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFilteringChest.class, new RendererFilteringChest());

        MinecraftForgeClient.registerItemRenderer(BlockIds.BLOCK_EXTENDER, new ItemRendererBlockExtender());
        MinecraftForgeClient.registerItemRenderer(BlockIds.BUFFER, new ItemRendererBuffer());
        MinecraftForgeClient.registerItemRenderer(BlockIds.FILTERING_CHEST, new ItemRendererFilteringChest());

        if (Loader.isModLoaded("IronChest"))
        {
            ClientRegistry.bindTileEntitySpecialRenderer(TileFilteringIronChest.class, new RendererFilteringIronChest());
            MinecraftForgeClient.registerItemRenderer(BlockIds.FILTERING_IRON_CHEST, new ItemRendererFilteringIronChest());
        }
    }
}
