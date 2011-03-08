package ikube.action.rule;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AnyServersWorking implements IRule<IndexContext> {

	@Override
	public boolean evaluate(final IndexContext indexContext) {
		return ApplicationContextManager.getBean(IClusterManager.class).anyWorking();
	}

}
