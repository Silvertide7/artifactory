/*

The MIT License (MIT)

Copyright (c) 2020 Joseph Bettendorff a.k.a. "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

package net.silvertide.artifactory.config.codecs;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic data loader for Codec-parsable data.
 * This works best if initialized during your mod's construction.
 * After creating the manager, subscribeAsSyncable can optionally be called on it to subscribe the manager
 * to the forge events necessary for syncing datapack data to clients.
 */
public class MergeableCodecDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, ItemAttunementData>>
{
    private static final Logger LOGGER = LogManager.getLogger();

    /** ".json" **/
    protected static final String JSON_EXTENSION = ".json";
    /** 5 **/
    protected static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    /** the loaded data **/
    protected Map<ResourceLocation, ItemAttunementData> data = new HashMap<>();

    private final String folderName;
    private final Codec<ItemAttunementData> codec;
    private final Function<List<ItemAttunementData>, ItemAttunementData> merger;

    /**
     * Initialize a data manager with the given folder name, codec, and merger
     * @param folderName The name of the folder to load data from,
     * e.g. "cheeses" would load data from "data/modid/cheeses" for all modids.
     * Can include subfolders, e.g. "cheeses/sharp"
     * @param codec A codec that will be used to parse jsons. See drullkus's codec primer for help on creating these:
     * https://gist.github.com/Drullkus/1bca3f2d7f048b1fe03be97c28f87910
     * @param merger A merging function that uses a list of java-objects-that-were-parsed-from-json to create a final object.
     * The list contains all successfully-parsed objects with the same ID from all mods and datapacks.
     * (for a json located at "data/modid/folderName/name.json", the object's ID is "modid:name")
     * As an example, consider vanilla's Tags: mods or datapacks can define tags with the same modid:name itemUUID,
     * and then all tag jsons defined with the same ID are merged additively into a single set of items, etc
     */
    public MergeableCodecDataManager(final String folderName, Codec<ItemAttunementData> codec, final Function<List<ItemAttunementData>, ItemAttunementData> merger)
    {
        this.folderName = folderName;
        this.codec = codec;
        this.merger = merger;
    }

    /**
     * @return The immutable map of data entries
     */
    public Map<ResourceLocation, ItemAttunementData> getData()
    {
        return this.data;
    }

    /** Off-thread processing (can include reading files from hard drive) **/
    @Override
    protected Map<ResourceLocation, ItemAttunementData> prepare(final ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);
        final Map<ResourceLocation, ItemAttunementData> map = new HashMap<>();

        Map<ResourceLocation,List<Resource>> resourceStacks = resourceManager.listResourceStacks(this.folderName, id -> id.getPath().endsWith(JSON_EXTENSION));
        for (var entry : resourceStacks.entrySet())
        {
            List<ItemAttunementData> raws = new ArrayList<>();
            ResourceLocation fullId = entry.getKey();
            String fullPath = fullId.getPath(); // includes folderName/ and .json
            ResourceLocation id = new ResourceLocation(
                    fullId.getNamespace(),
                    fullPath.substring(this.folderName.length() + 1, fullPath.length() - JSON_EXTENSION_LENGTH));

            for (Resource resource : entry.getValue())
            {
                try(Reader reader = resource.openAsReader())
                {
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    this.codec.parse(JsonOps.INSTANCE, jsonElement)
                            .resultOrPartial(errorMsg -> LOGGER.error("Error deserializing json {} in folder {} from pack {}: {}", id, this.folderName, resource.sourcePackId(), errorMsg))
                            .ifPresent(raws::add);
                }
                catch(Exception e)
                {
                    LOGGER.error(String.format(Locale.ENGLISH, "Error reading resource %s in folder %s from pack %s: ", id, this.folderName, resource.sourcePackId()), e);
                }
            }
            map.put(id, merger.apply(raws));
        }

        LOGGER.info("Data loader for {} loaded {} finalized objects", this.folderName, this.data.size());
        return Map.copyOf(map);
    }

    /** Main-thread processing, runs after prepare concludes **/
    @Override
    protected void apply(final Map<ResourceLocation, ItemAttunementData> processedData, final ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        Map<ResourceLocation, ItemAttunementData> filteredData = filterItems(processedData);
        sanitizeItemRequirements(filteredData);

        // now that we're on the main thread, we can finalize the data
        this.data.putAll(filteredData);
    }

    /**
     * This goes through all resource locations and makes sure that the items given attunementLevels meet 3 critera:
     * 1 - They are valid items and do not fail to find an item from the resource location
     * 2 - They have a max stack size of 1. The functionality of this mod doesn't make sense with items that can stack
     * 3 - It is not a block item, again that doesn't seem to fit the functionality of this mod.
     * @param data The data to filter
     * @return
     */
    private Map<ResourceLocation,ItemAttunementData> filterItems(Map<ResourceLocation, ItemAttunementData> data) {
        Map<ResourceLocation, ItemAttunementData> filteredData = new HashMap<>();
        for(ResourceLocation key : data.keySet()) {
            ItemStack stack = ResourceLocationUtil.getItemStackFromResourceLocation(key);
            if(!stack.isEmpty() & stack.getMaxStackSize() == 1 && !(stack.getItem() instanceof BlockItem)) filteredData.put(key, data.get(key));
        }
        return filteredData;
    }

    /**
     * This method looks at all attunement level item requirements and makes sure they exist
     * in the game. If it can't create an ItemStack of them then it will remove it.
     * @param data - The data to search for item requirements in
     */
    private void sanitizeItemRequirements(Map<ResourceLocation, ItemAttunementData> data) {
        // TODO
//        for(ItemAttunementData attunementData : data.values()) {
//            for(String levelString : attunementData.attunementLevels().keySet()) {
//
//            }
//        }
    }

    /**
     * This should be called at most once, during construction of your mod
     * Calling this method automatically subscribes a packet-sender to {@link OnDatapackSyncEvent}.
     * @param <PACKET> the packet type that will be sent on the given channel
     * @param channel The networking channel of your mod
     * @param packetFactory  A packet constructor or factory method that converts the given map to a packet object to send on the given channel
     * @return this manager object
     */
    public <PACKET> MergeableCodecDataManager subscribeAsSyncable(final SimpleChannel channel,
                                                                             final Function<Map<ResourceLocation, ItemAttunementData>, PACKET> packetFactory)
    {
        MinecraftForge.EVENT_BUS.addListener(this.getDatapackSyncListener(channel, packetFactory));
        return this;
    }

    /** Generate an event listener function for the on-datapack-sync event **/
    private <PACKET> Consumer<OnDatapackSyncEvent> getDatapackSyncListener(final SimpleChannel channel,
                                                                           final Function<Map<ResourceLocation, ItemAttunementData>, PACKET> packetFactory)
    {
        return event -> {
            ServerPlayer player = event.getPlayer();
            PACKET packet = packetFactory.apply(this.data);
            PacketTarget target = player == null
                    ? PacketDistributor.ALL.noArg()
                    : PacketDistributor.PLAYER.with(() -> player);
            channel.send(target, packet);
        };
    }
}
