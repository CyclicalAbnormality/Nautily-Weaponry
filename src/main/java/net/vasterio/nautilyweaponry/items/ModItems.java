package net.vasterio.nautilyweaponry.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.vasterio.nautilyweaponry.NautilyWeaponry;
import net.vasterio.nautilyweaponry.components.FirthOfFifthsData;
import net.vasterio.nautilyweaponry.data.ModDataComponents;
import net.vasterio.nautilyweaponry.items.custom.FirthOfFifthsItems;

import java.util.Optional;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NautilyWeaponry.MOD_ID);

    public static final DeferredItem<FirthOfFifthsItems> FIFTH_OF_FIFTHS = ITEMS.register(
            "firth_of_fifths",
            () -> new FirthOfFifthsItems(
                    Tiers.DIAMOND,
                    new Item.Properties()
                            .component(ModDataComponents.FIRTH_DATA.get(), new FirthOfFifthsData(0, Optional.empty(), 0.0f, 0L, 0L, false))
                            .attributes(SwordItem.createAttributes(Tiers.DIAMOND, -4.0F, -2.4F))
            )
    );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
