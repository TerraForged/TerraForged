package com.terraforged.mod.util;

import com.terraforged.mod.Log;
import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.ResourcePackInfo;

import java.io.File;
import java.util.Map;

public class DataPackFinder extends FolderPackFinder {

    public DataPackFinder(File folderIn) {
        super(folderIn);
    }

    @Override
    public <T extends ResourcePackInfo> void addPackInfosToMap(Map<String, T> map, ResourcePackInfo.IFactory<T> factory) {
        int start = map.size();
        super.addPackInfosToMap(map, factory);
        Log.info("Found {} datapacks", map.size() - start);
    }
}
