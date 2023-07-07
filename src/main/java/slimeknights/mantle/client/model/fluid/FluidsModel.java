package slimeknights.mantle.client.model.fluid;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This model contains a list of fluid cuboids for the sake of rendering multiple fluid regions in world. It is used by the faucet at this time
 */
@AllArgsConstructor
public class FluidsModel implements IUnbakedGeometry<FluidsModel> {
  private final SimpleBlockModel model;
  private final List<FluidCuboid> fluids;

  @Override
  public Collection<Material> getMaterials(IGeometryBakingContext bakingContext, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getMaterials(bakingContext, modelGetter, missingTextureErrors);
  }

  @Override
  public BakedModel bake(IGeometryBakingContext bakingContext, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    BakedModel baked = model.bakeModel(bakingContext, transform, overrides, spriteGetter, location);
    return new Baked(baked, fluids);
  }

  @Override
  public Set<String> getConfigurableComponentNames() {
    return IUnbakedGeometry.super.getConfigurableComponentNames();
  }

  /** Baked model, mostly a data wrapper around a normal model */
  @SuppressWarnings("WeakerAccess")
  public static class Baked extends BakedModelWrapper<BakedModel> {
    @Getter
    private final List<FluidCuboid> fluids;
    public Baked(BakedModel originalModel, List<FluidCuboid> fluids) {
      super(originalModel);
      this.fluids = fluids;
    }
  }

  /** Loader for this model */
  public static class Loader implements IGeometryLoader<FluidsModel> {
    /**
     * Shared loader instance
     */
    public static final Loader INSTANCE = new Loader();

    @Override
    public FluidsModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
      SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
      List<FluidCuboid> fluid = FluidCuboid.listFromJson(modelContents, "fluids");
      return new FluidsModel(model, fluid);
    }
  }
}
