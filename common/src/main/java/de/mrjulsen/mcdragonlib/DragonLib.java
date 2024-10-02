package de.mrjulsen.mcdragonlib;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;

import de.mrjulsen.mcdragonlib.client.OverlayManager;
import de.mrjulsen.mcdragonlib.client.gui.DLOverlayScreen;
import de.mrjulsen.mcdragonlib.internal.ClientWrapper;
import de.mrjulsen.mcdragonlib.internal.DragonLibBlock;
import de.mrjulsen.mcdragonlib.internal.DragonLibBlockEntity;
import de.mrjulsen.mcdragonlib.net.builtin.IdentifiableResponsePacketBase;
import de.mrjulsen.mcdragonlib.net.NetworkManagerBase;
import de.mrjulsen.mcdragonlib.net.builtin.WritableSignPacket;
import de.mrjulsen.mcdragonlib.util.ScheduledTask;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.accessor.BasicDataAccessorPacket;
import de.mrjulsen.mcdragonlib.util.accessor.DataAccessorResponsePacket;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonLib {

    public static final String MODID = "dragonlib";
	public static final String MOD_NAME = "DragonLib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    
    public static final Random RANDOM = new Random();
    public static final Gson GSON = new Gson();
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    
    public static final int TICKS_PER_DAY = Level.TICKS_PER_DAY;
    public static final int TICKS_PER_INGAME_HOUR = Level.TICKS_PER_DAY / 24;
    public static final int DAYTIME_SHIFT = 6000;
    public static final byte TPS = 1000 / MinecraftServer.MS_PER_TICK;
    public static final int TICKS_PER_REAL_LIFE_DAY = 86400 * TPS;
	/** One block pixel */ public static final float PIXEL = 1.0F / 16.0F;

    public static final ResourceLocation UI = new ResourceLocation(MODID, "textures/gui/ui.png");
    public static final ResourceLocation NATIVE_WIDGETS = new ResourceLocation("minecraft:textures/gui/widgets.png"); 

    public static final int NATIVE_UI_FONT_COLOR = 0xFF404040;
    public static final int NATIVE_BUTTON_FONT_COLOR_ACTIVE = 0xFFFFFFFF;
    public static final int NATIVE_BUTTON_FONT_COLOR_DISABLED = 0xFF9E9E9E;
    public static final int NATIVE_BUTTON_FONT_COLOR_HIGHLIGHT = 0xFFFFFFA0;
    public static final int DARK_WINDOW_COLOR = 0xFF303030;
    public static final int DEFAULT_BUTTON_COLOR = 0xFF484848;
    public static final int LIGHT_BUTTON_COLOR = 0xFF888888;
    public static final int PRIMARY_BUTTON_COLOR = 0xFF1572E6;//0xFF2190ff;
    public static final int ACCEPT_BUTTON_COLOR = 0xFF0DB24D;
    public static final int ERROR_BUTTON_COLOR = 0xFFE83E4D;
    public static final int WARN_BUTTON_COLOR = 0xFFE8BD3E;
    
    /** 🐉 */ public static final Component TEXT_DRAGON = TextUtils.translate("text." + MODID + ".dragon");
    public static final Component TEXT_NEXT = TextUtils.translate("text." + MODID + ".next");
    public static final Component TEXT_PREVIOUS = TextUtils.translate("text." + MODID + ".previous");
    public static final Component TEXT_GO_BACK = TextUtils.translate("text." + MODID + ".go_back");
    public static final Component TEXT_GO_FORTH = TextUtils.translate("text." + MODID + ".go_forth");    
    public static final Component TEXT_GO_UP = TextUtils.translate("text." + MODID + ".go_down");
    public static final Component TEXT_GO_DOWN = TextUtils.translate("text." + MODID + ".go_up");
    public static final Component TEXT_GO_RIGHT= TextUtils.translate("text." + MODID + ".go_right");
    public static final Component TEXT_GO_LEFT = TextUtils.translate("text." + MODID + ".go_left");
    public static final Component TEXT_GO_TO_TOP = TextUtils.translate("text." + MODID + ".go_to_top");
    public static final Component TEXT_GO_TO_BOTTOM = TextUtils.translate("text." + MODID + ".go_to_bottom");
    public static final Component TEXT_RESET_DEFAULTS = TextUtils.translate("text." + MODID + ".reset_defaults");
    public static final Component TEXT_EXPAND = TextUtils.translate("text." + MODID + ".expand");
    public static final Component TEXT_COLLAPSE = TextUtils.translate("text." + MODID + ".collapse");
    public static final Component TEXT_COUNT = TextUtils.translate("text." + MODID + ".count");
    public static final Component TEXT_TRUE = TextUtils.translate("text." + MODID + ".true");
    public static final Component TEXT_FALSE = TextUtils.translate("text." + MODID + ".false");
    public static final Component TEXT_CLOSE = TextUtils.translate("text." + MODID + ".close");
    public static final Component TEXT_SHOW = TextUtils.translate("text." + MODID + ".show");
    public static final Component TEXT_HIDE = TextUtils.translate("text." + MODID + ".hide");
    public static final Component TEXT_SEARCH = TextUtils.translate("text." + MODID + ".search");
    public static final Component TEXT_REFRESH = TextUtils.translate("text." + MODID + ".refresh");
    public static final Component TEXT_RELOAD = TextUtils.translate("text." + MODID + ".reload");

    private static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MODID));    
    private static final Registrar<Item> ITEMS = REGISTRIES.get().get(Registry.ITEM_REGISTRY);        
    private static final Registrar<Block> BLOCKS = REGISTRIES.get().get(Registry.BLOCK_REGISTRY);
    private static final Registrar<BlockEntityType<?>> BLOCK_ENTITIES = REGISTRIES.get().get(Registry.BLOCK_ENTITY_TYPE_REGISTRY);

    /** A sample block which is added by DragonLib to test stuff. Does nothing by default and can safely be used in your world. Think of it as a small ~~easter~~ dragon egg. 🐉*/
    public static final RegistrySupplier<Block> DRAGON_BLOCK = registerBlock("dragon", () -> new DragonLibBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f)));
    public static final RegistrySupplier<BlockEntityType<DragonLibBlockEntity>> DRAGONLIB_BLOCK_ENTITY = BLOCK_ENTITIES.register(new ResourceLocation(MODID, "dragonlib_block_entity"), () -> BlockEntityType.Builder.of(DragonLibBlockEntity::new, DragonLib.DRAGON_BLOCK.get()).build(null));
        
    private static NetworkManagerBase dragonLibNet;
    private static MinecraftServer currentServer;

    private static <T extends Block, I extends BlockItem>RegistrySupplier<T> registerBlock(String name, Supplier<T> block) {
        RegistrySupplier<T> toReturn = BLOCKS.register(new ResourceLocation(MODID, name), block);
        registerBlockItem(name, toReturn, DragonLibBlock.DragonLibItem.class);
        return toReturn;
    }

    private static <T extends Block, I extends BlockItem>RegistrySupplier<Item> registerBlockItem(String name, RegistrySupplier<T> block, Class<I> blockItemClass) {
        return ITEMS.register(new ResourceLocation(MODID, name), () -> {
            try {
                return blockItemClass.getDeclaredConstructor(Block.class, Item.Properties.class).newInstance(block.get(), new Item.Properties());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error("Unable to register block item '" + name + "'.", e);
                return new BlockItem(block.get(), new Item.Properties());
            }
        });
    }

    
    
    /**
     * DO NOT CALL THIS METHOD FROM OTHER MODS!
     */
    @SuppressWarnings("resource")
    public static void init() {
        dragonLibNet = new NetworkManagerBase(MODID, "dragonlib_network", List.of(
            IdentifiableResponsePacketBase.class, 
            WritableSignPacket.class,
            DataAccessorResponsePacket.class
        ));
        registerCustom(BasicDataAccessorPacket.class);

        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientTickEvent.CLIENT_POST.register((Minecraft mc) -> {
                NetworkManagerBase.callbackListenerTick();
                OverlayManager.tickAll();
            });
            

            // Overlay Renderer
            ClientGuiEvent.RENDER_HUD.register((poseStack, partialTicks) -> {
                if (Minecraft.getInstance().font == null) {
                    return;
                }
                OverlayManager.renderAll(poseStack, partialTicks);
            });

            ClientRawInputEvent.KEY_PRESSED.register((mc, keyCode, scanCode, action, modifiers) -> {
                for (DLOverlayScreen overlay : OverlayManager.getAllOverlays()) {
                    if (overlay.keyPressed(keyCode, scanCode, modifiers)) {
                        return EventResult.interruptTrue();
                    }
                }
                return EventResult.pass();
            });

            ClientRawInputEvent.MOUSE_CLICKED_POST.register((mc, mouseX, mouseY, button) -> {
                for (DLOverlayScreen overlay : OverlayManager.getAllOverlays()) {
                    if (overlay.mouseClicked(mouseX, mouseY, button)) {
                        return EventResult.interruptTrue();
                    }
                }
                return EventResult.pass();
            });

            ClientRawInputEvent.MOUSE_SCROLLED.register((mc, scrollDelta) -> {
                for (DLOverlayScreen overlay : OverlayManager.getAllOverlays()) {
                    if (overlay.mouseScrolled((int)Minecraft.getInstance().mouseHandler.xpos(), (int)Minecraft.getInstance().mouseHandler.ypos(), scrollDelta)) {
                        return EventResult.interruptTrue();
                    }
                }
                return EventResult.pass();
            });
        }

        // On server tick
        TickEvent.Server.SERVER_POST.register((server) -> {            
            ScheduledTask.runScheduledTasks();
        });

        LifecycleEvent.SERVER_STARTED.register((server) -> {
            DragonLib.currentServer = server;
        });

        LifecycleEvent.SERVER_STOPPED.register((server) -> {
            DragonLib.currentServer = null;
        });

        // On Server stop
        LifecycleEvent.SERVER_STOPPING.register((server) -> {
            ScheduledTask.cancelAllTasks();
        });        
        /*
        ClientLifecycleEvent.CLIENT_SETUP.register(mc -> {
            BlockEntityRendererRegistry.register(DRAGONLIB_BLOCK_ENTITY.get(), DragonLibBlockEntityRenderer::new);
        });
        */

        // After loading
        printDraconicWelcomeMessage();
    }

    /**
     * @return The network manager of the DragonLib mod. Please use your own network manager in your mod. this is intended for DraagonLib's internal stuff.
     */
    public static final NetworkManagerBase getDragonLibNetworkManager() {
        return dragonLibNet;
    }

    public static boolean hasServer() {
        return currentServer != null;
    }

    public static Optional<MinecraftServer> getCurrentServer() {
        return Optional.ofNullable(currentServer);
    }

    public static Level getPhysicalLevel() {
        return hasServer() ? getCurrentServer().get().overworld() : ClientWrapper.getClientLevel();
    }

    public static long getCurrentWorldTime() {
        Level level = getPhysicalLevel();
        return level == null ? 0 : level.getDayTime();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void registerCustom(Class<BasicDataAccessorPacket> c) {
        try {
            BasicDataAccessorPacket packet = c.getConstructor().newInstance();
            getDragonLibNetworkManager().CHANNEL.register(c, packet::encode, (buf) -> (BasicDataAccessorPacket)packet.decode(buf), packet::handle);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            DragonLib.LOGGER.error("Unable to register packet.", e);
        }
    }

    /**
     * Why 🐲? Because I can. Let me bee 🐝
     * @since 1.0
     * @author MrJulsen
     * @see 🐉
     */
    private static final void printDraconicWelcomeMessage() {
        String[] dragonTypes = {"Dragon", "Fire Dragon", "Ice Dragon", "Lightning Dragon", "Mountain Dragon", "Poison Dragon", "Drake", "Wyvern", "Wyrm", "MrJulsen", "Toothless", "Drogon", "Smaug", "Ender Dragon", "Spyro", "Do you think dragons exist?"};
        final int charCount = 66;

        StringBuilder ver = new StringBuilder();
        ver.append("Minecraft ");
        ver.append(Platform.isForge() ? "Forge" : (Platform.isFabric() ? "Fabric" : ""));
        ver.append(" ");
        ver.append(Platform.getMinecraftVersion());
        if (Platform.isDevelopmentEnvironment()) {
            ver.append(" (Dev)");
        } 
        
        int verLength = ver.length();
        for (int i = 0; i < (charCount - verLength) / 2; i++) {
            ver.insert(0, " ");
            ver.append(" ");
        }

        new Thread(() -> {
            LOGGER.info("                           +++ 🐉 +++                             ");
            LOGGER.info("------------------------------------------------------------------");
            LOGGER.info("                  Loaded DRAGONLIB by MRJULSEN!                   ");
            LOGGER.info(ver.toString());
            LOGGER.info("             Discord: https://discord.gg/AeSbNgvc7f               ");
            LOGGER.info("      GitHub: https://github.com/MisterJulsen/MC-DragonLib2       ");
            LOGGER.info(" Bug Reports: https://github.com/MisterJulsen/MC-DragonLib2/issues");
            LOGGER.info("------------------------------------------------------------------");
            LOGGER.info("                           +++ 🐉 +++                             ");
        }, dragonTypes[RANDOM.nextInt(dragonTypes.length)]).start();
    }
}
