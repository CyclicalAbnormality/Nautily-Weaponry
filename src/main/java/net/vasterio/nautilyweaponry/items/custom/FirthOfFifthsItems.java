package net.vasterio.nautilyweaponry.items.custom;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.vasterio.nautilyweaponry.components.FirthOfFifthsData;
import net.vasterio.nautilyweaponry.data.ModDataComponents;

import java.util.Map;
import java.util.Optional;

public class FirthOfFifthsItems extends SwordItem {

    public enum MODES {
        NULL(0),
        RECORD(1),
        COLLECT(2),
        ACCUMULATE(3);

        private final int id;
        MODES(int id){ this.id = id; }
        public int getId() { return id; }
    }


    public FirthOfFifthsItems(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public FirthOfFifthsData getData(ItemStack stack){
        return stack.getOrDefault(
                ModDataComponents.FIRTH_DATA.get(),
                new FirthOfFifthsData(0, Optional.empty(), 0.0f, 0, 0));
    }

    public float getTotalDMG(ItemStack stack){
        return getData(stack).totaldmg();
    }

    public void setTotalDMG(ItemStack stack, float DMG){
        //REVIEW THIS
        FirthOfFifthsData current = getData(stack);
        FirthOfFifthsData updated = new FirthOfFifthsData(
                current.MODE(),
                current.targetedplayer(),
                DMG,
                current.TimeLeft(),
                current.CoolDownEndTick()
        );

        stack.set(ModDataComponents.FIRTH_DATA.get(), updated);
    }

}
