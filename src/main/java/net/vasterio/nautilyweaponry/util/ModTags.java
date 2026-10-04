package net.vasterio.nautilyweaponry.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.vasterio.nautilyweaponry.NautilyWeaponry;

public class ModTags {

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(NautilyWeaponry.MOD_ID, name));
        }
    }
}
