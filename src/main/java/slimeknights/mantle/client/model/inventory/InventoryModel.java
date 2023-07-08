package slimeknights.mantle.client.model.inventory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
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
 * This model contains a list of multiple items to display in a TESR
 */
@AllArgsConstructor
public class InventoryModel implements IUnbakedGeometry<InventoryModel> {
  protected final SimpleBlockModel model;
  protected final List<ModelItem> items;

  @Override
  public Collection<Material> getMaterials(IGeometryBakingContext bakingContext, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getMaterials(bakingContext, modelGetter, missingTextureErrors);
  }

  @Override
  public BakedModel bake(IGeometryBakingContext bakingContext, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    BakedModel baked = model.bakeModel(bakingContext, transform, overrides, spriteGetter, location);
    return new Baked(baked, items);
  }

  /** Baked model, mostly a data wrapper around a normal model */
  @SuppressWarnings("WeakerAccess")
  public static class Baked extends BakedModelWrapper<BakedModel> {
    @Getter
    private final List<ModelItem> items;
    public Baked(BakedModel originalModel, List<ModelItem> items) {
      super(originalModel);
      this.items = items;
    }
  }

  /** Loader for this model */
  public static class Loader implements IGeometryLoader<InventoryModel> {
    /**
     * Shared loader instance
     */
    public static final Loader INSTANCE = new Loader();

    @Override
    public InventoryModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
      List<ModelItem> items = ModelItem.listFromJson(modelContents, "items");
      return new InventoryModel(model, items);
    }
  }
}
