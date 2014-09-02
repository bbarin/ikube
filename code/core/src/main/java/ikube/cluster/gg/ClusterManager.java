package ikube.cluster.gg;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.model.Action;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.gridgain.grid.*;
import org.gridgain.grid.cache.GridCache;
import org.gridgain.grid.compute.*;
import org.gridgain.grid.messaging.GridMessaging;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * This is the GridGain implementation of the cluster manager.
 *
 * @author Michael Couck
 * @version 01.00
 * @see ikube.cluster.IClusterManager
 * @since 15-08-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class ClusterManager extends AClusterManager {

    private Grid grid;

    public void initialize() {
        ip = UriUtilities.getIp();
        grid = GridGain.grid(IConstants.IKUBE);
        Collection<GridNode> gridNodes = grid.nodes();
        for (final GridNode gridNode : gridNodes) {
            logger.info("Grid node : " + gridNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public synchronized boolean lock(final String name) {
        try {
            GridCache<String, String> gridCache = grid.cache(IConstants.IKUBE);
            return gridCache.lock(IConstants.IKUBE, 250);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public synchronized boolean unlock(final String name) {
        try {
            GridCache<String, String> gridCache = grid.cache(IConstants.IKUBE);
            gridCache.unlock(IConstants.IKUBE);
            return Boolean.TRUE;
        } catch (final GridException e) {
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean anyWorking() {
        boolean working = Boolean.FALSE;
        Map<String, Server> servers = getServers();
        for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
            working |= mapEntry.getValue().isWorking();
        }
        return working;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean anyWorking(final String indexName) {
        boolean working = Boolean.FALSE;
        Map<String, Server> servers = getServers();
        for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
            Server server = mapEntry.getValue();
            if (server.isWorking() && server.getActions() != null) {
                for (final Action action : server.getActions()) {
                    working |= indexName.equals(action.getIndexName());
                }
            }
        }
        return working;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public synchronized Action startWorking(final String actionName, final String indexName, final String indexableName) {
        try {
            Server server = getServer();
            Action action = getAction(actionName, indexName, indexableName);
            server.getActions().add(action);
            GridCache<String, Server> gridCache = grid.cache(IConstants.SERVER);
            gridCache.put(address, server);
            return action;
        } catch (final Exception e) {
            logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public synchronized void stopWorking(final Action action) {
        try {
            Server server = getServer();
            server.getActions().remove(action);
            GridCache<String, Server> gridCache = grid.cache(IConstants.SERVER);
            gridCache.put(address, server);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public Map<String, Server> getServers() {
        try {
            GridCache<String, Server> gridCache = grid.cache(IConstants.SERVER);
            Set<String> keys = gridCache.keySet();
            return gridCache.getAll(keys);
        } catch (final GridException e) {
            logger.error("Exception getting the cache : " + IConstants.SERVER, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Server getServer() {
        return getServers().get(this.address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(final Serializable serializable) {
        try {
            GridMessaging gridMessaging = grid.message();
            gridMessaging.send(IConstants.IKUBE, serializable);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> sendTask(final Callable<T> callable) {
        GridCompute gridCompute = grid.compute();
        GridFuture<?> gridFuture = gridCompute.call(callable);
        return wrapFuture(gridFuture);
    }

    @SuppressWarnings("unchecked")
    <T> Future<T> wrapFuture(final GridFuture<?> gridFuture) {
        return (Future<T>) ThreadUtilities.submit(IConstants.IKUBE, new Runnable() {
            public void run() {
                while (!gridFuture.isDone() && !gridFuture.isCancelled()) {
                    try {
                        gridFuture.get();
                    } catch (final GridException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> sendTaskTo(final Server server, final Callable<T> callable) {
        GridCompute gridCompute = grid.compute();
        Collection<GridNode> gridNodes = grid.nodes();
        GridNode gridNode = gridNodes.iterator().next();
//        gridCompute.execute(new GridComputeTask<Object, Object>() {
//            @Nullable
//            @Override
//            public Map<? extends GridComputeJob, GridNode> map(final List<GridNode> subgrid, @Nullable final Object arg) throws GridException {
//                return null;
//            }
//
//            @Override
//            public GridComputeJobResultPolicy result(final GridComputeJobResult res, final List<GridComputeJobResult> rcvd) throws GridException {
//                return null;
//            }
//
//            @Nullable
//            @Override
//            public Object reduce(final List<GridComputeJobResult> results) throws GridException {
//                return null;
//            }
//        });
        throw new RuntimeException("Couldn't find member with address : " + server.getAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Future<T>> sendTaskToAll(final Callable<T> callable) {
        GridCompute gridCompute = grid.compute();
        GridFuture<T> gridFuture = (GridFuture<T>) gridCompute.broadcast(callable);
        List<Future<T>> futures = new ArrayList<>();
        futures.add((Future<T>) wrapFuture(gridFuture));
        return futures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public Object get(final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(IConstants.IKUBE);
            return gridCache.get(key);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void put(final Object key, final Serializable value) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(IConstants.IKUBE);
            gridCache.put(key, value);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public void remove(final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(IConstants.IKUBE);
            gridCache.remove(key, get(key));
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public void clear(final String map) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            gridCache.removeAll();
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public <T> T get(final String map, final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            return (T) gridCache.get(key);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void put(final String map, final Object key, final Serializable value) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            gridCache.put(key, value);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public void remove(final String map, final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            gridCache.remove(key, get(key));
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        try {
            grid.close();
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

}