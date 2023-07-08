package slimeknights.mantle.client.model.util;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

public interface IGeometryBakingContextWithParent extends IGeometryBakingContext {
  UnbakedModel getParent();
}
