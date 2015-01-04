package mantle.blocks.abstracts;

import mantle.blocks.BlockUtils;
import mantle.blocks.iface.IMasterLogic;
import mantle.blocks.iface.IServantLogic;
import mantle.debug.DebugData;
import mantle.debug.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MultiServantLogic extends TileEntity implements IServantLogic, IDebuggable
{
    boolean hasMaster;
    BlockPos master;
    Block masterBlock;
    IBlockState state;

    public boolean canUpdate ()
    {
        return false;
    }

    public boolean getHasMaster ()
    {
        return hasMaster;
    }

    public boolean hasValidMaster ()
    {
        if (!hasMaster)
            return false;

        if (worldObj.getBlockState(master).getBlock() == masterBlock && worldObj.getBlockState(master) == state)
            return true;

        else
        {
            hasMaster = false;
            master = null;
            return false;
        }
    }

    public BlockPos getMasterPosition ()
    {
        return master;
    }

    public void overrideMaster (BlockPos pos)
    {
        hasMaster = true;
        master = pos;
        state = worldObj.getBlockState(master);
        masterBlock = state.getBlock();
    }

    public void removeMaster ()
    {
        hasMaster = false;
        master = null;
        masterBlock = null;
        state = null;
    }

    @Override
    public boolean setPotentialMaster (IMasterLogic master, World w, BlockPos pos)
    {
        return !hasMaster;
    }

    @Deprecated
    public boolean verifyMaster (IMasterLogic logic, BlockPos pos)
    {
        return master.equals(pos) && worldObj.getBlockState(pos) == state && worldObj.getBlockState(pos).getBlock() == masterBlock;
    }

    @Override
    public boolean verifyMaster (IMasterLogic logic, World world, BlockPos pos)
    {
        if (hasMaster)
        {
            return hasValidMaster();
        }
        else
        {
            overrideMaster(pos);
            return true;
        }
    }

    @Override
    public void invalidateMaster (IMasterLogic master, World w, BlockPos pos)
    {
        hasMaster = false;
        master = null;
    }

    public void notifyMasterOfChange ()
    {
        if (hasValidMaster())
        {
            IMasterLogic logic = (IMasterLogic) worldObj.getTileEntity(pos);
            logic.notifyChange(this, pos);
        }
    }

    public void readCustomNBT (NBTTagCompound tags)
    {
        hasMaster = tags.getBoolean("TiedToMaster");
        if (hasMaster)
        {
            int xCenter = tags.getInteger("xCenter");
            int yCenter = tags.getInteger("yCenter");
            int zCenter = tags.getInteger("zCenter");
            master = new BlockPos(xCenter, yCenter, zCenter);
            masterBlock = BlockUtils.getBlockFromUniqueName(tags.getString("MasterBlockName"));
            // TODO: Make this a byte.
            state = Block.getStateById(tags.getInteger("masterState"));
        }
    }

    public void writeCustomNBT (NBTTagCompound tags)
    {
        tags.setBoolean("TiedToMaster", hasMaster);
        if (hasMaster)
        {
            tags.setInteger("xCenter", master.getX());
            tags.setInteger("yCenter", master.getY());
            tags.setInteger("zCenter", master.getZ());
            tags.setString("MasterBlockName", BlockUtils.getUniqueName(masterBlock));
            // TODO: Make this a byte.
            tags.setInteger("masterState", Block.getStateId(state));
        }
    }

    @Override
    public void readFromNBT (NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        readCustomNBT(tags);
    }

    @Override
    public void writeToNBT (NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        writeCustomNBT(tags);
    }

    /* Packets */
    @Override
    public Packet getDescriptionPacket ()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeCustomNBT(tag);
        return new S35PacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readCustomNBT(packet.getNbtCompound());
        worldObj.notifyLightSet(pos);
        worldObj.markBlockForUpdate(pos);
    }

    /* IDebuggable */
    @Override
    public DebugData getDebugInfo (EntityPlayer player)
    {
        String[] strs = new String[2];
        strs[0] = "Location: x" + pos.getX() + ", y" + pos.getY() + ", z" + pos.getZ();
        if (hasMaster)
        {
            strs[1] = "masterBlock: " + masterBlock.toString() + ", masterMeat: " + state.toString();
        }
        else
        {
            strs[1] = "No active master.";
        }
        return new DebugData(player, getClass(), strs);
    }

    public World getWorld ()
    {
        return worldObj;
    }

    @Deprecated
    public boolean setMaster (BlockPos pos)
    {
        if (!hasMaster || worldObj.getBlockState(master) != state || (worldObj.getBlockState(master).getBlock() != masterBlock))
        {
            overrideMaster(pos);
            return true;
        }
        else
        {
            return false;
        }
    }

}
