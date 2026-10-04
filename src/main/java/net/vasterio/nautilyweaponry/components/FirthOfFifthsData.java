package net.vasterio.nautilyweaponry.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.vasterio.nautilyweaponry.items.custom.FirthOfFifthsItems;

import java.util.Optional;
import java.util.UUID;

public record FirthOfFifthsData(
        int MODE,
        Optional<UUID> targetedplayer,
        float totaldmg,
        long TimeLeft,
        long CoolDownEndTick
) {
    public static final Codec<FirthOfFifthsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("MODE").forGetter(FirthOfFifthsData::MODE),
                    UUIDUtil.CODEC.optionalFieldOf("targetedplayer").forGetter(FirthOfFifthsData::targetedplayer),
                    Codec.FLOAT.fieldOf("totaldmg").forGetter(FirthOfFifthsData::totaldmg),
                    Codec.LONG.fieldOf("TimeLeft").forGetter(FirthOfFifthsData::TimeLeft),
                    Codec.LONG.fieldOf("CoolDownEndTick").forGetter(FirthOfFifthsData::CoolDownEndTick)
            ).apply(instance, FirthOfFifthsData::new));
}
