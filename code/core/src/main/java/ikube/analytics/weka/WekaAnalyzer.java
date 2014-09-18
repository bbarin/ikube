package ikube.analytics.weka;

import com.google.common.collect.Lists;
import ikube.IConstants;
import ikube.analytics.AAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.StringUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.filters.Filter;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static ikube.toolkit.FileUtilities.*;
import static ikube.toolkit.ThreadUtilities.waitForAnonymousFutures;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * This is the base class for the Weka implementation of the analytics API. It has base methods for creating
 * {@link weka.core.Instances} objects, loading data from files in 'arff' format, getting the class or cluster for an
 * instance, creating {@link weka.core.Instance} objects and filtering them for processing. There are also methods for
 * getting the correlation co-efficients and distributions for instances. {@link ikube.analytics.IAnalyzer} implementations
 * that wrap Weka should use this class as a base.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-11-2013
 */
public abstract class WekaAnalyzer extends AAnalyzer<Analysis, Analysis, Analysis> {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void init(final Context context) throws Exception {
        logger.warn("Config file path : " + configFilePath);
        if (String.class.isAssignableFrom(context.getAnalyzer().getClass())) {
            context.setAnalyzer(Class.forName(context.getAnalyzer().toString()).newInstance());
        }
        Object[] algorithms = context.getAlgorithms();
        Object[] filters = context.getFilters();
        // Create the analyzer algorithm, the filter and the model
        for (int i = 0; i < algorithms.length; i++) {
            Object algorithm = Class.forName(algorithms[i].toString()).newInstance();
            algorithms[i] = algorithm;
            if (filters != null && filters.length > i) {
                Filter filter = (Filter) Class.forName(filters[i].toString()).newInstance();
                filters[i] = filter;
            }
            Object[] options = context.getOptions();
            // The options for Weka are string arrays only
            if (options != null && options.length > 0 && OptionHandler.class.isAssignableFrom(algorithm.getClass())) {
                String[] cloned = new String[options.length];
                for (int j = 0; j < options.length; j++) {
                    cloned[j] = options[j].toString();
                }
                ((OptionHandler) algorithm).setOptions(cloned);
            }
        }
        // Load the models(Instances) for all the analyzers
        context.setModels(instances(context));
    }

