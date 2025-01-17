package net.xalcon.energyconverters.common.tiles;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.xalcon.energyconverters.common.EnergyConvertersConfig;
import net.xalcon.energyconverters.common.energy.BuildcraftProductionHandler;

import javax.annotation.Nullable;

public class TileEntityProducerMj extends TileEntityEnergyConvertersProducer implements ITickable
{
	private BuildcraftProductionHandler productionHandler = new BuildcraftProductionHandler(this);

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == MjAPI.CAP_CONNECTOR || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == MjAPI.CAP_CONNECTOR ? MjAPI.CAP_CONNECTOR.cast(this.productionHandler) : super.getCapability(capability, facing);
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update()
	{
		if (this.getWorld().isRemote) return;
		for (EnumFacing facing : EnumFacing.VALUES)
		{
			BlockPos pos = this.pos.offset(facing);
			TileEntity te = this.getWorld().getTileEntity(pos);
			if(te == null || te.getBlockType().getRegistryName().equals("galacticraftcore:aluminum_wire")) continue;
			if (te.hasCapability(MjAPI.CAP_RECEIVER, facing.getOpposite()))
			{
				IMjReceiver receiver = te.getCapability(MjAPI.CAP_RECEIVER, facing.getOpposite());
				if (receiver != null && receiver.canReceive())
				{
					long requested = receiver.getPowerRequested();
					if(requested > 0)
					{
						long availableMj = (long) Math.min(this.getBridgeEnergyStored() / EnergyConvertersConfig.mjConversion * MjAPI.MJ, requested);
						long usedMj = availableMj - receiver.receivePower(availableMj, false);
						this.retrieveEnergyFromBridge(usedMj / (double) MjAPI.MJ * EnergyConvertersConfig.mjConversion, false);
					}
				}
			}
		}
	}
}
