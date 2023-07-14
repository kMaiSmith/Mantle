package slimeknights.mantle.client.model.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BakedItemModel implements BakedModel {

  protected final List<BakedQuad> quads;
  protected final TextureAtlasSprite particle;
  protected final ImmutableMap<TransformType, Transformation> transforms;
  protected final ItemOverrides overrides;
  protected final boolean useBlockLight;

  public BakedItemModel(List<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<TransformType, Transformation> transforms, ItemOverrides overrides, boolean useBlockLight)
  {
    this.quads = quads;
    this.particle = particle;
    this.transforms = transforms;
    this.overrides = overrides;
    this.useBlockLight = useBlockLight;
  }

  private static boolean hasGuiIdentity(ImmutableMap<TransformType, Transformation> transforms)
  {
    Transformation guiTransform = transforms.get(TransformType.GUI);
    return guiTransform == null || guiTransform.equals(Transformation.identity());
  }

  @Override public boolean useAmbientOcclusion() { return true; }
  @Override public boolean isGui3d() { return false; }
  @Override public boolean usesBlockLight() { return useBlockLight; }
  @Override public boolean isCustomRenderer() { return false; }
  @Override public TextureAtlasSprite getParticleIcon() { return particle; }

  @Override
  public ItemTransforms getTransforms() {
    return transforms;
  }

  @Override public ItemOverrides getOverrides() { return overrides; }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)
  {
    if (side == null)
    {
      return quads;
    }
    return ImmutableList.of();
  }
}
