package ikube;

import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FILE;
import ikube.toolkit.LOGGING;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.SimpleKMeans;

import java.io.File;

/**
 * This is the base test class for the unit tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractTest {

    public static final String SERVICE = "/service";
    protected static int SERVER_PORT = 9090;
    protected static String LOCALHOST = "localhost";
    protected static String REST_PASSWORD = "user";
    protected static String REST_USER_NAME = "user";
    /**
     * This client({@link org.apache.http.client.HttpClient}) is for the web services.
     */
    protected static HttpClient HTTP_CLIENT = new DefaultHttpClient();

    static {
        LOGGING.configure();
        // Mockit.setUpMocks(SpellingCheckerMock.class);
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Context getContext(final String fileName, final String name) throws Exception {
        File trainingDataFile = FILE.findFileRecursively(new File("."), fileName);
        String trainingData = FILE.getContent(trainingDataFile);

        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(WekaClusterer.class.getName());
        context.setAlgorithms(SimpleKMeans.class.getName());
        context.setOptions("-N", "6");

        context.setTrainingDatas(trainingData);
        context.setMaxTrainings(Integer.MAX_VALUE);

        return context;
    }

    protected Analysis getAnalysis(final String context, final String input) {
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setContext(context);
        analysis.setInput(input);
        return analysis;
    }

}
