package blu3.ruhamaplus.module.modules.experimental;
import blu3.ruhamaplus.module.Category;
import blu3.ruhamaplus.module.Module;
import blu3.ruhamaplus.settings.SettingBase;
import blu3.ruhamaplus.settings.SettingMode;
import blu3.ruhamaplus.settings.SettingSlider;
import blu3.ruhamaplus.settings.SettingToggle;
import blu3.ruhamaplus.utils.*;
import blu3.ruhamaplus.utils.Timer;
import blu3.ruhamaplus.utils.friendutils.FriendManager;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;
public class blu3CrystalAura extends Module {
    private static final List<SettingBase> settings = Arrays.asList(
            new SettingMode("Logic: ", "BreakPlace", "PlaceBreak"), //0
            new SettingMode("Rotate: ", "Never", "Place", "Break", "Always"), //1
            new SettingMode("BreakHand: ", "Mainhand", "Offhand", "BothHands"), // 2
            new SettingMode("PlaceMode: ", "1.12", "1.13+"), //3
            new SettingMode("BreakMode: ", "All", "Smart"), //4
            new SettingSlider(0.0D, 6.0D, 5.0D, 0, "Range: "), //5
            new SettingSlider(0.0D, 20.0D, 3.0D, 0, "Hit Delay: "), //6
            new SettingSlider(0.0D, 20.0D, 3.0D, 0, "Place Delay: "), //7
            new SettingSlider(1.0D, 36.0D, 6.0D, 0, "MinDMG: "), //8
            new SettingSlider(1.0D, 36.0D, 6.0D, 0, "MaxSelfDMG: "), //9
            new SettingSlider(1.0D, 36.0D, 10.0D, 0, "FacePlace HP: "), //10
            new SettingToggle(false, "auto backdoor"), //11
            new SettingToggle(false, "RenderTarget"), //12
            new SettingToggle(true, "AutoSwitch"), //13
            new SettingToggle(true, "Chat Alert"), //14
            new SettingToggle(true, "speedi") //15
    );
    private final Timer placeTimer;
    private final Timer breakTimer;


