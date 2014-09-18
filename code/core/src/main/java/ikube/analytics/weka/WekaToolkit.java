package ikube.analytics.weka;

import ikube.model.Context;
import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static ikube.toolkit.MatrixUtilities.excludeColumns;
import static ikube.toolkit.MatrixUtilities.objectVectorToDoubleVector;

/**
 * This class contains general methods for manipulating the Weka data, and for writing
 * the models to the file system.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public final class WekaToolkit {

    private static final Logger LOGGER = LoggerFactory.getLogger(WekaToolkit.class);

    /**
     * Writes the instanes to a file that can be loaded again and used to train algorithms.
     *
     * @param instances the instances data to write to the file
     * @param filePath  the absolute path to the output file
     */
    public static void writeToArff(final Instances instances, final String filePath) {
        try {
            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(instances);
            File file = FileUtilities.getOrCreateFile(filePath);
            arffSaver.setFile(file);
            arffSaver.writeBatch();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method converts and instances object to a sparse instance, which will then
     * be more memory efficient.
     *
     * @param instances the instances to convert to a sparse instances model
     * @return the sparse instances, more memory efficient
     */
    public static Instances nonSparseToSparse(final Instances instances) {
        try {
            NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
            nonSparseToSparseInstance.setInputFormat(instances);
            return Filter.useFilter(instances, nonSparseToSparseInstance);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is a convenience method to load a pure csv file and create an instances object that Weka
     * can use. This would typically be used in an environment where there are missing values in the input
     * data and the Weka loader doesn't like that.
     *
     * @param filePath   the path to the file to load the data from
     * @param classIndex the class index that is missing, can be -1 if there is no known missing attribute,
     *                   and the missing attribute will be set to 0, i.e. the first one, which is the default
     * @return the instances object created from the input, data, with the same number of attributed labled as
     * the input data has vector lengths
     */
    public static Instances csvFileToInstances(final String filePath, final int classIndex) {
        Object[][] matrix;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filePath));
            matrix = new CsvFileTools().getCsvData(inputStream);
            return matrixToInstances(matrix, classIndex);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * This method will convert a matrix to a 'flat' {@link weka.core.Instances} object. Note that the
     * matrix will be converted where necessary to a double vector to comply with the input requirements
     * of the Weka algorithms.
     *
     * @param matrix             the matrix to convert into an instances object, or data set for Weka
     * @param classIndex         the class index or index o the attribute that is 'missing' or to be predicted, if this is
     *                           set to Integer.MAX_VALUE then the last attribute will be used as the clss index
     * @param excludedAttributes attributes that should be excluded from the matrix. If these are specified then the
     *                           matrix will be pruned of the 'columns' that are specified before creating the instances
     *                           object
     * @return the instances object created from the matrix, with all the attributes doubles, ready for processing
     */
    public static Instances matrixToInstances(final Object[][] matrix, final int classIndex, final int... excludedAttributes) {
        Object[][] prunedMatrix = excludeColumns(matrix, excludedAttributes);
        // Create the instances from the matrix data
        FastVector attributes = new FastVector();
        // Check that we have the shortest vector
        int shortestVectorLength = Integer.MAX_VALUE;
        for (final Object[] vector : prunedMatrix) {
            shortestVectorLength = Math.min(shortestVectorLength, vector.length);
        }
        // Add the attributes to the data set
        for (int i = 0; i < shortestVectorLength; i++) {
            attributes.addElement(new Attribute(Integer.toHexString(i)));
        }
        // Create the instances data set from the data and the attributes
        Instances instances = new Instances("instances", attributes, 0);
        instances.setClass(instances.attribute(Math.min(instances.numAttributes() - 1, classIndex)));
        // Populate the instances
        for (final Object[] vector : prunedMatrix) {
            double[] doubleVector = objectVectorToDoubleVector(vector, shortestVectorLength);
            Instance instance = new Instance(1.0, doubleVector);
            instances.add(instance);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Instances : " + instances.numAttributes() + ", " + instances.numInstances()
                    + ", " + instances.toSummaryString());
        }
        return instances;
    }

    public static void printClusterInstances(final Context context) throws Exception {
        Object[] clusterers = context.getAlgorithms();
        Object[] instanceses = context.getModels();
        for (int i = 0; i < clusterers.length; i++) {
            Clusterer clusterer = (Clusterer) clusterers[i];
            Instances instances = (Instances) instanceses[i];
            LOGGER.warn("Num clusters : " + clusterer.numberOfClusters());
            for (int j = 0; j < instances.numAttributes(); j++) {
                Attribute attribute = instances.attribute(j);
                LOGGER.warn("Attribute : " + attribute.name() + ", " + attribute.type());
                for (int k = 0; k < attribute.numValues(); k++) {
                    LOGGER.warn("          : " + attribute.value(k));
                }
            }
        }
    }

    private WekaToolkit() {
    }

}
