package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.THREAD;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;

import static junit.framework.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14-11-2013
 */
public class WekaClustererTest extends AbstractTest {

    private Context context;
    @Spy
    @InjectMocks
    private WekaClusterer wekaClusterer;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        String algorithm = EM.class.getName();
        String[] options = new String[]{"-N", "6"};
        String fileName = "bmw-browsers.arff";
        int maxTraining = Integer.MAX_VALUE;

        context = new Context();
        context.setName("clusterer");
        context.setAnalyzer(WekaClusterer.class.getName());
        context.setAlgorithms(algorithm, algorithm, algorithm);
        context.setOptions(options);
        context.setFileNames(fileName, fileName, fileName);
        context.setMaxTrainings(maxTraining, maxTraining, maxTraining);

        wekaClusterer.init(context);
    }

    @Test
    public void build() throws Exception {
        wekaClusterer.build(context);
        THREAD.sleep(5000);
        assertNotNull(context.getAnalyzer());
    }

    @Test
    public void analyze() throws Exception {
        wekaClusterer.build(context);
        THREAD.sleep(1000);
        Analysis<Object, Object> analysis = getAnalysis(null, "1,0,1,1,1,1,1,1");

        Analysis<Object, Object> result = wekaClusterer.analyze(context, analysis);
        assertNotNull(result.getClazz());
        assertNotNull(result.getOutput());
    }

    @Test
    public void getDistributionForInstances() throws Exception {
        wekaClusterer.build(context);
        THREAD.sleep(1000);
        for (final Object model : context.getModels()) {
            Instances instances = (Instances) model;
            // printInstances(instances);
            double[][][] distributionForInstances = wekaClusterer.getDistributionForInstances(context, instances);
            for (final double[][] distributionForInstance : distributionForInstances) {
                logger.debug("Probability : " + Arrays.toString(distributionForInstance));
                for (final double[] distribution : distributionForInstance) {
                    for (final double probability : distribution) {
                        assertTrue(probability >= 0 && probability <= 1.0);
                    }
                }
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void printInstances(final Instances instances) {
        logger.info("Num attributes : " + instances.numAttributes());
        logger.info("Num instances : " + instances.numInstances());
        logger.info("Sum of weights : " + instances.sumOfWeights());
        for (int i = 0; i < instances.numAttributes(); i++) {
            Attribute attribute = instances.attribute(i);
            logger.info("Attribute : " + attribute);
        }
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            logger.info("Instance : " + instance);
        }
    }

}