    private boolean switchCooldown = false;
    private BlockPos renderTarget = null;
    private final List<EntityPlayer> ezplayers = new ArrayList<>();
    private final List<EntityPlayer> targetPlayers = new ArrayList<>();
    public blu3CrystalAura() { super("blu3CA", 0, Category.EXPERIMENTAL, "absolute shit i say", settings);
        this.placeTimer = new Timer();
        this.breakTimer = new Timer();
    }
    public void onDisable(){
        if (this.getSetting(14).asToggle().state) ClientChat.log("Blu3CA: " + ChatFormatting.RED + "OFF");
        renderTarget = null;
        ezplayers.clear();
        targetPlayers.clear();
    }
    public void onEnable(){
        if (this.getSetting(14).asToggle().state) ClientChat.log("Blu3CA: " + ChatFormatting.AQUA + "ON");
    }
    public void onUpdate() {
        this.doStuff();
        if (this.mc.getDebugFPS() < 2) {
            this.setToggled(false);
            ClientChat.warn("FPS dropped below 2, disbling for safety");
        }
    }
    public void doStuff(){

        switch (this.getSetting(0).asMode().mode){
            case 0: {
                if (this.breakTimer.passedMs((long) this.getSetting(6).asSlider().getValue() * 25L)) {
                    this.breakCrystal();
                    this.breakTimer.reset();
                }
                if (this.placeTimer.passedMs((long) this.getSetting(7).asSlider().getValue() * 25L)) {
                    this.placeCrystal();
                    this.placeTimer.reset();
                }
                break;
            }
            case 1: {
                if (this.placeTimer.passedMs((long) this.getSetting(7).asSlider().getValue() * 25L)) {
                    this.placeCrystal();
                    this.placeTimer.reset();
                }
                if (this.breakTimer.passedMs((long) this.getSetting(6).asSlider().getValue() * 25L)) {
                    this.breakCrystal();
                    this.breakTimer.reset();
                }
                break;
            }
        }
    }
    private void placeCrystal(){
        ezplayers.clear();
        int crystalLimit = 1;
        int crystalSlot;
        crystalSlot = this.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? this.mc.player.inventory.currentItem : -1;
        if (crystalSlot == -1)
        {
            for (int l = 0; l < 9; ++l)
            {
                if (this.mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL)
                {
                    crystalSlot = l;
                    break;
                }
            }
        }
        if (crystalSlot == -1) { return; }
        int minDmg;
        minDmg = (int)this.getSetting(8).asSlider().getValue();
        this.renderTarget = null;
        List<BlockPos> blocks = this.findCrystalBlocks();
        ezplayers.addAll(this.mc.world.playerEntities);
        ezplayers.remove(this.mc.player);
        for (Object o : new ArrayList<>(ezplayers)) {
            Entity e = (EntityPlayer) o;
            if (FriendManager.Get().isFriend(e.getName().toLowerCase())){
                ezplayers.remove(e);
            }
        }
        BlockPos placeTarget = null;
        float highDamage = 0.5f;
        for (BlockPos pos : blocks) {
           final float selfDmg = DamageUtil.calculateDamage(pos, this.mc.player);
           if (selfDmg + 0.5D >= this.mc.player.getHealth() || selfDmg > this.getSetting(9).asSlider().getValue()) {
               continue;
           }
           for (EntityPlayer player : ezplayers) {
               if (player.getHealth() <= this.getSetting(10).asSlider().getValue()){ minDmg = 2; }
               targetPlayers.remove(player);
               if (player.getDistanceSq(pos) < square(this.getSetting(5).asSlider().getValue())){
                   if (!targetPlayers.contains(player)) targetPlayers.add(player);
                   float damage = DamageUtil.calculateDamage(pos, player);
                   if (damage <= selfDmg) continue;
                   if (damage <= minDmg || damage <= highDamage) continue;
                   highDamage = damage;
                   placeTarget = pos;
                   renderTarget = pos;
               }
           }
       }
       if (placeTarget != null){
               if (this.getSetting(13).asToggle().state && this.mc.player.inventory.currentItem != crystalSlot && !eatingGap()) {
                   this.mc.player.inventory.currentItem = crystalSlot;
                   this.switchCooldown = true;
                   return;
               }
           if (this.switchCooldown)
           {
               this.switchCooldown = false;
               return;
           }
               if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemEndCrystal) {
                   if (this.getSetting(1).asMode().mode == 1 || this.getSetting(1).asMode().mode == 3) WorldUtils.rotatePacket(placeTarget.getX(), placeTarget.getY(), placeTarget.getZ());
                   this.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeTarget, EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
               }
           }
       }
    private void breakCrystal(){
        List <EntityEnderCrystal> crystals = new ArrayList<>();
        crystals.clear();
        for (Entity e : this.mc.world.loadedEntityList) {
            if (e instanceof EntityEnderCrystal){
                crystals.add((EntityEnderCrystal) e);
            }
        }
        List<EntityPlayer> players = new ArrayList<>();
        players.clear();
        players.addAll(this.mc.world.playerEntities);
        players.remove(this.mc.player);
        for (Object o : new ArrayList<>(players)) {
            Entity e = (EntityPlayer) o;
            if (FriendManager.Get().isFriend(e.getName().toLowerCase())){
                players.remove(e);
            }
        }
        switch (this.getSetting(4).asMode().mode){
            case 0: {
                EntityEnderCrystal crystal = this.mc.world.loadedEntityList.stream().filter((entityx) -> entityx instanceof EntityEnderCrystal).map((entityx) -> (EntityEnderCrystal) entityx).min(Comparator.comparing((c) -> this.mc.player.getDistance(c))).orElse(null);
                if (crystal != null && (double)this.mc.player.getDistance(crystal) <= this.getSetting(5).asSlider().getValue()) {
                    if (this.getSetting(1).asMode().mode == 2 || this.getSetting(1).asMode().mode == 3) WorldUtils.rotatePacket(crystal.posX, crystal.posY, crystal.posZ);
                    this.mc.playerController.attackEntity(this.mc.player, crystal);
                    swing();
                }
                break;
            }
            case 1: {
                int minDmg;
                minDmg = (int)this.getSetting(8).asSlider().getValue();
                for (EntityPlayer player : players) {
                    for (EntityEnderCrystal crystal : crystals) {
                        final float selfDmg = DamageUtil.calculateDamage(crystal, this.mc.player);
                        if (player.getHealth() <= this.getSetting(10).asSlider().getValue()) {
                            minDmg = 2;
                        }
                        if (selfDmg + 0.5D >= this.mc.player.getHealth() || selfDmg > this.getSetting(9).asSlider().getValue()) {
                            continue;
                        }
                        if (DamageUtil.calculateDamage(crystal, player) >= minDmg ) {
                            if (this.getSetting(1).asMode().mode == 2 || this.getSetting(1).asMode().mode == 3)
                                WorldUtils.rotatePacket(crystal.posX, crystal.posY, crystal.posZ);
                            this.mc.playerController.attackEntity(this.mc.player, crystal);
                            swing();
                        }
                    }
                }
                        break;
            }
        }
    }
    public void swing(){
        switch (this.getSetting(2).asMode().mode) {
            case 0: {
                this.mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            }
            case 1: {
                this.mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            }
            case 2: {
                this.mc.player.swingArm(EnumHand.MAIN_HAND);
                this.mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            }
        }
    }
    public static double square(final double input) {
        return input * input;
    }
    private List<BlockPos> findCrystalBlocks()
    {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(this.getSphere(this.getPlayerPos(), (float) this.getSetting(5).asSlider().getValue(), (int) this.getSetting(5).asSlider().getValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));

        return positions;
    }
    public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y)
    {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; (float) x <= (float) cx + r; ++x)
        {
            for (int z = cz - (int) r; (float) z <= (float) cz + r; ++z)
            {
                for (int y = sphere ? cy - (int) r : cy; (float) y < (sphere ? (float) cy + r : (float) (cy + h)); ++y)
                {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < (double) (r * r) && (!hollow || dist >= (double) ((r - 1.0F) * (r - 1.0F))))
                    {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }
    private boolean eatingGap(){
        return mc.player.getHeldItemMainhand().getItem() instanceof ItemAppleGold && mc.player.isHandActive();
    }
    public BlockPos getPlayerPos()
    {
        return new BlockPos(Math.floor(this.mc.player.posX), Math.floor(this.mc.player.posY), Math.floor(this.mc.player.posZ));
    }
    private boolean canPlaceCrystal(BlockPos blockPos){
        if (this.getSetting(3).asMode().mode == 0) {
            BlockPos boost = blockPos.add(0, 1, 0);
            BlockPos boost2 = blockPos.add(0, 2, 0);
            return (this.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && this.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && this.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && this.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
        } else {
            BlockPos boost = blockPos.add(0, 1, 0);
            return (this.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && this.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && this.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
        }
    }
    public void onRender() {
       if (this.renderTarget != null){
           RenderUtils.drawFilledBlockBox(new AxisAlignedBB(renderTarget), 0, 1, 1, 0.3F);
       }
        if (this.getSetting(12).asToggle().state) {
            for (EntityPlayer e : targetPlayers)
                RenderUtils.drawFilledBlockBox(e.getEntityBoundingBox(), 0.0F, 1.0F, 1.0F, 0.3F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public boolean onPacketRead(Packet<?> packet){
        if (packet instanceof SPacketSoundEffect && this.getSetting(15).asToggle().state) {
            final SPacketSoundEffect bruh = (SPacketSoundEffect) packet;
            if (bruh.getCategory() == SoundCategory.BLOCKS && bruh.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
                    if (e instanceof EntityEnderCrystal) {
                        if (e.getDistance(bruh.getX(), bruh.getY(), bruh.getZ()) <= 6.0f) {
                            e.setDead();
                        }
                    }
                }
            }
        }
        return false;
    }
}