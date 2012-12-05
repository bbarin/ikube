package ikube.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.Base;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;
import ikube.toolkit.PerformanceTester;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MonitorTest extends Base {

	private Monitor monitor;
	private IMonitorService monitorService;
	private IClusterManager clusterManager;
	@SuppressWarnings("rawtypes")
	private IndexContext indexContext;
	@Cascading
	private Server server;

	@Before
	public void before() {
		monitor = new Monitor();
		indexContext = getIndexContext();

		monitorService = Mockito.mock(IMonitorService.class);
		clusterManager = Mockito.mock(IClusterManager.class);
		Deencapsulation.setField(monitor, monitorService);
		Deencapsulation.setField(monitor, clusterManager);

		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void fields() throws Exception {
		Mockito.when(monitorService.getIndexFieldNames(Mockito.anyString())).thenReturn(new String[] { "one", "two", "three" });
		Response fields = monitor.fields("indexName");
		assertEquals("The string should be a concatenation of the fields : ", "[\"one\",\"two\",\"three\"]", fields.getEntity());
	}

	@Test
	public void indexContext() {
		Mockito.when(monitorService.getIndexContext(Mockito.anyString())).thenReturn(indexContext);
		Response indexContext = monitor.indexContext(IConstants.GEOSPATIAL);
		Object entity = indexContext.getEntity();
		assertTrue("The max age should be in the Json string : ", entity.toString().contains("maxAge"));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void indexContexts() {
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(IConstants.GEOSPATIAL, indexContext);

		Mockito.when(monitorService.getIndexContexts()).thenReturn(indexContexts);
		Mockito.when(monitorService.getIndexContext(Mockito.anyString())).thenReturn(indexContext);
		Response indexContext = monitor.indexContexts();
		Object entity = indexContext.getEntity();
		assertTrue("The max age should be in the Json string : ", entity.toString().contains("maxAge"));
	}

	@Test
	public void servers() {
		Map<String, Server> servers = new HashMap<String, Server>();
		servers.put(IConstants.IKUBE, server);

		Mockito.when(clusterManager.getServers()).thenReturn(servers);
		Response indexContext = monitor.servers();
		Object entity = indexContext.getEntity();
		assertTrue("The max age should be in the Json string : ", entity.toString().contains("averageCpuLoad"));
	}

	@MockClass(realClass = Server.class)
	public static class ServerMock {
		@Mock
		public boolean isWorking() {
			return Boolean.TRUE;
		}
	}

	@Test
	public void indexingStatistics() {
		Mockit.tearDownMocks();
		Mockit.setUpMocks(ServerMock.class);

		Map<String, Server> servers = getServers();

		Mockito.when(clusterManager.getServers()).thenReturn(servers);
		Response response = monitor.indexingStatistics();
		Object entity = response.getEntity();
		assertEquals(
				"[[\"Times\", \"127.0.0.1-8002\", \"127.0.0.1-8003\", \"127.0.0.1-8000\", \"127.0.0.1-8001\"], [\"1.1\", 3, 3, 3, 3], "
						+ "[\"1.2\", 6, 6, 6, 6], [\"1.3\", 9, 9, 9, 9]]", entity);

		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				monitor.indexingStatistics();
			}
		}, "Indexing statistics", 1000, Boolean.TRUE);
		assertTrue(perSecond > 100);
	}

	@Test
	public void searchingStatistics() {
		Mockit.tearDownMocks();
		Mockit.setUpMocks(ServerMock.class);

		Map<String, Server> servers = getServers();

		Mockito.when(clusterManager.getServers()).thenReturn(servers);
		Response response = monitor.searchingStatistics();
		Object entity = response.getEntity();
		assertEquals(
				"[[\"Times\", \"127.0.0.1-8002\", \"127.0.0.1-8003\", \"127.0.0.1-8000\", \"127.0.0.1-8001\"], [\"1.1\", 300, 300, 300, 300], "
						+ "[\"1.2\", 600, 600, 600, 600], [\"1.3\", 900, 900, 900, 900]]", entity);

		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				monitor.searchingStatistics();
			}
		}, "Indexing statistics", 1000, Boolean.TRUE);
		assertTrue(perSecond > 100);
	}

	@Test
	public void actions() {
		Mockit.tearDownMocks();
		Mockit.setUpMocks(ServerMock.class);
		Map<String, Server> servers = getServers();
		Mockito.when(clusterManager.getServers()).thenReturn(servers);
		Response response = monitor.actions();
		Object entity = response.getEntity();

		assertTrue(entity.toString().contains(
				"{\"result\":\"true\",\"docsPerMinute\":3,\"actionName\":\"action\",\"invocations\":\"2147483647\",\"startTime\""));
	}

	private Map<String, Server> getServers() {
		Server serverOne = getServer("127.0.0.1-8000");
		Server serverTwo = getServer("127.0.0.1-8001");
		Server serverThree = getServer("127.0.0.1-8002");
		Server serverFour = getServer("127.0.0.1-8003");

		serverOne.getActions().add(getAction(serverOne));
		serverTwo.getActions().add(getAction(serverTwo));
		serverThree.getActions().add(getAction(serverThree));
		serverFour.getActions().add(getAction(serverFour));

		Map<String, Server> servers = new HashMap<String, Server>();
		servers.put(serverOne.getAddress(), serverOne);
		servers.put(serverTwo.getAddress(), serverTwo);
		servers.put(serverThree.getAddress(), serverThree);
		servers.put(serverFour.getAddress(), serverFour);

		return servers;
	}

	private Action getAction(final Server server) {
		Action action = new Action();
		action.setActionName("action");
		action.setDuration(Integer.MAX_VALUE);
		action.setEndTime(null);
		action.setId(Integer.MAX_VALUE);
		action.setIndexableName("indexableName");
		action.setIndexName("indexName");
		action.setResult(Boolean.TRUE);
		action.setStartTime(new Date());
		action.setTimestamp(new Timestamp(System.currentTimeMillis()));
		return action;
	}

	@SuppressWarnings("rawtypes")
	private Server getServer(final String address) {
		Server server = new Server();
		server.setAddress(address);

		List<IndexContext> indexContexts = new ArrayList<IndexContext>();
		indexContexts.add(getIndexContext());
		indexContexts.add(getIndexContext());
		indexContexts.add(getIndexContext());

		server.setIndexContexts(indexContexts);

		return server;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IndexContext getIndexContext() {
		IndexContext indexContext = new IndexContext();
		indexContext.setIndexName("indexName");
		List<Snapshot> snapshots = new ArrayList<Snapshot>();

		snapshots.add(getSnapshot(1, 100, 60000));
		snapshots.add(getSnapshot(2, 200, 120000));
		snapshots.add(getSnapshot(3, 300, 180000));
		indexContext.setSnapshots(snapshots);
		return indexContext;
	}

	private Snapshot getSnapshot(final long docsPerMinute, final long searchesPerMinute, final long time) {
		Snapshot snapshot = new Snapshot();
		snapshot.setTimestamp(new Timestamp(time));
		snapshot.setDocsPerMinute(docsPerMinute);
		snapshot.setSearchesPerMinute(searchesPerMinute);
		return snapshot;
	}

}