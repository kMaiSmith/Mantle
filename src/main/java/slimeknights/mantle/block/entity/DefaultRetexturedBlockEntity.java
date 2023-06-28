package slimeknights.mantle.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.Lazy;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nonnull;

import static slimeknights.mantle.util.RetexturedHelper.BLOCK_PROPERTY;
import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Standard implementation for {@link IRetexturedBlockEntity}, use alongside {@link RetexturedBlock} and {@link slimeknights.mantle.item.RetexturedBlockItem}
 */
public class DefaultRetexturedBlockEntity extends MantleBlockEntity implements IRetexturedBlockEntity {

  private final Lazy<ModelData> data = Lazy.of(this::getRetexturedModelData);
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;
  public DefaultRetexturedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Override
  public ModelData getModelData() {
    return this.data.get();
  }

  @Override
  public CompoundTag getTileData() {
    return null;//Todo: figure out why this is needed
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = texture;
    texture = RetexturedHelper.getBlock(name);
    if (oldTexture != texture) {
      setChangedFast();
      onTextureUpdated();
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    if (texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    if (tags.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE));
      onTextureUpdated();
    }
  }

  /** Helper to call client side when the texture changes to refresh model data */
  public <T extends BlockEntity & IRetexturedBlockEntity> void onTextureUpdated() {
    // update the texture in BE data
    Level level = this.getLevel();
    if (level != null && level.isClientSide) {
      Block texture = this.getTexture();
      texture = texture == Blocks.AIR ? null : texture;
      if (data.get().get(BLOCK_PROPERTY) != texture) {
        data = Lazy.of(data.get().derive().with(BLOCK_PROPERTY, texture).build());
        this.requestModelDataUpdate();
        BlockState state = this.getBlockState();
        level.sendBlockUpdated(this.getBlockPos(), state, state, 0);
      }
    }
  }
}
