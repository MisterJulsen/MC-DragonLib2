package de.mrjulsen.mcdragonlib;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.commands.DebugCommand;
import de.mrjulsen.mcdragonlib.internal.*;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType;
import de.mrjulsen.mcdragonlib.network.NetworkThreadPool;
import de.mrjulsen.mcdragonlib.network.builtin.WritableSignPacketData;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.ScheduledTask;
import de.mrjulsen.mcdragonlib.util.time.datapack.TimeSystemDatapackLoader;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonLib {

    public static final String MODID = "dragonlib";
	public static final String MOD_NAME = "DragonLib";
    public static final String MRJULSEN_DISCORD = "https://discord.mrjulsen.net";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    
    public static final Random RANDOM = new Random();
    public static final Gson GSON = new Gson();
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();

	/** One block pixel */ public static final float BLOCK_PIXEL = 1.0F / 16.0F;

    public static final ResourceLocation DRAGONLIB_UI = DLUtils.resourceLocation(MODID, "textures/gui/ui.png");
    public static final ResourceLocation VANILLA_WIDGETS = DLUtils.resourceLocation("minecraft:textures/gui/widgets.png"); 

    public static final DLColor VANILLA_UI_FONT_COLOR = DLColor.fromInt(0xFF404040);
    public static final DLColor VANILLA_BUTTON_ACTIVE_FONT_COLOR = DLColor.WHITE;
    public static final DLColor VANILLA_BUTTON_DISABLED_FONT_COLOR = DLColor.fromInt(0xFF9E9E9E);
    public static final DLColor VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR = DLColor.fromInt(0xFFFFFFA0);

    public static final DLColor WINDOW_COLOR_DARK = DLColor.fromInt(0xFF303030);
    public static final DLColor BUTTON_COLOR_DEFAULT_DARK = DLColor.fromInt(0xFF484848);
    public static final DLColor BUTTON_COLOR_DEFAULT_LIGHT = DLColor.fromInt(0xFF888888);
    public static final DLColor BUTTON_COLOR_PRIMARY = DLColor.fromInt(0xFF1572E6);//0xFF2190ff;
    public static final DLColor BUTTON_COLOR_ACCEPT = DLColor.fromInt(0xFF0DB24D);
    public static final DLColor BUTTON_COLOR_CANCEL = DLColor.fromInt(0xFFE83E4D);
    public static final DLColor BUTTON_COLOR_CAUTION = DLColor.fromInt(0xFFE8BD3E);
    

    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MODID)); 
    private static final Registrar<Item> ITEMS = MANAGER.get().get(Registries.ITEM);        
    private static final Registrar<Block> BLOCKS = MANAGER.get().get(Registries.BLOCK);
    private static final Registrar<BlockEntityType<?>> BLOCK_ENTITIES = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);

    /** A sample block which is added by DragonLib to test stuff. Does nothing by default and can safely be used in your world. Think of it as a small ~~easter~~ dragon egg. 🐉*/
    public static final RegistrySupplier<Block> DRAGON_BLOCK = registerBlock("dragon", () -> new DragonLibBlock(BlockBehaviour.Properties.of().strength(1.5f)));
    public static final RegistrySupplier<BlockEntityType<DragonLibBlockEntity>> DRAGONLIB_BLOCK_ENTITY = BLOCK_ENTITIES.register(DLUtils.resourceLocation(MODID, "dragonlib_block_entity"), () -> BlockEntityType.Builder.of(DragonLibBlockEntity::new, DragonLib.DRAGON_BLOCK.get()).build(null));

    public static final DLNetworkManager DRAGONLIB_NETWORK = new DLNetworkManager(DLUtils.resourceLocation(MODID, "network"), "14");
    public static final NetworkPacketType.Send<NetworkDirection.C2S, WritableSignPacketData> UPDATE_SIGN_TEXT = DRAGONLIB_NETWORK.registerSendOnlyPacket("update_writable_sign", NetworkDirection.C2S, WritableSignPacketData::handler, WritableSignPacketData::new) ;
    
    private static MinecraftServer currentServer;

    private static <T extends Block, I extends BlockItem>RegistrySupplier<T> registerBlock(String name, Supplier<T> block) {
        RegistrySupplier<T> toReturn = BLOCKS.register(DLUtils.resourceLocation(MODID, name), block);
        registerBlockItem(name, toReturn, DragonLibBlock.DragonLibItem.class);
        return toReturn;
    }

    private static <T extends Block, I extends BlockItem>RegistrySupplier<Item> registerBlockItem(String name, RegistrySupplier<T> block, Class<I> blockItemClass) {
        return ITEMS.register(DLUtils.resourceLocation(MODID, name), () -> {
            try {
                return blockItemClass.getDeclaredConstructor(Block.class, Item.Properties.class).newInstance(block.get(), new Item.Properties());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error("Unable to register block item '" + name + "'.", e);
                return new BlockItem(block.get(), new Item.Properties());
            }
        });
    }

    private static boolean initialized = false;
    
    public static ShaderInstance EXAMPLE_SHADER;
    
    /**
     * DO NOT CALL THIS METHOD FROM OTHER MODS!
     */
    public static void init() {
        if (initialized) {
            // You shall not pass!
            throw new IllegalAccessError("Prohibited to init DragonLib manually!");
        }
        initialized = true;

        DragonLibCrossPlatform.registerConfig();        
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new TimeSystemDatapackLoader());
        NetworkTest.init();
        //ModMenuTypes.register();

        

        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(mc -> {
                BlockEntityRendererRegistry.register(DRAGONLIB_BLOCK_ENTITY.get(), DragonLibBlockEntityRenderer::new);
                /*                
                MenuScreens.register(ModMenuTypes.PLAYER_INVENTORY.get(), (PlayerInventoryContainerMenu.Base menu, Inventory inventory, Component title) -> {
                    DLScreenWrapper<PlayerInventoryContainerMenu.Base> wrapper = new DLScreenWrapper<>(menu, DLPlayerInventoryWindow::new);
                    return wrapper;
                });
                */
            });
            DLOverlayManager.init();

            ClientLifecycleEvent.CLIENT_STARTED.register((mc) -> {
                NetworkThreadPool.init();
            });

            ClientLifecycleEvent.CLIENT_STOPPING.register((mc) -> {
                NetworkThreadPool.shutdown();
            });

            //DLBlockModelRegistry.registerForBlock(DRAGON_BLOCK, TestModel::new, TestModel::new);
        }

        // On server tick
        TickEvent.Server.SERVER_POST.register((server) -> {           
            ScheduledTask.runScheduledTasks();
        });

        LifecycleEvent.SERVER_STARTED.register((server) -> {
            DragonLib.currentServer = server;
            NetworkThreadPool.init();
        });

        LifecycleEvent.SERVER_STOPPED.register((server) -> {
            DragonLib.currentServer = null;
        });

        // On Server stop
        LifecycleEvent.SERVER_STOPPING.register((server) -> {
            ScheduledTask.cancelAllTasks();
            NetworkThreadPool.shutdown();
        }); 

        CommandRegistrationEvent.EVENT.register((dispatcher, context, selection) -> {
            DebugCommand.register(dispatcher, selection);
        });

        // After loading
        printDraconicWelcomeMessage();
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

    /**
     * Why 🐲? Because I can. Let me bee 🐝
     * @since 1.0
     * @author MrJulsen
     * @see 🐉
     */
    private static final void printDraconicWelcomeMessage() {
        String[] dragonTypes = {
            "Dragon",
            "Fire Dragon",
            "Ice Dragon",
            "Lightning Dragon",
            "Mountain Dragon",
            "Poison Dragon",
            "Drake",
            "Wyvern",
            "MrJulsen",
            "Toothless",
            "Drogon",
            "Smaug",
            "Ender Dragon",
            "Do you think dragons exist?",
            "Here be Dragons!"
        };
        LOGGER.info("Starting the setup of DragonLib...");
        new Thread(() -> {
            Mod mod = Platform.getMod(MODID);
            List<String> lines = new ArrayList<>();

            String border = "+++ 🐉 +++";
            lines.add(border);
            lines.add(String.format("Loaded %s v%s by MrJulsen!", mod.getName(), mod.getVersion()));
            lines.add(String.format("Minecraft %s %s %s%s%s",
                Platform.isForge() ? "Forge" : (Platform.isFabric() ? "Fabric" : ""),
                (Platform.getEnvironment() == Env.CLIENT ? "Client" : (Platform.getEnvironment() == Env.SERVER ? "Server" : "?")),
                Platform.getMinecraftVersion(),
                getModloaderVersion(),
                Platform.isDevelopmentEnvironment() ? " (Dev)" : "")
            );
            lines.add("");
            lines.add(String.format("Discord: %s", MRJULSEN_DISCORD));
            lines.add(String.format("Documentation: %s", mod.getHomepage().orElse("unknown")));
            lines.add(String.format("Bug Reports: %s", mod.getIssueTracker().orElse("unknown")));
            lines.add(border);

            int width = lines.stream().mapToInt(String::length).max().getAsInt() + 4;
            lines = new ArrayList<>(lines.stream().map(x -> centerStringInArea(x, width)).toList());
            lines.add(1, lineOf('-', width));
            lines.add(lines.size() - 1, lineOf('-', width));
            
            lines.forEach(LOGGER::info);
        }, dragonTypes[RANDOM.nextInt(dragonTypes.length)]).start();
    }

    private static final String centerStringInArea(String text, int width) {
        if (text.isBlank()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        int verLength = sb.length();
        for (int i = 0; i < (width - verLength) / 2; i++) {
            sb.insert(0, " ");
            sb.append(" ");
        }
        return sb.toString();
    }

    private static final String lineOf(char c, int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(c);
        return sb.toString();
    }

    private static String getModloaderVersion() {
        if (Platform.isForge()) {
            return Platform.getOptionalMod("forge").map(x -> "-" + x.getVersion()).orElse("");
        } else if (Platform.isFabric()) {
            return Platform.getOptionalMod("fabric").map(x -> "-" + x.getVersion()).orElse("");
        }
        return "";
    }
}
