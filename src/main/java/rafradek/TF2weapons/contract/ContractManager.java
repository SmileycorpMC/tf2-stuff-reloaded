package rafradek.TF2weapons.contract;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementTreeNode;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class ContractManager extends AdvancementManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final AdvancementList ADVANCEMENT_LIST = new AdvancementList();

	public ContractManager(File advancementsDirIn) {
		super(advancementsDirIn);
	}

	@Override
	public void reload() {
		ADVANCEMENT_LIST.clear();
		Map<ResourceLocation, Advancement.Builder> map = Maps.<ResourceLocation, Advancement.Builder>newHashMap();
		this.loadBuiltInAdvancements(map);
		ADVANCEMENT_LIST.loadAdvancements(map);

		for (Advancement advancement : ADVANCEMENT_LIST.getRoots()) {
			if (advancement.getDisplay() != null) {
				AdvancementTreeNode.layout(advancement);
			}
		}
	}

	private void loadBuiltInAdvancements(Map<ResourceLocation, Advancement.Builder> map) {
		FileSystem filesystem = null;

		try {
			URL url = AdvancementManager.class.getResource("/assets/.mcassetsroot");

			if (url != null) {
				URI uri = url.toURI();
				Path path;

				if ("file".equals(uri.getScheme())) {
					path = Paths.get(CraftingManager.class.getResource("/assets/minecraft/advancements").toURI());
				} else {
					if (!"jar".equals(uri.getScheme())) {
						LOGGER.error("Unsupported scheme " + uri + " trying to list all built-in advancements (NYI?)");
						return;
					}

					filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
					path = filesystem.getPath("/assets/minecraft/advancements");
				}

				Iterator<Path> iterator = Files.walk(path).iterator();

				while (iterator.hasNext()) {
					Path path1 = iterator.next();

					if ("json".equals(FilenameUtils.getExtension(path1.toString()))) {
						Path path2 = path.relativize(path1);
						String s = FilenameUtils.removeExtension(path2.toString()).replaceAll("\\\\", "/");
						ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);

						if (!map.containsKey(resourcelocation)) {
							BufferedReader bufferedreader = null;

							try {
								bufferedreader = Files.newBufferedReader(path1);
								Advancement.Builder advancement$builder = JsonUtils.fromJson(GSON, bufferedreader,
										Advancement.Builder.class);
								map.put(resourcelocation, advancement$builder);
							} catch (JsonParseException jsonparseexception) {
								LOGGER.error("Parsing error loading built-in advancement " + resourcelocation,
										jsonparseexception);
							} catch (IOException ioexception) {
								LOGGER.error("Couldn't read advancement " + resourcelocation + " from " + path1,
										ioexception);
							} finally {
								IOUtils.closeQuietly(bufferedreader);
							}
						}
					}
				}

				return;
			}

			LOGGER.error("Couldn't find .mcassetsroot");
		} catch (IOException | URISyntaxException urisyntaxexception) {
			LOGGER.error("Couldn't get a list of all built-in advancement files", urisyntaxexception);
			return;
		} finally {
			IOUtils.closeQuietly(filesystem);
		}
	}

	@Override
	@Nullable
	public Advancement getAdvancement(ResourceLocation id) {
		return ADVANCEMENT_LIST.getAdvancement(id);
	}

	@Override
	public Iterable<Advancement> getAdvancements() {
		return ADVANCEMENT_LIST.getAdvancements();
	}
}
