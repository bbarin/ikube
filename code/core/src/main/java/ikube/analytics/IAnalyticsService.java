package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Context;

import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public interface IAnalyticsService<I, O> {

    IAnalyzer<I, O> create(final Context context);

    IAnalyzer<I, O> train(final Analysis<I, O> analysis);

    IAnalyzer<I, O> build(final Context context);

    Analysis<I, O> analyze(final Analysis<I, O> analysis);

    IAnalyzer<I, O> destroy(final Context context);

    Map<String, IAnalyzer> getAnalyzers();

    Context getContext(final String name);

}