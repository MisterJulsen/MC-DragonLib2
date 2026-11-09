package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.Minecraft;

public class DLAnimator extends GuiComponent {

    @FunctionalInterface
    public interface IAnimatorRenderCallback {
        void execute(DLGuiGraphics g, float currentTicks, int totalTicks, double percentage);
    }

    @FunctionalInterface
    public interface IAnimatorTickCallback {
        void execute(int currentTicks, int totalTicks, double percentage);
    }

    private int maxTicks;
    private int currentTicks;
    private float currentTicksSmooth;
    private boolean running;

    private IAnimatorRenderCallback onAnimateRender;
    private IAnimatorTickCallback onAnimateTick;
    private Runnable onCompleted;

    public DLAnimator() {
        super(0, 0, 0, 0);
    }

    public boolean isRunning() {
        return running;
    }

    public int getTotalTicks() {
        return maxTicks;
    }

    public int getCurrentIntTicks() {
        return currentTicks;
    }

    public float getCurrentTicks() {
        return currentTicksSmooth;
    }

    public double getPercentage() {
        return 1D / (double)maxTicks * currentTicksSmooth;
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        if (!running) {
            return;
        }

        float frameTime = Minecraft.getInstance().getDeltaFrameTime();
        currentTicksSmooth += frameTime;
        DLUtils.doIfNotNull(onAnimateRender, x -> x.execute(g, getCurrentTicks(), getTotalTicks(), getPercentage()));
        if (currentTicks >= maxTicks) {
            stop();
            DLUtils.doIfNotNull(onCompleted, Runnable::run);
        }
    }

    @Override
    public void tick() {
        if (running) {
            DLUtils.doIfNotNull(onAnimateTick, x -> x.execute(getCurrentIntTicks(), getTotalTicks(), getPercentage()));
            currentTicks++;
            currentTicksSmooth = currentTicks;
            if (currentTicks >= maxTicks) {
                currentTicksSmooth = maxTicks;
                stop();
                DLUtils.doIfNotNull(onCompleted, Runnable::run);
            }
        }
    }

    public void start(int ticks, IAnimatorRenderCallback renderCallback, IAnimatorTickCallback tickCallback, Runnable onCompleted) {
        this.currentTicks = 0;
        this.currentTicksSmooth = 0;
        this.maxTicks = ticks;
        this.onAnimateRender = renderCallback;
        this.onAnimateTick = tickCallback;
        this.onCompleted = onCompleted;
        this.running = true;
    }

    public void stop() {
        this.running = false;
        this.currentTicks = 1;
        this.currentTicksSmooth = 1;
        this.maxTicks = 1;
    }
}