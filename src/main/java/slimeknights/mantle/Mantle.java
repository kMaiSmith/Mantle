package slimeknights.mantle;

import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.block.SetBlockPredicate;
import slimeknights.mantle.data.predicate.block.TagBlockPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.predicate.entity.MobTypePredicate;
import slimeknights.mantle.data.predicate.entity.TagEntityPredicate;
import slimeknights.mantle.datagen.MantleFluidTagProvider;
import slimeknights.mantle.datagen.MantleFluidTooltipProvider;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.transfer.EmptyFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.EmptyFluidWithNBTTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidWithNBTTransfer;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.item.LecternBookItem;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.crafting.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.helper.TagEmptyCondition;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.registration.adapter.BlockEntityTypeRegistryAdapter;
import slimeknights.mantle.util.OffhandCooldownTracker;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(Mantle.modId)
public class Mantle {
  public static final String modId = "mantle";
  public static final Logger logger = LogManager.getLogger("Mantle");

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  /* Proxies for sides, used for graphics processing */
  public Mantle() {
    ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
    ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_SPEC);

    FluidContainerTransferManager.INSTANCE.init();
    MantleTags.init();

    instance = this;
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerInteractEvent.RightClickBlock.class, LecternBookItem::interactWithBlock);
  }

  @SubscribeEvent
  public void registerCapabilities(RegisterCapabilitiesEvent event) {
    OffhandCooldownTracker.register(event);
  }

  @SubscribeEvent
  public void commonSetup(FMLCommonSetupEvent event) {
    MantleNetwork.registerPackets();
    MantleCommand.init();
    OffhandCooldownTracker.init();
    TagPreference.init();
  }

  @SubscribeEvent
  public void handleRegisterEvent(RegisterEvent event) {
    event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS,
      helper -> {
        helper.register("crafting_shaped_fallback", new ShapedFallbackRecipe.Serializer());
        helper.register("crafting_shaped_retextured", new ShapedRetexturedRecipe.Serializer());
      }
    );

    BlockEntityTypeRegistryAdapter adapter = new BlockEntityTypeRegistryAdapter(event.getForgeRegistry());
    adapter.register(MantleSignBlockEntity::new, "sign", MantleSignBlockEntity::buildSignBlocks);

    CraftingHelper.register(TagEmptyCondition.SERIALIZER);
    CraftingHelper.register(FluidContainerIngredient.ID, FluidContainerIngredient.SERIALIZER);

    // fluid container transfer
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyFluidContainerTransfer.ID, EmptyFluidContainerTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(FillFluidContainerTransfer.ID, FillFluidContainerTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyFluidWithNBTTransfer.ID, EmptyFluidWithNBTTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(FillFluidWithNBTTransfer.ID, FillFluidWithNBTTransfer.DESERIALIZER);

    // predicates
    {
      // block predicates
      BlockPredicate.LOADER.register(getResource("and"), BlockPredicate.AND);
      BlockPredicate.LOADER.register(getResource("or"), BlockPredicate.OR);
      BlockPredicate.LOADER.register(getResource("inverted"), BlockPredicate.INVERTED);
      BlockPredicate.LOADER.register(getResource("requires_tool"), BlockPredicate.REQUIRES_TOOL.getLoader());
      BlockPredicate.LOADER.register(getResource("set"), SetBlockPredicate.LOADER);
      BlockPredicate.LOADER.register(getResource("tag"), TagBlockPredicate.LOADER);
      // entity predicates
      LivingEntityPredicate.LOADER.register(getResource("and"), LivingEntityPredicate.AND);
      LivingEntityPredicate.LOADER.register(getResource("or"), LivingEntityPredicate.OR);
      LivingEntityPredicate.LOADER.register(getResource("inverted"), LivingEntityPredicate.INVERTED);
      LivingEntityPredicate.LOADER.register(getResource("any"), LivingEntityPredicate.ANY.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("fire_immune"), LivingEntityPredicate.FIRE_IMMUNE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("water_sensitive"), LivingEntityPredicate.WATER_SENSITIVE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("on_fire"), LivingEntityPredicate.ON_FIRE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("tag"), TagEntityPredicate.LOADER);
      LivingEntityPredicate.LOADER.register(getResource("mob_type"), MobTypePredicate.LOADER);
      // register mob types
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("undefined"), MobType.UNDEFINED);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("undead"), MobType.UNDEAD);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("arthropod"), MobType.ARTHROPOD);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("illager"), MobType.ILLAGER);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("water"), MobType.WATER);
    }
  }

  @SubscribeEvent
  public void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();

    generator.addProvider(event.includeServer(), new MantleFluidTagProvider(generator, event.getExistingFileHelper()));
    generator.addProvider(event.includeClient(), new MantleFluidTooltipProvider(generator));
  }

  /**
   * Gets a resource location for Mantle
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation getResource(String name) {
    return new ResourceLocation(modId, name);
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeDescriptionId(String base, String name) {
    return Util.makeDescriptionId(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name) {
    return Component.translatable(makeDescriptionId(base, name));
  }
}
