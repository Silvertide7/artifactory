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
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementRequirements;
import net.silvertide.artifactory.client.state.ItemRequirements;
import net.silvertide.artifactory.util.ResourceLocationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.util.*;
import java.util.function.Function;

/**
 * Generic data loader for Codec-parsable data.
 * This works best if initialized during your mod's construction.
 * After creating the manager, subscribeAsSyncable can optionally be called on it to subscribe the manager
 * to the forge events necessary for syncing datapack data to clients.
 */
public class MergeableCodecDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, AttunementDataSource>>
{
    private static final Logger LOGGER = LogManager.getLogger();

    /** ".json" **/
    protected static final String JSON_EXTENSION = ".json";
    /** 5 **/
    protected static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    /** the loaded data **/
    protected Map<ResourceLocation, AttunementDataSource> data = new HashMap<>();

    private final String folderName;
    private final Codec<AttunementDataSource> codec;
    private final Function<List<AttunementDataSource>, AttunementDataSource> merger;

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
    public MergeableCodecDataManager(final String folderName, Codec<AttunementDataSource> codec, final Function<List<AttunementDataSource>, AttunementDataSource> merger)
    {
        this.folderName = folderName;
        this.codec = codec;
        this.merger = merger;
    }

    /**
     * @return The immutable map of data entries
     */
    public Map<ResourceLocation, AttunementDataSource> getData()
    {
        return this.data;
    }

    /** Off-thread processing (can include reading files from hard drive) **/
    @Override
    protected Map<ResourceLocation, AttunementDataSource> prepare(final ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);
        final Map<ResourceLocation, AttunementDataSource> map = new HashMap<>();

        Map<ResourceLocation,List<Resource>> resourceStacks = resourceManager.listResourceStacks(this.folderName, id -> id.getPath().endsWith(JSON_EXTENSION));
        for (var entry : resourceStacks.entrySet())
        {
            List<AttunementDataSource> raws = new ArrayList<>();
            ResourceLocation fullId = entry.getKey();
            String fullPath = fullId.getPath(); // includes folderName/ and .json
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
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
    protected void apply(final Map<ResourceLocation, AttunementDataSource> processedData, final ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        // Sanitation is setup to check all item resource location codes and make sure they are valid within minecraft.
        // If they are not they are removed and warning is logged.
        Artifactory.LOGGER.info("Artifactory - Reading and Validating Data Packs - Start");
        Map<ResourceLocation, AttunementDataSource> filteredData = filterItems(processedData);
        Map<ResourceLocation, AttunementDataSource> sanitizedData = sanitizeItemRequirements(filteredData);

        this.data.putAll(sanitizedData);
        Artifactory.LOGGER.info("Artifactory - Reading and Validating Data Packs - Complete");
    }

    /**
     * This goes through all resource locations and makes sure that the items given attunementLevels meet 3 critera:
     * 1 - They are valid items and do not fail to find an item from the resource location
     * 2 - They have a max stack size of 1. The functionality of this mod doesn't make sense with items that can stack
     * 3 - It is not a block item, again that doesn't seem to fit the functionality of this mod.
     * @param data The data to filter
     * @return
     */
    private Map<ResourceLocation, AttunementDataSource> filterItems(Map<ResourceLocation, AttunementDataSource> data) {
        Map<ResourceLocation, AttunementDataSource> filteredData = new HashMap<>();
        for(ResourceLocation key : data.keySet()) {
            ItemStack stack = ResourceLocationUtil.getItemStackFromResourceLocation(key);
            if(!stack.isEmpty() & stack.getMaxStackSize() == 1 && !(stack.getItem() instanceof BlockItem)){
                filteredData.put(key, data.get(key));
            } else {
                Artifactory.LOGGER.warn("Artifactory - " + key + " - Invalid datapack pathway found. Either item doesn't exist, it is a block item, or it has a stack size greater than 1.");
            }
        }
        return filteredData;
    }

    /**
     * This method looks at all attunement level item requirements and makes sure they exist
     * in the game. If it can't create an ItemStack of them then it will remove it.
     * @param data - The data to search for item requirements in
     */
    private Map<ResourceLocation, AttunementDataSource> sanitizeItemRequirements(Map<ResourceLocation, AttunementDataSource> data) {
        Map<ResourceLocation, AttunementDataSource> sanitizedData = new HashMap<>();
        for(Map.Entry<ResourceLocation, AttunementDataSource> entry : data.entrySet()) {
            AttunementDataSource dataSource = entry.getValue();
            if(!dataSource.useWithoutAttunement() && dataSource.chance() != 1.0D){
                dataSource = dataSource.withChance(1.0D);
                Artifactory.LOGGER.warn("Artifactory - " + entry.getKey() + " - chance is not valid, must have a chance of 1.0D if use_without_attunement is false. Setting to 1.0D.");
            }

            // Sanitize items in attunement levels
            List<AttunementLevel> sanitizedAttunementLevels = new ArrayList<>();
            for(AttunementLevel attunementLevel : dataSource.attunementLevels()) {
                List<String> itemCodes = attunementLevel.requirements().items();
                List<String> sanitizedItems = new ArrayList<>();
                if(!itemCodes.isEmpty()) {
                    for (String itemCode : itemCodes) {
                        ItemStack stack = ItemRequirements.parseItemStack(itemCode);
                        if (stack != null && !stack.isEmpty()) {
                            sanitizedItems.add(itemCode);
                        } else {
                            Artifactory.LOGGER.warn("Artifactory - " + entry.getKey() + " - ItemRequirement not valid, must be modid:itemid#quantity - " + itemCode + " removed.");
                        }
                    }
                }
                AttunementRequirements sanitizedAttunementRequirements = attunementLevel.requirements().withItems(sanitizedItems);
                sanitizedAttunementLevels.add(attunementLevel.withRequirements(sanitizedAttunementRequirements));
            }
            sanitizedData.put(entry.getKey(), dataSource.withAttunementLevels(sanitizedAttunementLevels));
        }
        return sanitizedData;
    }
}