    @Override
    public void build(final Context context) throws Exception {
        final Object[] algorithms = context.getAlgorithms();
        final Object[] models = context.getModels();
        final String[] evaluations = new String[algorithms.length];
        final Object[] capabilities = new Object[algorithms.length];

        class AnalyzerBuilder implements Runnable {
            final int index;

            AnalyzerBuilder(final int index) {
                this.index = index;
            }

            public void run() {
                try {
                    Instances instances = (Instances) models[index];
                    Filter filter = getFilter(context, index);

                    // Filter the data if necessary
                    Instances filteredInstances = filter(instances, filter);
                    filteredInstances.setRelationName("filtered-instances");

                    Object analyzer = algorithms[index];
                    if (Clusterer.class.isAssignableFrom(analyzer.getClass())) {
                        logger.info("Building clusterer : " + instances.numInstances());
                        ((Clusterer) analyzer).buildClusterer(filteredInstances);
                        evaluations[index] = evaluate((Clusterer) analyzer, instances);
                        capabilities[index] = ((Clusterer) analyzer).getCapabilities().toString();
                        logger.info("Clusterer built : " + filteredInstances.numInstances() + ", " + context.getName());
                    } else if (Classifier.class.isAssignableFrom(analyzer.getClass())) {
                        // And build the model
                        logger.info("Building classifier : " + instances.numInstances() + ", " + context.getName());
                        ((Classifier) analyzer).buildClassifier(filteredInstances);
                        logger.info("Classifier built : " + filteredInstances.numInstances() + ", " + context.getName());

                        // Set the evaluation of the classifier and the training model
                        evaluations[index] = evaluate((Classifier) analyzer, filteredInstances);
                        capabilities[index] = ((Classifier) analyzer).getCapabilities().toString();
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (context.isPersisted()) {
            Object[] deserializeAlgorithms = deserializeAnalyzers(context);
            if (deserializeAlgorithms != null && deserializeAlgorithms.length == algorithms.length) {
                System.arraycopy(deserializeAlgorithms, 0, algorithms, 0, algorithms.length);
                context.setBuilt(Boolean.TRUE);
            }
        }
        if (!context.isBuilt()) {
            List<Future> futures = Lists.newArrayList();
            for (int i = 0; i < context.getAlgorithms().length; i++) {
                Future<?> future = ThreadUtilities.submit(this.getClass().getName(), new AnalyzerBuilder(i));
                futures.add(future);
            }
            waitForAnonymousFutures(futures, Long.MAX_VALUE);
        }
        context.setAlgorithms(algorithms);
        context.setEvaluations(evaluations);
        context.setCapabilities(capabilities);
        if (context.isPersisted() && !context.isBuilt()) {
            serializeAnalyzers(context);
        }
        context.setBuilt(Boolean.TRUE);
    }

    private String evaluate(final Clusterer clusterer, final Instances instances) throws Exception {
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(instances);
        return clusterEvaluation.clusterResultsToString();
    }

    private String evaluate(final Classifier classifier, final Instances instances) throws Exception {
        Evaluation evaluation = new Evaluation(instances);
        evaluation.crossValidateModel(classifier, instances, 3, new Random());
        evaluation.evaluateModel(classifier, instances);
        return evaluation.toSummaryString();
    }

    /**
     * This method returns the distribution for the instance. The distribution is the probability that the instance
     * falls into either the classification or cluster category, and suggests the classification or cluster of the instance.
     *
     * @param context  the context for the analyzer, holds all the components, the classifier, the model and the options etc.
     * @param instance the instance to get the distribution for
     * @return the probability distribution for the instance over the classesOrClusters or clusters
     * @throws Exception
     */
    abstract double[][] distributionForInstance(final Context context, final Instance instance) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeForClassOrCluster(final Context context, final Analysis analysis) throws Exception {
        int sizeForClassOrCluster = 0;
        Instances[] models = (Instances[]) context.getModels();
        for (final Instances instances : models) {
            Attribute classAttribute = instances.classAttribute();
            Enumeration enumeration = instances.enumerateInstances();
            while (enumeration.hasMoreElements()) {
                Instance instance = (Instance) enumeration.nextElement();
                int classAttributeIndex = (int) instance.value(classAttribute.index());
                Object value = classAttribute.value(classAttributeIndex);
                if (analysis.getClazz().equals(value.toString())) {
                    sizeForClassOrCluster++;
                }
            }
        }
        return sizeForClassOrCluster;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(final Context context) throws Exception {
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Instances instances = (Instances) context.getModels()[i];
            instances.delete();
        }
    }

    /**
     * This method will create an instance from the input string. The string is assumed to be a comma separated list of values, with the same dimensions as the
     * attributes in the instances data set. If not, then the results are undefined.
     *
     * @param input the input string, a comma separated list of values, i.e. '35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES'
     * @return the instance, with the attributes set to the values of the tokens in the input string
     */
    Instance instance(final Object input, final Instances instances) {
        Instance instance = new Instance(instances.numAttributes());
        Object[] values = null;
        if (String.class.isAssignableFrom(input.getClass())) {
            values = StringUtils.split((String) input, ',');
        } else if (input.getClass().isArray()) {
            values = (Object[]) input;
        }
        assert values != null;
        for (int i = instances.numAttributes() - 1, j = values.length - 1; i >= 1 && j >= 0; i--, j--) {
            Object value = values[j];
            Attribute attribute = instances.attribute(i);
            if (attribute.isNumeric()) {
                instance.setValue(attribute, Double.parseDouble(value.toString()));
            } else if (attribute.isString()) {
                instance.setValue(attribute, attribute.addStringValue(value.toString()));
            } else {
                if (StringUtilities.isNumeric(value.toString())) {
                    instance.setValue(attribute, Double.parseDouble(value.toString()));
                } else {
                    instance.setValue(attribute, value.toString());
                }
            }
        }
        // instance.setMissing(0);
        instance.setDataset(instances);
        return instance;
    }

    /**
     * This method is for accessing the training/structure file and instantiating an {@link Instances} object.
     *
     * @param context the configuration object to build the instances object from
     * @return the instances object built from the arff training and structure file
     * @throws IOException
     */
    Instances[] instances(final Context context) throws IOException {
        InputStream[] inputStreams = getInputStreams(context);
        Instances[] instances = new Instances[context.getAlgorithms().length];
        for (int i = 0; inputStreams != null && i < inputStreams.length; i++) {
            try {
                if (inputStreams[i] == null) {
                    continue;
                }
                InputStream inputStream = inputStreams[i];
                try {
                    Reader reader = new InputStreamReader(inputStream);
                    instances[i] = new Instances(reader);
                    instances[i].setRelationName("instances");
                } finally {
                    if (inputStream != null) {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
            } catch (final Exception e) {
                logger.error("Exception building analyzer : " + context.getName(), e);
                throw new RuntimeException(e);
            }
        }
        return instances;
    }

    /**
     * This method gets the input stream to the data file or alternatively creates an input stream from the input in
     * the context. Typically when the latter is the case the analyzer is being trained via the rest API.
     *
     * @param context the context for the analyzer
     * @return the input stream either to the data file for training or a stream from the input in the context
     * @throws FileNotFoundException
     */
    InputStream[] getInputStreams(final Context context) throws FileNotFoundException {
        InputStream[] inputStreams = new InputStream[context.getAlgorithms().length];
        if (context.getTrainingDatas() != null) {
            for (int i = 0; i < context.getTrainingDatas().length; i++) {
                inputStreams[i] = new ByteArrayInputStream(context.getTrainingDatas()[i].getBytes());
            }
        } else {
            File[] dataFiles = getDataFiles(context);
            for (int i = 0; i < dataFiles.length; i++) {
                inputStreams[i] = new FileInputStream(dataFiles[i]);
            }
        }
        return inputStreams;
    }

    File[] serializeAnalyzers(final Context context) {
        String[] dataFileNames = context.getFileNames();
        File[] serializedAnalyzerFiles = new File[dataFileNames.length];
        File configurationDirectory = getOrCreateDirectory(new File(configFilePath).getParentFile());
        File serializationDirectory = getOrCreateDirectory(new File(configurationDirectory, context.getName()));
        Object[] analyzers = context.getAlgorithms();
        for (int i = 0; i < analyzers.length; i++) {
            Object analyzer = analyzers[i];
            String fileName = getBaseName(dataFileNames[i]) + IConstants.ANALYZER_SERIALIZED_FILE_EXTENSION;
            File serializedAnalyzerFile = getOrCreateFile(new File(serializationDirectory, fileName));
            serializedAnalyzerFiles[i] = serializedAnalyzerFile;
            logger.info("Serializing analyzer to file : " + serializedAnalyzerFile.getPath());
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(serializedAnalyzerFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(analyzer);
            } catch (final Exception e) {
                logger.error("Exception serializing analyzer : " + serializedAnalyzerFile + ", " + context.getName(), e);
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }
        return serializedAnalyzerFiles;
    }

    Object[] deserializeAnalyzers(final Context context) {
        List<Object> analyzers = Lists.newArrayList();
        File[] serializedAnalyzerFiles = getSerializedAnalyzerFiles(context);
        if (serializedAnalyzerFiles == null || serializedAnalyzerFiles.length == 0 ||
                serializedAnalyzerFiles.length != context.getAlgorithms().length) {
            logger.info("No analyzer files found : " + context.getName() + ", " + Arrays.toString(serializedAnalyzerFiles));
            return null;
        }
        for (final File serializedAnalyzerFile : serializedAnalyzerFiles) {
            FileInputStream fileInputStream = null;
            try {
                logger.info("De-serialising : " + serializedAnalyzerFile);
                fileInputStream = new FileInputStream(serializedAnalyzerFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                Object analyzer = objectInputStream.readObject();
                analyzers.add(analyzer);
            } catch (final Exception e) {
                logger.error("Exception de-serializing analyzer : " + serializedAnalyzerFile + ", " + context.getName(), e);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            }
        }
        return analyzers.toArray(new Object[analyzers.size()]);
    }

    File[] getSerializedAnalyzerFiles(final Context context) {
        File configurationDirectory = new File(configFilePath).getParentFile();
        File serializationDirectory = getOrCreateDirectory(new File(configurationDirectory, context.getName()));
        List<File> dataFiles = Lists.newArrayList();
        findFilesRecursively(serializationDirectory, dataFiles, IConstants.ANALYZER_SERIALIZED_FILE_EXTENSION);
        return dataFiles.toArray(new File[dataFiles.size()]);
    }

    /**
     * This method will filter the instance using the filter defined. Ultimately the filter changes the input
     * instance into an instance that is useful for the analyzer. For example in the case of a SVM classifier, the
     * support vectors are exactly that, vectors of doubles. If we are trying to classify text, we need to change(filter)
     * the text from words to feature vectors, most likely using the tf-idf logic. The filter essentially does that
     * for us in this method.
     *
     * @param instance the instance to filter into the correct form for the analyser
     * @param filter   the filter to use for the transformation
     * @return the filtered instance that is usable in the analyzer
     * @throws Exception
     */
    synchronized Instance filter(final Instance instance, final Filter filter) throws Exception {
        // Filter from string to inverse vector if necessary
        Instance filteredData;
        if (filter == null) {
            filteredData = instance;
        } else {
            filter.input(instance);
            filteredData = filter.output();
        }
        return filteredData;
    }

    /**
     * As with the {@link ikube.analytics.weka.WekaAnalyzer#filter(weka.core.Instance, weka.filters.Filter)} method, this method filters
     * the entire data set into something that is usable. Typically this is used in the training faze of the logic when the 'raw' data set
     * needs to be transformed into a matrix that can be used for training the analyzer.
     *
     * @param instances the instances that are to be transformed using the filter
     * @param filter    the filter to use for the transformation
     * @return the transformed instances object, ready to be used in training the classifier
     * @throws Exception
     */
    synchronized Instances filter(final Instances instances, final Filter filter) throws Exception {
        Instances filteredData;
        if (filter == null) {
            filteredData = instances;
        } else {
            filter.setInputFormat(instances);
            filteredData = Filter.useFilter(instances, filter);
        }
        return filteredData;
    }

    /**
     * This method will return the distribution for the entire data set. The distribution is the probability of the
     * variable being in the specific class or cluster in the data set.
     *
     * @param instances the data set of instances to get the distribution for, of the individual instances of course
     * @return the total distribution for all the instances in the data set
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    double[][][] getDistributionForInstances(final Context context, final Instances instances) throws Exception {
        double[][][] distributionForInstances = new double[instances.numInstances()][][];
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            double[][] distributionForInstance = distributionForInstance(context, instance);
            distributionForInstances[i] = distributionForInstance;
        }
        return distributionForInstances;
    }

    Filter getFilter(final Context context, final int i) {
        Object[] filters = context.getFilters();
        Filter filter = null;
        if (filters != null && filters.length > i) {
            filter = (Filter) filters[i];
        }
        return filter;
    }

}