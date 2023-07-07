package slimeknights.mantle.client.model.fluid;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.registration.ModelFluidAttributes;
import slimeknights.mantle.registration.ModelFluidAttributes.IFluidModelProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Fluid model that allows a resource pack to control the textures of a block. Use alongside {@link ModelFluidAttributes} */
@RequiredArgsConstructor
public class FluidTextureModel implements IUnbakedGeometry<FluidTextureModel> {
  public static Loader LOADER = new Loader();

  private final int color;

  /** Checks if a texture is missing */
  private static boolean isMissing(Material material) {
    return MissingTextureAtlasSprite.getLocation().equals(material.texture());
  }

  /** Gets the texture, or null if missing */
  private static void getMaterial(IGeometryBakingContext bakingContext, String name, Collection<Material> textures, Set<Pair<String,String>> missingTextureErrors) {
    Material material = bakingContext.getMaterial(name);
    if (isMissing(material)) {
      missingTextureErrors.add(Pair.of(name, bakingContext.getModelName()));
    }
    textures.add(material);
  }

  @Override
  public Collection<Material> getMaterials(IGeometryBakingContext bakingContext, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    Set<Material> textures = new HashSet<>();
    getMaterial(bakingContext, "still", textures, missingTextureErrors);
    getMaterial(bakingContext, "flowing", textures, missingTextureErrors);
    Material overlay = bakingContext.getMaterial("overlay");
    if (!isMissing(overlay)) {
      textures.add(overlay);
    }
    return textures;
  }

  @Override
  public BakedModel bake(IGeometryBakingContext bakingContext, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
    Material still = bakingContext.getMaterial("still");
    Material flowing = bakingContext.getMaterial("flowing");
    Material overlay = bakingContext.getMaterial("overlay");
    ResourceLocation overlayLocation = isMissing(overlay) ? null : overlay.texture();
    BakedModel baked = new SimpleBakedModel.Builder(
      bakingContext.useAmbientOcclusion(),
      bakingContext.useBlockLight(),
      bakingContext.isGui3d(),
      bakingContext.getTransforms(),
      overrides).particle(spriteGetter.apply(still)).build();
    return new Baked(baked, still.texture(), flowing.texture(), overlayLocation, color);
  }

  /** Data holder class, has no quads */
  private static class Baked extends BakedModelWrapper<BakedModel> {
    @Getter
    private final ResourceLocation still;
    @Getter
    private final ResourceLocation flowing;
    @Getter
    private final ResourceLocation overlay;
    @Getter
    private final int color;
    public Baked(BakedModel originalModel, ResourceLocation still, ResourceLocation flowing, @Nullable ResourceLocation overlay, int color) {
      super(originalModel);
      this.still = still;
      this.flowing = flowing;
      this.overlay = overlay;
      this.color = color;
    }
  }

  /** Model loader, also doubles as the fluid model provider */
  private static class Loader implements IGeometryLoader<FluidTextureModel>, IFluidModelProvider {
    private final Map<Fluid,Baked> modelCache = new ConcurrentHashMap<>();

    /** Gets a model for a fluid */
    @Nullable
    private Baked getFluidModel(Fluid fluid) {
      return ModelHelper.getBakedModel(fluid.defaultFluidState().createLegacyBlock(), Baked.class);
    }

    /** Gets a model for a fluid from the cache */
    @Nullable
    private Baked getCachedModel(Fluid fluid) {
      return modelCache.computeIfAbsent(fluid, this::getFluidModel);
    }

    @Override
    @Nullable
    public ResourceLocation getStillTexture(Fluid fluid) {
      Baked model = getCachedModel(fluid);
      return model == null ? null : model.getStill();
    }

    @Override
    @Nullable
    public ResourceLocation getFlowingTexture(Fluid fluid) {
      Baked model = getCachedModel(fluid);
      return model == null ? null : model.getFlowing();
    }

    @Override
    @Nullable
    public ResourceLocation getOverlayTexture(Fluid fluid) {
      Baked model = getCachedModel(fluid);
      return model == null ? null : model.getOverlay();
    }

    @Override
    public int getColor(Fluid fluid) {
      Baked model = getCachedModel(fluid);
      return model == null ? -1 : model.getColor();
    }

    @Override
    public FluidTextureModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
      int color = -1;
      if (modelContents.has("color")) {
        String colorString = GsonHelper.getAsString(modelContents, "color");
        int length = colorString.length();
        // prevent some invalid strings, colors should all be 6 or 8 digits
        if (colorString.charAt(0) == '-' || (length != 6 && length != 8)) {
          throw new JsonSyntaxException("Invalid color '" + colorString + "'");
        }
        try {
          color = (int)Long.parseLong(colorString, 16);
          // for 6 length, make fully opaque
          if (length == 6) {
            color |= 0xFF000000;
          }
        } catch (NumberFormatException e) {
          throw new JsonSyntaxException("Invalid color '" + colorString + "'");
        }
      }
      return new FluidTextureModel(color);
    }
  }
}
