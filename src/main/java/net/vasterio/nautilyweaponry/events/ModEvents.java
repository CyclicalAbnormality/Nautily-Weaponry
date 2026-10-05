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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
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

    //might also need to check for CoolDown here
    @SubscribeEvent
    public static void onPlayerAttackFOF_ForRecord(AttackEntityEvent Event){
        //find the player that attacks
        Player player = Event.getEntity();
        //check if the action is done on the server side
        if (player.level().isClientSide()){
            return;
        }
        //check if the player's main-hand item is FOF
        if (player.getMainHandItem().getItem() instanceof FirthOfFifthsItems FOF){
            FirthOfFifthsData data = FOF.getData(player.getMainHandItem());
            // mode is set to 1, and there's no existing locked-on player, the totaldmg of FOF is still the baseline
            if (Event.getTarget() instanceof LivingEntity livingEntity
                    && data.MODE() == 1
                    && data.targetedplayer().isEmpty()
                    && data.totaldmg() == 0
                    && data.CoolDownEndTick() <= player.level().getGameTime()) {
                //set the timer
                long timeLeft = player.level().getGameTime() + 600L;
                FirthOfFifthsData update = new FirthOfFifthsData(
                        data.MODE(),
                        Optional.of(Event.getTarget().getUUID()),
                        data.totaldmg(),
                        timeLeft,
                        data.CoolDownEndTick(),
                        data.isUpgraded()
                );
                //applying the glow effect, this can and SHOULD be changed in the future for particle animations
                livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, true));
                //actually updating the item
                player.getMainHandItem().set(ModDataComponents.FIRTH_DATA.get(), update);
                //cancel the attack
                Event.setCanceled(true);
                return;
            }

            /*if mode isn't 1 or there is an existing locked-on player, then it is simple since that just means FOF won't work
            but if it is the case where FOF's total damage isn't 0, this will be dealt with in the other events, it might be possible
            to deal with it here, but I'll need to look that up*/
            return;
        }

        //if the item in the player's main hand isn't FOF, then just don't do anything, it's fine
        return;
    }

    //this will need a little rework, mainly on the code structure
    @SubscribeEvent
    public static void onTargetAttackedFOF_ForRecord(LivingDamageEvent.Post Event){
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
                                    data.CoolDownEndTick(),
                                    data.isUpgraded()
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
                                    data.CoolDownEndTick(),
                                    data.isUpgraded()
                            );
                            //actually updates the thing
                            stack.set(ModDataComponents.FIRTH_DATA.get(), updated);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onReleaseDamageFOF(LivingIncomingDamageEvent event){
        //check if it happens on the server side
        if (event.getEntity().level().isClientSide()){
            return;
        }

        //check if the source of attack comes from a player
        if (event.getSource().getEntity() instanceof Player player){
            //did they attack using FOF????
            if (player.getMainHandItem().getItem() instanceof FirthOfFifthsItems FOF){
                FirthOfFifthsData data = FOF.getData(player.getMainHandItem());
                //is the timer = 0 and dmg > 0???
                if(data.TimeLeft() <= player.level().getGameTime() && data.totaldmg() > 0){
                    //set the damage done to the entity equal to the damage recorded in FOF
                    event.setAmount(data.totaldmg());
                    //set the coolDown
                    long coolDown = player.level().getGameTime() + 1200L;
                    //then reset FOF and add coolDown
                    FirthOfFifthsData updated = new FirthOfFifthsData(
                            data.MODE(),
                            Optional.empty(),
                            0.0F,
                            player.level().getGameTime(),
                            coolDown,
                            data.isUpgraded()
                    );
                    //actually updating the shit
                    player.getMainHandItem().set(ModDataComponents.FIRTH_DATA.get(), updated);
                }
                //if not FOF, just opt out
                return;
            }
            //if not player, opt out
            return;
        }
    }
}
