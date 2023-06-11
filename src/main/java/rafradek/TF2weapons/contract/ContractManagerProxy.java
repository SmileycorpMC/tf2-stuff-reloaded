package rafradek.TF2weapons.contract;

import net.minecraft.advancements.AdvancementManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;

public class ContractManagerProxy extends MinecraftServer {

	private MinecraftServer original;
	private ContractManager manager;

	public ContractManagerProxy(MinecraftServer original, ContractManager manager) {
		super(new File("."), Proxy.NO_PROXY, null, null, null, null, null);
		this.original = original;
		this.manager = manager;
	}

	@Override
	public boolean init() throws IOException {
		return false;
	}

	@Override
	public boolean canStructuresSpawn() {
		return false;
	}

	@Override
	public GameType getGameType() {
		return null;
	}

	@Override
	public EnumDifficulty getDifficulty() {
		return null;
	}

	@Override
	public boolean isHardcore() {
		return false;
	}

	@Override
	public int getOpPermissionLevel() {
		return 0;
	}

	@Override
	public boolean shouldBroadcastRconToOps() {
		return false;
	}

	@Override
	public boolean shouldBroadcastConsoleToOps() {
		return false;
	}

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public boolean shouldUseNativeTransport() {
		return false;
	}

	@Override
	public boolean isCommandBlockEnabled() {
		return false;
	}

	@Override
	public String shareToLAN(GameType type, boolean allowCheats) {
		return null;
	}

	@Override
	public AdvancementManager getAdvancementManager() {
		return this.manager;
	}

	@Override
	public PlayerList getPlayerList() {
		return this.original.getPlayerList();
	}

}
