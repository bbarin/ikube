package ikube.action;

import ikube.IntegrationTest;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.Url;
import ikube.toolkit.OBJECT;
import mockit.Deencapsulation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class ResetIntegration extends IntegrationTest {

    private Reset reset;
    @Autowired
    private IDataBase dataBase;
    @Autowired
    protected IMonitorService monitorService;

    @Before
    public void before() {
        reset = new Reset();
    }

    @After
    public void after() {
        delete(dataBase, Url.class);
    }

    @Test
    public void execute() throws Exception {
        delete(dataBase, Url.class);

        Deencapsulation.setField(reset, "dataBase", dataBase);

        List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
        assertEquals("There should be no urls in the database : ", 0, urls.size());

        Url url = OBJECT.populateFields(new Url(), Boolean.TRUE, 3, "id");
        url.setName("indexContext");

        dataBase.persist(url);

        urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
        assertEquals("There should be one url in the database : ", 1, urls.size());

        boolean result = reset.execute(monitorService.getIndexContext("indexContext"));
        assertTrue(result);

        urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
        assertEquals("There should be no urls in the database : ", 0, urls.size());
    }

}
