package net.vasterio.nautilyweaponry.data;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.vasterio.nautilyweaponry.NautilyWeaponry;
import net.vasterio.nautilyweaponry.components.FirthOfFifthsData;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, NautilyWeaponry.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FirthOfFifthsData>> FIRTH_DATA =
            register("firth_data", builder -> builder
                    .persistent(FirthOfFifthsData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(FirthOfFifthsData.CODEC))
            );

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                           UnaryOperator<DataComponentType.Builder<T>> BuilderOperator){
        return DATA_COMPONENT_TYPE.register(name, () -> BuilderOperator.apply(DataComponentType.builder()).build());
    };

    public static void register(IEventBus eventBus){
        DATA_COMPONENT_TYPE.register(eventBus);
    }
}
