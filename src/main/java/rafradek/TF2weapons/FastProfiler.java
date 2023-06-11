package rafradek.TF2weapons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FastProfiler extends Profiler {
	private static final Logger LOGGER = LogManager.getLogger();
	/** List of parent sections */
	private final List<String> sectionList = Lists.<String>newArrayList();
	/** List of timestamps (System.nanoTime) */
	private final List<Long> timestampList = Lists.<Long>newArrayList();
	/** Flag profiling enabled */
	/** Current profiling section */
	private String profilingSection = "";
	/** Profiling map */
	private final Map<String, Long> profilingMap = Maps.<String, Long>newHashMap();

	private long nanotime;

	private String lastcaller = "";
	private long maxcomplex = 6;
	private long complex = 0;

	/**
	 * Clear profiling.
	 */
	@Override
	public void clearProfiling() {
		this.profilingMap.clear();
		this.profilingSection = "";
		this.sectionList.clear();
		this.nanotime = 0;
	}

	/**
	 * Start section
	 */
	@Override
	public void startSection(String name) {
		if (this.profilingEnabled) {
			this.complex += 1;
			if (nanotime == 0) {
				long prev = System.nanoTime();
				long total = 0;
				for (int i = 0; i < 100; i++) {
					total += System.nanoTime() - prev;
				}
				nanotime = total / 100;
			}
			if (this.complex <= this.maxcomplex) {
				if (!this.profilingSection.isEmpty()) {
					this.profilingSection = this.profilingSection + ".";
				}
				this.profilingSection = this.profilingSection + name;
				this.sectionList.add(this.profilingSection);
				this.timestampList.add(Long.valueOf(System.nanoTime()));
			}
		}
	}

	@Override
	public void func_194340_a(Supplier<String> p_194340_1_) {
		if (this.profilingEnabled) {
			this.startSection(p_194340_1_.get());
		}
	}

	/**
	 * End section
	 */
	@Override
	public void endSection() {
		if (this.profilingEnabled) {
			this.complex--;
			if (this.complex < this.maxcomplex) {
				long i = System.nanoTime();
				long j = this.timestampList.remove(this.timestampList.size() - 1).longValue();
				this.sectionList.remove(this.sectionList.size() - 1);
				long k = i - j;

				if (this.profilingMap.containsKey(this.profilingSection)) {
					this.profilingMap.put(this.profilingSection,
							Long.valueOf(this.profilingMap.get(this.profilingSection).longValue() + k));
				} else {
					this.profilingMap.put(this.profilingSection, Long.valueOf(k));
				}

				if (k > 50000000L) {
					LOGGER.warn("Something's taking too long! '{}' of {} took aprox {} ms", this.profilingSection,
							lastcaller, Double.valueOf(k / 1000000.0D));
				}

				this.profilingSection = this.sectionList.isEmpty() ? ""
						: (String) this.sectionList.get(this.sectionList.size() - 1);
			}
		}
	}

	/**
	 * Get profiling data
	 */
	@Override
	public List<Profiler.Result> getProfilingData(String profilerName) {
		if (!this.profilingEnabled) {
			return Collections.<Profiler.Result>emptyList();
		} else {
			long i = this.profilingMap.containsKey("root") ? this.profilingMap.get("root").longValue() : 0L;
			long j = this.profilingMap.containsKey(profilerName) ? this.profilingMap.get(profilerName).longValue()
					: -1L;
			List<Profiler.Result> list = Lists.<Profiler.Result>newArrayList();

			if (!profilerName.isEmpty()) {
				profilerName = profilerName + ".";
			}

			long k = 0L;

			for (String s : this.profilingMap.keySet()) {
				if (s.length() > profilerName.length() && s.startsWith(profilerName)
						&& s.indexOf(".", profilerName.length() + 1) < 0) {
					k += this.profilingMap.get(s).longValue();
				}
			}

			float f = k;

			if (k < j) {
				k = j;
			}

			if (i < k) {
				i = k;
			}

			for (String s1 : this.profilingMap.keySet()) {
				if (s1.length() > profilerName.length() && s1.startsWith(profilerName)
						&& s1.indexOf(".", profilerName.length() + 1) < 0) {
					long l = this.profilingMap.get(s1).longValue();
					double d0 = l * 100.0D / k;
					double d1 = l * 100.0D / i;
					String s2 = s1.substring(profilerName.length());
					list.add(new Profiler.Result(s2, d0, d1));
				}
			}

			for (String s3 : this.profilingMap.keySet()) {
				this.profilingMap.put(s3, Long.valueOf(this.profilingMap.get(s3).longValue() * 999L / 1000L));
			}

			if (k > f) {
				list.add(new Profiler.Result("unspecified", (k - f) * 100.0D / k, (k - f) * 100.0D / i));
			}

			Collections.sort(list);
			list.add(0, new Profiler.Result(profilerName, 100.0D, k * 100.0D / i));
			return list;
		}
	}

	/**
	 * End current section and start a new section
	 */
	@Override
	public void endStartSection(String name) {
		this.endSection();
		this.startSection(name);
	}

	@Override
	public String getNameOfLastSection() {
		return this.sectionList.isEmpty() ? "[UNKNOWN]" : (String) this.sectionList.get(this.sectionList.size() - 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void func_194339_b(Supplier<String> p_194339_1_) {
		this.endSection();
		this.func_194340_a(p_194339_1_);
	}

	/**
	 * Forge: Fix for MC-117087, World.updateEntities is wasting time calling
	 * Class.getSimpleName() when the profiler is not active
	 */
	@Override
	@Deprecated
	public void startSection(Class<?> profiledClass) {
		if (this.profilingEnabled) {
			startSection(profiledClass.getSimpleName());
		}
	}
}
