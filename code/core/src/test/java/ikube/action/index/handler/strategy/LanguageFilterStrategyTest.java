package ikube.action.index.handler.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.Indexable;
import ikube.toolkit.PerformanceTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 03.12.13
 * @version 01.00
 */
public class LanguageFilterStrategyTest extends AbstractTest {

	private LanguageFilterStrategy languageFilterStrategy;

	@Before
	public void before() {
		languageFilterStrategy = new LanguageFilterStrategy();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void aroundProcess() throws Exception {
		String content = "some english text $   ù'àé .class  $$class.again";
		String cleanedContent = languageFilterStrategy.cleanContent(content);
		assertEquals("some english text ù àé class class again", cleanedContent);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void aroundProcessPerformance() {
		when(indexableColumn.getContent()).thenReturn("some english text");
		List<Indexable<?>> children = new ArrayList(Arrays.asList(indexableColumn));
		when(indexableTable.getChildren()).thenReturn(children);

		int iterations = 1000;
		final Document document = new Document();
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				languageFilterStrategy.aroundProcess(indexContext, indexableTable, document, null);
			}
		}, "Language detection strategy : ", iterations, Boolean.TRUE);
		assertTrue(perSecond > 1000);
	}

}