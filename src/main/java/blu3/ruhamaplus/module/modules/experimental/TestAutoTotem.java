package blu3.ruhamaplus.module.modules.experimental;

import blu3.ruhamaplus.module.Category;
import blu3.ruhamaplus.module.Module;
import blu3.ruhamaplus.settings.SettingBase;
import blu3.ruhamaplus.settings.SettingSlider;
import blu3.ruhamaplus.settings.SettingToggle;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;

import java.util.Arrays;
import java.util.List;

public class TestAutoTotem extends Module
{
    private static final List<SettingBase> settings = Arrays.asList(new SettingToggle(true, "Offhand"), new SettingToggle(false, "Hotbar"), new SettingToggle(true, "Delay"), new SettingSlider(0.0D, 2.0D, 0.25D, 3, "Delay: "));
    private long time = 0L;

    public TestAutoTotem()
    {
        super("TestAutoTotem", 0, Category.EXPERIMENTAL, "Automatically places totems in your offhand, test for 2b bypass", settings);
    }

    double xSpeed;
    double ySpeed;
    double zSpeed;

    public void onUpdate()
    {
        if (!this.getSettings().get(2).toToggle().state || (double) (System.currentTimeMillis() - this.time) >= this.getSettings().get(3).toSlider().getValue() * 1000.0D)
        {
            this.time = System.currentTimeMillis();

            if (this.mc.currentScreen == null || !(this.mc.currentScreen instanceof GuiHopper))
            {
                int i;

                if (this.getSettings().get(0).toToggle().state && this.mc.player.getHeldItemOffhand().getItem() == Items.AIR)
                {
                    for (i = 9; i <= 44; ++i)
                    {
                        if (this.mc.player.inventoryContainer.getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING)
                        {
                            this.mc.playerController.windowClick(this.mc.player.inventoryContainer.windowId, i, 0, ClickType.PICKUP, this.mc.player);
                            this.mc.playerController.windowClick(this.mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, this.mc.player);
                        }
                    }
                    this.mc.player.motionX = 0;
                    this.mc.player.motionY = 0;
                    this.mc.player.motionZ = 0;
                }

                if (this.getSettings().get(1).toToggle().state) {

                    if (this.mc.player.inventory.getStackInSlot(0).getItem() == Items.TOTEM_OF_UNDYING) {
                        return;
                    }

                    for (i = 9; i < 35; ++i) {
                        if (this.mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                            this.mc.playerController.windowClick(this.mc.player.inventoryContainer.windowId, i, 0, ClickType.SWAP, this.mc.player);

                            break;
                        }
                    }
                    this.mc.player.motionX = 0;
                    this.mc.player.motionY = 0;
                    this.mc.player.motionZ = 0;
                }
            }
        }
    }
}

