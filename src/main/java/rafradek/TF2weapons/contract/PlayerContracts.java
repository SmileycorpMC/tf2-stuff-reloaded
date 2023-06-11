package rafradek.TF2weapons.contract;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.io.File;

public class PlayerContracts extends PlayerAdvancements {

	private EntityPlayerMP player;
	private PlayerContractsProxy networkProxy;
	private PlayerAdvancements advancementsVanilla;

	public PlayerContracts(PlayerAdvancements advancements, MinecraftServer server, File p_i47422_2_,
			EntityPlayerMP player) {
		super(server, p_i47422_2_, player);
		this.player = player;
		this.networkProxy = new PlayerContractsProxy(player.connection, server, player);
		this.advancementsVanilla = advancements;
	}

	@Override
	public void setPlayer(EntityPlayerMP player) {
		this.player = player;
		advancementsVanilla.setPlayer(player);
	}

	@Override
	public void dispose() {
		advancementsVanilla.dispose();
	}

	@Override
	public void reload() {
		advancementsVanilla.reload();
	}

	@Override
	public void setSelectedTab(@Nullable Advancement p_194220_1_) {
		NetHandlerPlayServer old = player.connection;
		player.connection = networkProxy;
		advancementsVanilla.setSelectedTab(p_194220_1_);
		player.connection = old;

	}

	@Override
	public void flushDirty(EntityPlayerMP p_192741_1_) {
		advancementsVanilla.flushDirty(p_192741_1_);
		NetHandlerPlayServer old = player.connection;
		player.connection = networkProxy;
		super.flushDirty(p_192741_1_);
		player.connection = old;
	}

	@Override
	public void save() {
		this.advancementsVanilla.save();
	}
}
