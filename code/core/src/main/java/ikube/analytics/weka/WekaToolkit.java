package ikube.analytics.weka;

import ikube.toolkit.FileUtilities;
import org.apache.log4j.Logger;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
@SuppressWarnings("ALL")
public final class WekaToolkit {

    public static void writeToArff(final Instances instances, final String filePath) {
        try {
            ArffSaver arffSaverInstance = new ArffSaver();
            arffSaverInstance.setInstances(instances);
            File file = FileUtilities.getOrCreateFile(filePath);
            arffSaverInstance.setFile(file);
            arffSaverInstance.writeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Instances nonSparseToSparse(final Instances instances) {
        try {
            NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
            nonSparseToSparseInstance.setInputFormat(instances);
            return Filter.useFilter(instances, nonSparseToSparseInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private WekaToolkit() {
    }

}
