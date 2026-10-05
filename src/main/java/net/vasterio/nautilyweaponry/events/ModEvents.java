package net.vasterio.nautilyweaponry.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.vasterio.nautilyweaponry.NautilyWeaponry;
import net.vasterio.nautilyweaponry.components.FirthOfFifthsData;
import net.vasterio.nautilyweaponry.data.ModDataComponents;
import net.vasterio.nautilyweaponry.items.custom.FirthOfFifthsItems;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = NautilyWeaponry.MOD_ID)
public class ModEvents {

    //this needs a little reworked to also consider the case where there is damage stored in FOF
    @SubscribeEvent
    public static void onPlayerAttackFOF(AttackEntityEvent Event){
        //find the player that attacks
        Player player = Event.getEntity();
        //check if the action is done on the server side
        if (player.level().isClientSide()){
            return;
        }
        //check if the player's main-hand item is FOF
        if (player.getMainHandItem().getItem() instanceof FirthOfFifthsItems FOF){
            //check if FOF's MODE is set to 1
            if (FOF.getData(player.getMainHandItem()).MODE() == 1){
                //check if there's currently no targeted player in the FOF
                if (FOF.getData(player.getMainHandItem()).targetedplayer().isEmpty()){
                    //check if recorded damage = 0 or not
                    if (FOF.getData(player.getMainHandItem()).totaldmg() == 0.0f){
                        //set the timer
                        long timeLeft = player.level().getGameTime() + 600L;
                        //put the UUID of the entity that is attacked into target slot
                        FirthOfFifthsData current = FOF.getData(player.getMainHandItem());
                        FirthOfFifthsData updated = new FirthOfFifthsData(
                                current.MODE(),
                                Optional.of(Event.getTarget().getUUID()),
                                current.totaldmg(),
                                timeLeft,
                                current.CoolDownEndTick()
                        );
                        //update
                        player.getMainHandItem().set(ModDataComponents.FIRTH_DATA.get(), updated);
                        //apply potion effects
                        if (Event.getTarget() instanceof LivingEntity LivingEntity){
                            LivingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, true));
                        }
                        //cancel the attacking result
                        Event.setCanceled(true);
                        return;
                    }
                    //if the recorded damage is more than 0, do the following:
                    else {
                        long timeLeft = player.level().getGameTime() + 600L;
                        long coolDown = player.level().getGameTime() + 1200L;
                        //if the timer is still on (item is still activated), cancel the event
                        if (player.level().getGameTime() <= timeLeft){
                            Event.setCanceled(true);
                            return;
                        }
                        //in case 'record' is not activated
                        FirthOfFifthsData current = FOF.getData(player.getMainHandItem());
                        FirthOfFifthsData updated = new FirthOfFifthsData(
                                0,
                                Optional.empty(),
                                0.0f,
                                0L,
                                coolDown
                        );
                        //update the shit
                        player.getMainHandItem().set(ModDataComponents.FIRTH_DATA.get(), updated);
                    }
                }
                return;
            }
            return;
        }
        return;
    }

    @SubscribeEvent
    public static void onTargetAttackedFOF(LivingDamageEvent.Post Event){
        //check the thing that got attacked
        LivingEntity damagedEntity = Event.getEntity();
        //check if it's on server side
        if(damagedEntity.level().isClientSide()){
            return;
        }
        //get the UUID of the attacked thingy
        UUID damagedEntityUUID = damagedEntity.getUUID();
        //get the damaged dealt to the thingy
        float damagedDealt = Event.getNewDamage();

        //define possible FOF that could be attached to the thingy
        List<? extends Player> PlayerList;
        //if number of players on the server is more than 10, use area based search
        if (damagedEntity.level().players().size() >= 10) {
            AABB searchArea = damagedEntity.getBoundingBox().inflate(128.0);
            PlayerList = damagedEntity.level().getEntitiesOfClass(Player.class, searchArea);
        }
        //if it's not, get all the players
        else {
            PlayerList = damagedEntity.level().players();
        }

        //loop through every player in the list
        for (Player player : PlayerList){

            //loop through every single slots in their inventories
            for (int i = 0; i < player.getInventory().getContainerSize(); i++){
                ItemStack stack = player.getInventory().getItem(i);

                //if the item in the current slot is FOF, perform the following
                if (stack.getItem() instanceof FirthOfFifthsItems fofitems){
                    //get the data components for the current FOF
                    FirthOfFifthsData data = fofitems.getData(stack);

                    //if mode = record, there's actually a target, and the target is actually the thingy, then do the shit
                    if (data.MODE() == 1 && data.targetedplayer().isPresent() && data.targetedplayer().equals(Optional.of(damagedEntityUUID))){

                        //find the current game time
                        long currentTick = damagedEntity.level().getGameTime();

                        //if the current game time is less than the time left, that means that FOF is still recording
                        if(currentTick <= data.TimeLeft()) {
                            //set the new total damage to be the current damage + the added
                            float new_totalDMG = data.totaldmg() + damagedDealt;
                            FirthOfFifthsData updated = new FirthOfFifthsData(
                                    data.MODE(),
                                    data.targetedplayer(),
                                    new_totalDMG,
                                    data.TimeLeft(),
                                    data.CoolDownEndTick()
                            );
                            //actually updates the thing
                            stack.set(ModDataComponents.FIRTH_DATA.get(), updated);
                        }

                        //if not, then reset everything in the FOF
                        else {
                            FirthOfFifthsData updated = new FirthOfFifthsData(
                                    0,
                                    Optional.empty(),
                                    //this line actually doesn't make sense, the damage only resets when it hits something not when the timer runs out
                                    0.0f,
                                    0L,
                                    data.CoolDownEndTick()
                            );
                            //actually updates the thing
                            stack.set(ModDataComponents.FIRTH_DATA.get(), updated);
                        }
                    }
                }
            }
        }
    }
}
