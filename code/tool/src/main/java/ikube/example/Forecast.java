package ikube.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This example of prediction is for stock market time series prediction, using the Weka framework. The market data
 * is taken from Yahoo! and at the time of writing was accurate. The results can be validated against the values that
 * the market had in the following days.
 * <p/>
 * The process is as follows:
 * <p/>
 * <pre>
 *     * Create a context with the name, the analyzer type, in this
 *       case the WekaForecastClassifier, and the training data in csv format
 *     * Convert the context to Json format using the Gson library from Google
 *     * Execute the create method on the rest service
 *     * Create an analysis object with the name of the analyzer that has just been created, and
 *       the options for the prediction, in this case is the forecast field, i.e. the price, the timestamp
 *       field for the training data, min and max lag for the time periods and forecasts for how many days
 *       to forecast
 *     * Execute the analyze method on the rest service, the result will be the output field in the
 *       response object, which is an array of the predicted stock prices
 * </pre>
 * <p/>
 * These methods can of course be executed in a browser using the Json generated from the context
 * and analysis object, and as such can be used from any platform and language that can access the
 * internet.
 * <p/>
 * Urls at the time for the data were:
 * <pre>
 *      http://download.finance.yahoo.com/d/quotes.csv?s=@%5EIXIC&f=sl1d1t1c1ohgv&e=.csv&h=0
 *      finance.yahoo.com/d/quotes.csv?s=DKIENGKEUO.CO&f=snl1d1t1cc1p2pohgvmlt7a2ba
 *
 *      NOTE: This works:
 *      http://ichart.finance.yahoo.com/table.csv?s=GOOG
 *      http://ichart.finance.yahoo.com/table.csv?s=INDO.PA
 *      http://ichart.finance.yahoo.com/table.csv?s=NDXEX.GR
 *      http://ichart.yahoo.com/table.csv?s=BAS.DE&a=0&b=1&c=2005&d=0&e=31&f=2015&g=w&ignore=.csv
 *      http://ichart.yahoo.com/table.csv?s=BAS.DE&a=0&b=1&c=2000&d=0&e=31&f=2010&g=w
 *      http://ichart.yahoo.com/table.csv?s=INDO.PA&a=0&b=1&c=2010&d=0&e=31&f=2015&g=w
 *
 *      http://ichart.yahoo.com/table.csv?s=GOOG&d=8&e=26&f=2009&g=d&a=2&b=27&c=2014&ignore=.csv
 *
 *      http://real-chart.finance.yahoo.com/table.csv?s=GOOG&d=8&e=26&f=2014&g=d&a=2&b=27&c=2014&ignore=.csv
 *
 *      This is to get all the stocks in an xml file:
 *      http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.industry%20where%20id%20in%20(
 *          select%20industry.id%20from%20yahoo.finance.sectors)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys
 *
 *
 *      NOTE: For an individual quote
 *      http://finance.yahoo.com/webservice/v1/symbols/YHOO/quote?format=json
 *      http://finance.yahoo.com/webservice/v1/symbols/INDO.PA/quote?format=json&view=detail
 *      https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20%28%22INDO.PA%22%29&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2014
 */
public class Forecast extends Base {

    public static void main(final String[] args) throws IOException {
        Analysis analysis = doAnalysis(args);
        // The result should be something like: This represents the forecast price for the shares specified in the training data
        // [[[579.3721684789788],[581.4060746802609],[583.233603088952],[584.8823713779697],[586.3763013969173]]]
        System.out.println("Analysis response : " + ToStringBuilder.reflectionToString(analysis));
    }

    static Analysis doAnalysis(final String[] args) throws IOException {
        // The Jersey client for accessing the rest service
        Client client = Client.create();
        // For converting from Java to Json
        Gson gson = new GsonBuilder().create();

        String inputFilePath;
        if (args != null && args.length > 0) {
            inputFilePath = args[0];
        } else {
            inputFilePath = "code/tool/src/main/resources/anal/example/forecast.csv";
        }

        // Get the training data from the file
        File trainingDataFile = new File(inputFilePath);
        // Create the context for the creation
        Context context = new Context();
        context.name = "forecast-classifier";
        context.analyzer = "ikube.analytics.weka.WekaForecastClassifier";
//        context.trainingDatas = new String[]{
//                "1970-01-01-07-33-19,-3582976.380952381,6,850,0.07636904761904763,21,0.022793088322130776\n" +
//                "1970-01-01-07-33-20,-22115.29411764706,6,878,0.150845588235294,68,0.019966395606248605\n" +
//                "1970-01-01-07-33-21,28405.647058823528,6,879,0.19670955882352956,68,0.019554364021466673\n" +
//                "1970-01-01-07-33-22,-15182.117647058823,7,881,0.1949999999999999,68,0.019625725690173686\n" +
//                "1970-01-01-07-33-23,30973.882352941175,7,880,0.21064338235294106,68,0.019585168128898375\n" +
//                "1970-01-01-07-33-24,-89492.0,6,814,0.18812499999999996,12,0.01782114113912367"};
        context.trainingDatas = new String[]{IOUtils.toString(new FileInputStream(trainingDataFile))};

        // Create the analyzer, and initialize with the training data for the stock market prediction
        String body = gson.toJson(context);
        System.out.println("Create body : " + body);
        WebResource webResource = client.resource("http://ikube.be/ikube/service/analyzer/create");
        String response = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(String.class, body);
        System.out.println("Create response : " + response);

        // Use the analyzer to get a specified number of predictions, in this case days into the future, 5 day forecast
        Analysis analysis = new Analysis();
        analysis.context = context.name;
        analysis.input = "-fieldsToForecast,6,-timeStampField,0,-minLag,1,-maxLag,1,-forecasts,6";
        body = gson.toJson(analysis);
        System.out.println("Analysis body : " + body);
        webResource = client.resource("http://ikube.be/ikube/service/analyzer/analyze");
        response = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(String.class, body);

        return gson.fromJson(response, Analysis.class);
    }

}