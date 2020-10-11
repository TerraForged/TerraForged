/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.fm.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.terraforged.fm.FeatureManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Predicate;

public class DataManager implements AutoCloseable {

    public static final Predicate<String> ANY = s -> true;
    public static final Predicate<String> NBT = s -> s.endsWith(".nbt");
    public static final Predicate<String> JSON = s -> s.endsWith(".json");

    private final ResourcePackList packList;
    private final IResourceManager resourceManager;

    public DataManager(IResourceManager resourceManager, ResourcePackList packList) {
        this.resourceManager = resourceManager;
        this.packList = packList;
    }

    @Override
    public void close() {
        packList.close();
    }

    public IResource getResource(ResourceLocation location) throws IOException {
        return resourceManager.getResource(location);
    }

    public void forEach(String path, Predicate<String> matcher, ResourceVisitor<InputStream> consumer) {
        FeatureManager.LOG.debug("Input path: {}", path);
        for (ResourceLocation location : resourceManager.getAllResourceLocations(path, matcher)) {
            FeatureManager.LOG.debug(" Location: {}", location);
            try (IResource resource = getResource(location)) {
                if (resource == null) {
                    continue;
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    consumer.accept(location, inputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void forEachJson(String path, ResourceVisitor<JsonElement> consumer) {
        JsonParser parser = new JsonParser();
        forEach(path, DataManager.JSON, (location, data) -> {
            Reader reader = new BufferedReader(new InputStreamReader(data));
            JsonElement element = parser.parse(reader);
            consumer.accept(location, element);
        });
    }

    public static DataManager of(File dir) {
        SimpleReloadableResourceManager manager = new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA);
        ResourcePackList packList = new ResourcePackList(ResourcePackInfo::new);

        packList.addPackFinder(new ModDataPackFinder());
        // add global packs after mods so that they override
        packList.addPackFinder(new FolderDataPackFinder(dir));

        packList.reloadPacksFromFinders();
        packList.getAllPacks().stream().map(ResourcePackInfo::getResourcePack).forEach(manager::addResourcePack);

        return new DataManager(manager, packList);
    }
}
