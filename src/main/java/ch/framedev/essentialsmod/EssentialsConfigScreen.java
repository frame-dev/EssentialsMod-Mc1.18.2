package ch.framedev.essentialsmod;

import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

public class EssentialsConfigScreen extends Screen {

    private final Screen parent;

    public EssentialsConfigScreen(Screen parent) {
        super(new TextComponent("Essentials Mod Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        int startY = this.height / 6;
        int centerX = this.width / 2 - buttonWidth / 2;

        // Add buttons
        int currentY = startY;

        // Enable Warps
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Warps: " + (EssentialsConfig.enableWarps.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableWarps.get();
                    EssentialsConfig.enableWarps.set(newValue);
                    button.setMessage(new TextComponent("Enable Warps: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Enable Back Command
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Back Command: " + (EssentialsConfig.useBack.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.useBack.get();
                    EssentialsConfig.useBack.set(newValue);
                    button.setMessage(new TextComponent("Enable Back Command: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Enable Limited Homes
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Limited Homes: " + (EssentialsConfig.enableLimitedHomes.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableLimitedHomes.get();
                    EssentialsConfig.enableLimitedHomes.set(newValue);
                    button.setMessage(new TextComponent("Enable Limited Homes: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Limit for Homes Slider
        this.addRenderableWidget(new AbstractSliderButton(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Limit for Homes: " + EssentialsConfig.limitForHomes.get()),
                EssentialsConfig.limitForHomes.get() / 100.0) {

            @Override
            protected void updateMessage() {
                int limit = (int) (this.value * 100);
                this.setMessage(new TextComponent("Limit for Homes: " + limit));
            }

            @Override
            protected void applyValue() {
                int limit = (int) (this.value * 100);
                EssentialsConfig.limitForHomes.set(limit);
            }
        });
        currentY += spacing;

        // Enable Mute Other Player for Themselves
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Mute Other Player for Themselves: " + (EssentialsConfig.muteOtherPlayerForSelf.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.muteOtherPlayerForSelf.get();
                    EssentialsConfig.muteOtherPlayerForSelf.set(newValue);
                    button.setMessage(new TextComponent("Enable Mute Other Player for Themselves: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Enable Backpack
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Backpack: " + (EssentialsConfig.enableBackPack.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableBackPack.get();
                    EssentialsConfig.enableBackPack.set(newValue);
                    button.setMessage(new TextComponent("Enable Backpack: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Enable BackpackConfigSave
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Backpack Config Save: " + (EssentialsConfig.enableBackPackSaveInConfig.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableBackPackSaveInConfig.get();
                    EssentialsConfig.enableBackPackSaveInConfig.set(newValue);
                    button.setMessage(new TextComponent("Enable Backpack Config Save: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Enable Signs Events
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Enable Signs events as example [FREE]: " + (EssentialsConfig.enableSigns.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableSigns.get();
                    EssentialsConfig.enableSigns.set(newValue);
                    button.setMessage(new TextComponent("Enable Signs events as example [FREE]: " + (newValue ? "On" : "Off")));
                }));
        currentY += spacing;

        // Done Button
        this.addRenderableWidget(new Button(centerX, currentY, buttonWidth, buttonHeight,
                new TextComponent("Done"), button -> {
            EssentialsConfig.COMMON_CONFIG.save();
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Mod.EventBusSubscriber(modid = "essentials", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EssentialsModClient {

        public static void registerConfigGui() {
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                    () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> new EssentialsConfigScreen(parent)));
        }
    }
}
