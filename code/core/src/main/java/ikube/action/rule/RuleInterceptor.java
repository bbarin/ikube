package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.action.Open;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.concurrent.Future;

import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is implemented as an intercepter, and typically configured in Spring. The intercepter will intercept the execution of the
 * actions, like {@link Index} and {@link Open}. Each action has associated with it rules, like whether any other servers are currently
 * working on this index or if the index is current and already open. The rules for the action will then be executed, and based on the
 * result of the boolean predicate parameterized with the results of each rule, the action will either be executed or not. {@link JEP} is
 * the expression parser for the rules.
 * 
 * @see IRuleInterceptor
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleInterceptor.class);

	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object target = proceedingJoinPoint.getTarget();
		boolean proceed = Boolean.FALSE;
		// During the execution of the rules the cluster needs to be locked completely for the duration of the evaluation of the rules
		// because there exists a race condition where the rules evaluate to true for server one, and evaluate to true for server two before
		// server one can set the values that would make server two evaluate to false, so they both start the action they shouldn't start.
		// Generally this will never happen because the timers will be different, but in a very small percentage of cases they overlap
		IndexContext<?> indexContext = null;
		try {
			if (!IAction.class.isAssignableFrom(target.getClass())) {
				LOGGER.warn("Can't intercept non action class, proceeding : " + target);
				proceed = Boolean.TRUE;
			} else if (!clusterManager.lock(IConstants.IKUBE)) {
				LOGGER.debug("Couldn't aquire lock : ");
				proceed = Boolean.FALSE;
			} else {
				// Find the index context
				indexContext = getIndexContext(proceedingJoinPoint);
				if (indexContext == null) {
					LOGGER.warn("Couldn't find the index context : " + proceedingJoinPoint);
				} else {
					LOGGER.debug("Aquired lock : ");
					@SuppressWarnings("rawtypes")
					IAction action = (IAction) target;
					proceed = evaluateRules(indexContext, action);
				}
			}
			if (proceed) {
				proceed(indexContext, proceedingJoinPoint);
			}
		} catch (NullPointerException e) {
			LOGGER.warn("Context closing down : ");
			LOGGER.debug(null, e);
		} catch (Exception t) {
			LOGGER.error("Exception evaluating the rules : target : " + target + ", context : " + indexContext, t);
		} finally {
			boolean unlocked = clusterManager.unlock(IConstants.IKUBE);
			LOGGER.debug("Unlocked : {} ", unlocked);
		}
		return proceed;
	}

	/**
	 * This method will take the rules for the action and execute them, returning the result from the boolean rule predicate.
	 * 
	 * @param indexContext the index context for the index
	 * @param action the action who's rules are to be executed
	 * @return the result from the execution of the rules for the action
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean evaluateRules(final IndexContext<?> indexContext, final IAction action) {
		boolean finalResult = Boolean.TRUE;
		// Get the rules associated with this action
		List<IRule<IndexContext<?>>> classRules = action.getRules();
		if (classRules == null || classRules.size() == 0) {
			LOGGER.info("No rules defined, proceeding : " + action);
		} else {
			JEP jep = new JEP();
			Object result = null;
			for (IRule<IndexContext<?>> rule : classRules) {
				boolean evaluation = rule.evaluate(indexContext);
				String ruleName = rule.getClass().getSimpleName();
				jep.addVariable(ruleName, evaluation);
				// LOGGER.info("Rule : rule {}, : name : {}, evaluation {} : ", new Object[] { rule, ruleName, evaluation });
			}
			String predicate = action.getRuleExpression();
			jep.parseExpression(predicate);
			if (jep.hasError()) {
				LOGGER.warn("Exception in Jep expression : " + jep.getErrorInfo());
				LOGGER.warn("Symbol table : " + jep.getSymbolTable());
			}
			result = jep.getValueAsObject();
			if (result == null) {
				result = jep.getValue();
			}
			finalResult = result != null && (result.equals(1.0d) || result.equals(Boolean.TRUE));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info("Rule intercepter proceeding : {} {} {}", new Object[] { finalResult, action, indexContext.getIndexName() });
				printSymbolTable(jep, indexContext.getIndexName());
			}
		}
		return finalResult;
	}

	/**
	 * This method iterates through the join point arguments and looks for the index context.
	 * 
	 * @param proceedingJoinPoint the intercepted action join point
	 * @return the index context from the arguments or null if it can not be found
	 */
	protected IndexContext<?> getIndexContext(final ProceedingJoinPoint proceedingJoinPoint) {
		Object[] args = proceedingJoinPoint.getArgs();
		for (Object arg : args) {
			if (arg != null) {
				if (IndexContext.class.isAssignableFrom(arg.getClass())) {
					return (IndexContext<?>) arg;
				}
			}
		}
		return null;
	}

	/**
	 * Proceeds on the join point. A scheduled task will be started by the scheduler. The task is the action that has been given the green
	 * light to start. The current thread will wait for the action to complete, but will only wait for a few seconds then continue. The
	 * action is started in a separate thread because we don't want a queue of actions building up.
	 * 
	 * @param proceedingJoinPoint the intercepted action join point
	 */
	protected synchronized void proceed(final IndexContext<?> indexContext, final ProceedingJoinPoint proceedingJoinPoint) {
		try {
			// We set the working flag in the action within the cluster lock when setting to true
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						Object returnValue = proceedingJoinPoint.proceed();
						LOGGER.debug("Returned from join point : " + returnValue);
					} catch (Throwable e) {
						LOGGER.error("Exception proceeding on join point : " + proceedingJoinPoint, e);
					} finally {
						if (LOGGER.isDebugEnabled()) {
							Object[] objects = new Object[] { proceedingJoinPoint, indexContext.getIndexName() };
							LOGGER.debug("Action finished : {} {}", objects);
						}
					}
				}
			};
			Future<?> future = ThreadUtilities.submit(runnable);
			// We'll wait a few seconds for this action, perhaps it is a fast one
			ThreadUtilities.waitForFuture(future, 3);
			LOGGER.debug("Finished waiting for future : {} ", future);
		} finally {
			notifyAll();
		}
	}

	protected void printSymbolTable(final JEP jep, final String indexName) {
		try {
			SymbolTable symbolTable = jep.getSymbolTable();
			LOGGER.info("Index : " + indexName);
			LOGGER.info("Symbol table : " + symbolTable);
		} catch (Exception e) {
			LOGGER.error("Exception printing the nodes : ", e);
		}
	}

}