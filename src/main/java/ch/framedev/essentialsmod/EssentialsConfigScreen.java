package ch.framedev.essentialsmod;

import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
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

        // Checkbox for enabling warps
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 40, 200, 20,
                new TextComponent("Enable Warps: " + (EssentialsConfig.enableWarps.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableWarps.get();
                    EssentialsConfig.enableWarps.set(newValue);
                    button.setMessage(new TextComponent("Enable Warps: " + (newValue ? "On" : "Off")));
                }));

        // Checkbox for enabling the Back command
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 70, 200, 20,
                new TextComponent("Enable Back Command: " + (EssentialsConfig.useBack.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.useBack.get();
                    EssentialsConfig.useBack.set(newValue);
                    button.setMessage(new TextComponent("Enable Back Command: " + (newValue ? "On" : "Off")));
                }));

        // Checkbox for enabling limited homes
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 100, 200, 20,
                new TextComponent("Enable Limited Homes: " + (EssentialsConfig.enableLimitedHomes.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.enableLimitedHomes.get();
                    EssentialsConfig.enableLimitedHomes.set(newValue);
                    button.setMessage(new TextComponent("Enable Limited Homes: " + (newValue ? "On" : "Off")));
                }));

        // Slider for limitForHomes
        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, this.height / 6 + 130, 200, 20,
                new TextComponent("Limit for Homes: " + EssentialsConfig.limitForHomes.get()),
                EssentialsConfig.limitForHomes.get() / 100.0) { // Normalize to 0.0 - 1.0 range
            @Override
            protected void updateMessage() {
                int limit = (int) (this.value * 100); // Scale back to 1 - 100 range
                this.setMessage(new TextComponent("Limit for Homes: " + limit));
            }

            @Override
            protected void applyValue() {
                int limit = (int) (this.value * 100); // Scale back to 1-100 range
                EssentialsConfig.limitForHomes.set(limit);
            }
        });

        // Checkbox for enabling Mute Player for themselves.
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 130, 200, 20,
                new TextComponent("Enable Mute Other Player for themselves: " + (EssentialsConfig.muteOtherPlayeForSelf.get() ? "On" : "Off")),
                button -> {
                    boolean newValue = !EssentialsConfig.muteOtherPlayeForSelf.get();
                    EssentialsConfig.muteOtherPlayeForSelf.set(newValue);
                    button.setMessage(new TextComponent("Enable Mute Other Player for themselves: " + (newValue ? "On" : "Off")));
                }));


        // "Done" button to return to the parent screen
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 160, 200, 20,
                new TextComponent("Done"), button -> {
            // Save the configuration when done
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