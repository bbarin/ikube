package ikube.model;

import weka.classifiers.functions.SMO;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

/**
 * This class represents data that is to be analyzed as well as the results from the analysis if any.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Analysis<Input, Output> extends Distributed {

    /**
     * The name of the analyzer in the system, for example clusterer-em. This corresponds to
     * the {@link ikube.model.Context} name, so if the context name in the Spring configuration is
     * sentiment-smo-en then the analysis analyzer field name must be the same. Note also that this field
     * and the context field must be the same as the Spring bean id/name too.
     */
    private String analyzer;
    /**
     * The class/cluster for the instance, this is the result of the analysis.
     */
    private String clazz;
    /**
     * The input data to be analyzed, string text, or an array of dates and numbers, whatever, but typically
     * in the underlying function format. In the case of Weka it is the Weka format of course.
     */
    private Input input;
    /**
     * The output data, could be a string or a double array for distribution.
     */
    private Output output;
    /**
     * An algorithm specific output, could be toString from the {@link SMO} function.
     */
    private String algorithmOutput;
    /**
     * The correlation co-efficients for the data set, matching the first instance against the next.
     */
    @Transient
    private double[] correlationCoefficients;
    /**
     * The distribution probabilities of the instance in the clusters/class categories.
     */
    @Transient
    private double[][] distributionForInstances;
    /**
     * The classes or clusters available for the classifier or clusterer.
     */
    @Transient
    private Object[] classesOrClusters;
    /**
     * The size of each class or cluster in the classifier or clusterer.
     */
    @Transient
    private int[] sizesForClassesOrClusters;

    @Transient
    private transient Exception exception;

    /**
     * The time taken for the analysis.
     */
    private double duration;

    /**
     * Whether to get the algorithm output with the results.
     */
    private boolean algorithm;
    /**
     * Whether to get the correlation co-efficients for the instances.
     */
    private boolean correlation;
    /**
     * Whether to get the distribution for the instances, very expensive.
     */
    private boolean distribution;
    /**
     * Whether to get the classes or clusters for the instance.
     */
    private boolean classesAndClusters;
    /**
     * Whether to get the sizes for the classes or clusters for the instances.
     */
    private boolean sizesForClassesAndClusters;

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public String getAlgorithmOutput() {
        return algorithmOutput;
    }

    public void setAlgorithmOutput(String algorithmOutput) {
        this.algorithmOutput = algorithmOutput;
    }

    public boolean isAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(boolean algorithm) {
        this.algorithm = algorithm;
    }

    public double[] getCorrelationCoefficients() {
        return correlationCoefficients;
    }

    public void setCorrelationCoefficients(double[] correlationCoefficients) {
        this.correlationCoefficients = correlationCoefficients;
    }

    public double[][] getDistributionForInstances() {
        return distributionForInstances;
    }

    public void setDistributionForInstances(double[][] distributionForInstances) {
        this.distributionForInstances = distributionForInstances;
    }

    public Object[] getClassesOrClusters() {
        return classesOrClusters;
    }

    public void setClassesOrClusters(Object[] classesOrClusters) {
        this.classesOrClusters = classesOrClusters;
    }

    public int[] getSizesForClassesOrClusters() {
        return sizesForClassesOrClusters;
    }

    public void setSizesForClassesOrClusters(int[] sizesForClassesOrClusters) {
        this.sizesForClassesOrClusters = sizesForClassesOrClusters;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public boolean isCorrelation() {
        return correlation;
    }

    public void setCorrelation(boolean correlation) {
        this.correlation = correlation;
    }

    public boolean isDistribution() {
        return distribution;
    }

    public void setDistribution(boolean distribution) {
        this.distribution = distribution;
    }

    public boolean isClassesAndClusters() {
        return classesAndClusters;
    }

    public void setClassesAndClusters(boolean classesAndClusters) {
        this.classesAndClusters = classesAndClusters;
    }

    public boolean isSizesForClassesAndClusters() {
        return sizesForClassesAndClusters;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSizesForClassesAndClusters(boolean sizesForClassesAndClusters) {
        this.sizesForClassesAndClusters = sizesForClassesAndClusters;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}