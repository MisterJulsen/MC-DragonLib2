package de.mrjulsen.mcdragonlib.client.newgui.test;

import de.mrjulsen.mcdragonlib.client.newgui.builtin.DLColorPickerWindow;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.util.Color;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.world.entity.decoration.ArmorStand;

public class RedWindow extends DLWindow {
    ArmorStand armorStandPreview;

    public RedWindow(DLWindowManager manager) {
        super(manager);
        //windowSpawnPosition.set(WindowPosition.CENTER);
        setPosition(50, 30);
        setSize(200, 200);



        addEventListener(DLGuiStandardEvents.LayoutUpdateEvent.class, (s, e) -> {
            
            return false;
        });



        
        DLButton btn = new DLButton(width() - 20, 0, 20, 20);
        btn.text.set(TextUtils.text("×"));
        btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            manager.closeWindow(this);
            return false;
        });
        addComponent(btn);

        DLButton btn2 = new DLButton(width() - 40, 0, 20, 20);
        btn2.text.set(TextUtils.text("⬜"));
        btn2.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> {
                DLColorPickerWindow z = new DLColorPickerWindow(mgr, false, Color.UNDEFINED, (c) -> {});
                return z;
            });
            return false;
        });
        addComponent(btn2);

        DLButton btn3 = new DLButton(width() - 60, 0, 20, 20);
        btn3.text.set(TextUtils.text("—"));
        btn3.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            return false;
        });
        addComponent(btn3);
    }

    

    @Override
    public void renderMainLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite("window_rounded").render(graphics, 0, 0, width(), height());

        /*
        GuiUtils.renderItem(graphics, new ItemStack(Items.DIAMOND, 5), 2, 2, 2, true);

        Quaternionf quat = new Quaternionf();
        quat.rotateXYZ((float)Math.toRadians(((double)System.currentTimeMillis() / 20d) % 360), (float)Math.toRadians(((double)System.currentTimeMillis() / 20d) % 360), (float)Math.toRadians(((double)System.currentTimeMillis() / 20d) % 360));
        Vector3f pivot = new Vector3f(-0.5f);

        Matrix4f matrix = new Matrix4f()
            .translate(pivot.negate(new Vector3f()))
            .rotate(quat)
            .translate(pivot);
        
        GuiUtils.renderBlockState(graphics, 102, 52, 2, Blocks.CRAFTING_TABLE.defaultBlockState(), RenderType.solid(), matrix, LightTexture.FULL_BRIGHT);

        if (Minecraft.getInstance().player != null) {
            GuiUtils.renderEntityFollowingMouse(graphics, 50, 100, 2, (float)getWindowManager().mouseXOnScreen(), (float)getWindowManager().mouseYOnScreen(), Minecraft.getInstance().player, LightTexture.FULL_BRIGHT);
        }
            */
    }
        
    
    
}
