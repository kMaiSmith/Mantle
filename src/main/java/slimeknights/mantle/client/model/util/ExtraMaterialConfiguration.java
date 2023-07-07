package slimeknights.mantle.client.model.util;

import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import java.util.Map;

/**
 * Model configuration wrapper to add in an extra set of textures
 */
public class ExtraMaterialConfiguration extends ModelConfigurationWrapper {
  private final Map<String,Material> materials;

  /**
   * Creates a new wrapper using the given material
   * @param base      Base configuration
   * @param material  Textures map, any material in this map will take precedence over those in the base configuration
   */
  public ExtraMaterialConfiguration(IGeometryBakingContext base, Map<String,Material> material) {
    super(base);
    this.materials = material;
  }

//  /** TODO figure out what to do with this
//   * Creates a new wrapper for a single texture
//   * @param base     Base configuration
//   * @param name     Texture name, if it matches texture is returned
//   * @param texture  Texture path
//   */
//  public ExtraMaterialConfiguration(IGeometryBakingContext base, String name, ResourceLocation texture) {
//    super(base);
//    this.materials = ImmutableMap.of(name, ModelLoaderRegistry.blockMaterial(texture));
//  }

  @Override
  public Material getMaterial(String name) {
    Material connected = materials.get(name);
    if (connected != null) {
      return connected;
    }
    return super.getMaterial(name);
  }

  @Override
  public boolean hasMaterial(String name) {
    return materials.containsKey(name) || super.hasMaterial(name);
  }
}
