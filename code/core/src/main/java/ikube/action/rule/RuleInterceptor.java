package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Rule;
import ikube.toolkit.THREAD;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is implemented as an interceptor, and typically configured in Spring. The interceptor will intercept the
 * execution of the actions, like {@link ikube.action.Index} and {@link ikube.action.Open}. Each action has associated with
 * it rules, like whether any other servers are currently working on this index or if the index is current and already open.
 * The rules for the action will then be executed, and based on the category of the boolean predicate parametrized with the
 * results of each rule, the action will either be executed or not. {@link org.apache.commons.jexl2.JexlEngine} is the
 * expression parser for the rules.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IRuleInterceptor
 * @since 12-02-2011
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class RuleInterceptor implements IRuleInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleInterceptor.class);

    @Autowired
    private IDataBase dataBase;
    @Autowired
    @Qualifier("ikube.cluster.IClusterManager")
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public synchronized Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            Object target = proceedingJoinPoint.getTarget();
            boolean proceed = Boolean.TRUE;
            IndexContext indexContext = null;
            if (!IAction.class.isAssignableFrom(target.getClass())) {
                LOGGER.warn("Can't intercept non action class, proceeding : " + target);
            } else {
                IAction action = (IAction) target;
                try {
                    boolean gotLock = Boolean.TRUE;
                    if (action.requiresClusterLock()) {
                        gotLock = clusterManager.lock(IConstants.IKUBE);
                    }
                    if (gotLock) {
                        // Find the index context
                        indexContext = getIndexContext(proceedingJoinPoint);
                        proceed = evaluateRules(indexContext, action);
                    } else {
                        proceed = Boolean.FALSE;
                    }
                } catch (final NullPointerException e) {
                    LOGGER.warn("Context closing down : ");
                } catch (final Exception t) {
                    LOGGER.error("Exception proceeding on target : " + target, t);
                } finally {
                    clusterManager.unlock(IConstants.IKUBE);
                }
            }
            if (proceed) {
                proceed(indexContext, proceedingJoinPoint);
            }
        } catch (final Exception e) {
            LOGGER.error("Exception in rule interceptor : ", e);
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
        return Boolean.TRUE;
    }

    /**
     * Proceeds on the join point. A scheduled task will be started by the scheduler. The task is the action that
     * has been given the green light to start. The current thread will wait for the action to complete, but will only
     * wait for a few seconds then continue. The action is started in a separate thread because we don't want a
     * queue of actions building up.
     *
     * @param proceedingJoinPoint the intercepted action join point
     */
    protected synchronized void proceed(final IndexContext indexContext, final ProceedingJoinPoint proceedingJoinPoint) {
        try {
            // We set the working flag in the action within the cluster lock when setting to true
            Runnable runnable = new Runnable() {
                public void run() {
                    Action action = null;
                    try {
                        // Start the action in the cluster
                        String indexName = indexContext.getIndexName();
                        String actionName = proceedingJoinPoint.getTarget().getClass().getSimpleName();
                        action = clusterManager.startWorking(actionName, indexName, null);
                        // Execute the action logic
                        proceedingJoinPoint.proceed();
                    } catch (final Throwable e) {
                        LOGGER.error("Exception proceeding on join point : " + proceedingJoinPoint, e);
                    } finally {
                        // Remove the action in the cluster
                        clusterManager.stopWorking(action);
                        THREAD.destroy(this.toString());
                    }
                }
            };
            THREAD.submit(runnable.toString(), runnable);
        } finally {
            notifyAll();
        }
    }

    /**
     * This method will take the rules for the action and execute them, returning the category from the boolean rule predicate.
     *
     * @param indexContext the index context for the index
     * @param action       the action who's rules are to be executed
     * @return the category from the execution of the rules for the action
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean evaluateRules(final IndexContext indexContext, final IAction action) {
        LOGGER.debug("Evaluating rules : ");
        boolean finalResult = Boolean.TRUE;
        // Get the rules associated with this action
        List<IRule<IndexContext>> rules = action.getRules();
        if (rules == null || rules.size() == 0) {
            LOGGER.info("No rules defined, proceeding : " + action);
        } else {
            List<Boolean> evaluations = new ArrayList<>();
            Map<String, Object> results = new HashMap<>();
            JexlEngine jexlEngine = new JexlEngine();
            JexlContext jexlContext = new MapContext(results);
            for (final IRule<IndexContext> rule : rules) {
                boolean evaluation = rule.evaluate(indexContext);
                String ruleName = rule.getClass().getSimpleName();
                jexlContext.set(ruleName, evaluation);
                evaluations.add(evaluation);
            }
            String predicate = action.getRuleExpression();
            Expression expression = jexlEngine.createExpression(predicate);
            Object result = expression.evaluate(jexlContext);
            finalResult = result != null && (result.equals(1.0d) || result.equals(Boolean.TRUE));

            Rule rule = new Rule();
            rule.setAction(action.getClass().getSimpleName());
            rule.setServer(clusterManager.getServer().getAddress());
            rule.setIndexContext(indexContext.getName());

            rule.setPredicate(predicate);

            rule.setResult(finalResult);
            rule.setEvaluations(evaluations);

            dataBase.persist(rule);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Going to log : " + finalResult);
                log(indexContext, action, predicate, finalResult, results);
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
    protected IndexContext getIndexContext(final ProceedingJoinPoint proceedingJoinPoint) {
        Object[] args = proceedingJoinPoint.getArgs();
        for (final Object arg : args) {
            if (arg != null) {
                if (IndexContext.class.isAssignableFrom(arg.getClass())) {
                    return (IndexContext) arg;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    void log(
            final IndexContext indexContext,
            final IAction action,
            final String predicate,
            final boolean result,
            final Map<String, Object> results) {
        LOGGER.info("Rule evaluation of index : " + indexContext.getName() +
                ", action : " + action.getClass().getSimpleName() +
                ", predicate : " + predicate);
        LOGGER.info("Rules evaluation result : " + result + ", results : " + results);
    }

}