package ikube.application;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import ikube.IConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class will accumulate several properties from the running JVM via the MBeans over a
 * JMX connection. The types of data accumulated, the duration of the garbage collection, the interval
 * between garbage collections, the cpu load etc., are then used to populate a {@link ikube.application.GCSnapshot}
 * object. These populated snapshots then will be used an analysis to predict failure in the JVMs, or as
 * a monitoring feature.
 * <p/>
 * Unused memory areas are:
 * <pre>
 *     * Code Cache
 *     * PS Survivor Space
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCCollector implements Serializable {

    static final Logger LOGGER = LoggerFactory.getLogger(GCCollector.class);

    static final double PRUNE_THRESHOLD = IConstants.HUNDRED_THOUSAND;
    static final double PRUNE_RATIO = 0.75;

    private transient final List<GCSnapshot> gcSnapshots;

    private transient final String memoryBlock;
    private transient final ThreadMXBean threadMXBean;
    private transient final OperatingSystemMXBean operatingSystemMXBean;
    private transient final GarbageCollectorMXBean garbageCollectorMXBean;

    GCCollector(final String memoryBlock,
                final ThreadMXBean threadMXBean,
                final OperatingSystemMXBean operatingSystemMXBean,
                final GarbageCollectorMXBean garbageCollectorMXBean) {

        this.memoryBlock = memoryBlock;
        this.threadMXBean = threadMXBean;
        this.operatingSystemMXBean = operatingSystemMXBean;
        this.garbageCollectorMXBean = garbageCollectorMXBean;

        gcSnapshots = new ArrayList<>();
    }

    synchronized GCSnapshot getGcSnapshot() {
        GCSnapshot gcSnapshot = new GCSnapshot();
        DecimalFormat decimalFormat = new DecimalFormat("#.#####");
        GcInfo gcInfo = garbageCollectorMXBean.getLastGcInfo();
        if (gcInfo == null) {
            return gcSnapshot;
        }

        GCSnapshot previousGcSnapshot = gcSnapshots.isEmpty() ? null : gcSnapshots.get(gcSnapshots.size() - 1);

        Map<String, MemoryUsage> usageMap = gcInfo.getMemoryUsageBeforeGc();
        MemoryUsage memoryUsage = usageMap.get(memoryBlock);
        long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("Jvm start time : " + new Date(jvmStartTime).toString());
        }

        gcSnapshot.start = jvmStartTime + gcInfo.getStartTime();
        gcSnapshot.end = jvmStartTime + gcInfo.getEndTime();
        gcSnapshot.duration = gcInfo.getDuration();
        gcSnapshot.available = memoryUsage.getMax() - memoryUsage.getUsed();
        gcSnapshot.usedToMaxRatio = Double.parseDouble(decimalFormat.format((double) memoryUsage.getUsed() / (double) memoryUsage.getMax()));
        gcSnapshot.cpuLoad = operatingSystemMXBean.getSystemLoadAverage();
        gcSnapshot.processors = operatingSystemMXBean.getAvailableProcessors();
        gcSnapshot.perCoreLoad = gcSnapshot.cpuLoad / gcSnapshot.processors;
        gcSnapshot.threads = threadMXBean.getThreadCount();

        if (previousGcSnapshot != null) {
            gcSnapshot.interval = gcSnapshot.start - previousGcSnapshot.start;
            gcSnapshot.delta = Double.parseDouble(decimalFormat.format(previousGcSnapshot.available - gcSnapshot.available));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.error(
                    "Type : " + StringUtils.substring(memoryBlock, 0, 10) +
                            ", int : " + decimalFormat.format(gcSnapshot.interval) +
                            ", start : " + new Date(gcSnapshot.start) +
                            ", end : " + new Date(gcSnapshot.end) +
                            ", dura : " + decimalFormat.format(gcSnapshot.duration) +
                            ", delta : " + decimalFormat.format((gcSnapshot.delta / IConstants.MILLION)) +
                            ", avail : " + decimalFormat.format((gcSnapshot.available / IConstants.MILLION)) +
                            ", use/max : " + decimalFormat.format(gcSnapshot.usedToMaxRatio) +
                            /* ", run/sec : " + gcSnapshot.runsPerTimeUnit + */
                            ", cpu : " + decimalFormat.format(gcSnapshot.cpuLoad) +
                            ", cpu/core: " + decimalFormat.format(gcSnapshot.perCoreLoad) +
                            ", threads : " + gcSnapshot.threads);
        }

        gcSnapshots.add(gcSnapshot);

        if (gcSnapshots.size() > PRUNE_THRESHOLD) {
            double toKeep = PRUNE_THRESHOLD * PRUNE_RATIO; // 75000
            int to = (int) (PRUNE_THRESHOLD - toKeep); // 25000
            GCSnapshot[] gcSnapshotsRange = Arrays.copyOfRange(gcSnapshots.toArray(), 0, to, GCSnapshot[].class);
            // Remove the first 25000
            gcSnapshots.removeAll(Arrays.asList(gcSnapshotsRange));
        }

        return gcSnapshot;
    }

    List<GCSnapshot> getGcSnapshots() {
        return new ArrayList<>(this.gcSnapshots);
    }

}