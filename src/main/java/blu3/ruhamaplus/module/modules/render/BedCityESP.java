package blu3.ruhamaplus.module.modules.render;

import blu3.ruhamaplus.module.Category;
import blu3.ruhamaplus.module.Module;
import blu3.ruhamaplus.utils.RenderUtils;
import blu3.ruhamaplus.utils.friendutils.FriendManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BedCityESP extends Module {
    public BedCityESP() {
        super("BedCityESP", 0, Category.RENDER, "i can see you", null);
    }

    private EntityPlayer target;

    private List <BlockPos> clearSpots = new ArrayList<>();

    private BlockPos blockpos1;
    private BlockPos blockpos2;
    private BlockPos blockpos3;
    private BlockPos blockpos4;
    private BlockPos blockpos5;
    private BlockPos blockpos6;
    private BlockPos blockpos7;
    private BlockPos blockpos8;

    public boolean isInBlockRange(Entity target) {
        return target.getDistance(this.mc.player) <= 15.0F;
    }

    public boolean isValid(EntityPlayer entity) {
        return entity != null && this.isInBlockRange(entity) && entity.getHealth() > 0.0F && !entity.isDead;
    }

    public void onUpdate(){
        this.clearSpots.clear();
        if (!this.mc.player.isHandActive()) {
            if (!this.isValid(this.target) || this.target == null) {
                this.updateTarget();
            }

            List<Entity> entities = new ArrayList<>();

            entities.addAll(this.mc.world.playerEntities);
            for (Object o : new ArrayList<>(entities)) {
                Entity e = (EntityPlayer) o;

                if (FriendManager.Get().isFriend(e.getName().toLowerCase())){
                    entities.remove(e);
                }
            }
            Iterator<Entity> entityIter = entities.iterator();

            EntityPlayer player;
                do {

                    if (!entityIter.hasNext()) {
                        if (this.isValid(this.target) && this.mc.player.getDistance(this.target) < 160) {
                                this.showClearSpots(this.target);
                        }
                        return;
                    }
                    player = (EntityPlayer) entityIter.next();


                } while (player instanceof EntityPlayerSP || !this.isValid(player) || player.getDistance(this.mc.player) >= this.target.getDistance(this.mc.player));

                this.target = player;

        }
    }

    private void showClearSpots(EntityPlayer player) {



        List <BlockPos> poses = new ArrayList<>();

        this.blockpos1 = new BlockPos(player.posX + 1, player.posY + 1.0D, player.posZ); poses.add(this.blockpos1);
        this.blockpos2 = new BlockPos(player.posX - 1, player.posY + 1.0D, player.posZ); poses.add(this.blockpos2);
        this.blockpos3 = new BlockPos(player.posX, player.posY + 1.0D, player.posZ + 1); poses.add(this.blockpos3);
        this.blockpos4 = new BlockPos(player.posX, player.posY + 1.0D, player.posZ - 1); poses.add(this.blockpos4);
        this.blockpos5 = new BlockPos(player.posX + 1, player.posY + 2.0D, player.posZ); poses.add(this.blockpos5);
        this.blockpos6 = new BlockPos(player.posX - 1, player.posY + 2.0D, player.posZ); poses.add(this.blockpos6);
        this.blockpos7 = new BlockPos(player.posX, player.posY + 2.0D, player.posZ + 1); poses.add(this.blockpos7);
        this.blockpos8 = new BlockPos(player.posX, player.posY + 2.0D, player.posZ - 1); poses.add(this.blockpos8);



        for (Object o : new ArrayList<>(poses))
        {
            BlockPos b = (BlockPos) o;
            poses.add(0, b);
        }
        for (BlockPos b : poses)
        {
            if (this.mc.world.getBlockState(b).getMaterial().isReplaceable()){
                        clearSpots.add(b);
            }
        }
    }

    public void onRender() {

        for (BlockPos p : this.clearSpots)
        {
            RenderUtils.drawFilledBlockBox(new AxisAlignedBB(p), 1, 0, 0, 0.25F);
        }
    }

    public void updateTarget() {
        for (EntityPlayer player : this.mc.world.playerEntities) {
            if (!(player instanceof EntityPlayerSP) && this.isValid(player)) {
                this.target = player;
            }
        }
    }
}
