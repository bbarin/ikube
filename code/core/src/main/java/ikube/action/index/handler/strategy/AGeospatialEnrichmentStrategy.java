package ikube.action.index.handler.strategy;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.geospatial.GeoCity;
import ikube.toolkit.HASH;
import ikube.toolkit.THREAD;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the base class for strategies that add geospatial fields to the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-12-2013
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class AGeospatialEnrichmentStrategy extends AStrategy {

    static Map<Long, GeoCity> GEO_CITY = new HashMap<>();

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${max-geohash-levels}")
    protected int maxGeohashLevels = IConstants.MAX_GEOHASH_LEVELS;

    /**
     * The geocoder to get the co-ordinates for the indexable.
     */
    @Autowired
    protected IGeocoder geocoder;
    @Autowired
    private IDataBase dataBase;

    private SpatialContext spatialContext;
    private SpatialStrategy spatialStrategy;

    public AGeospatialEnrichmentStrategy() {
        this(null);
    }

    public AGeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    @SuppressWarnings("deprecation")
    public final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
        // Note to self: This takes an x and y co-ordinate so the
        // order must be longitude(x) and latitude(y), not the other way
        Shape shape = spatialContext.makePoint(coordinate.getLongitude(), coordinate.getLatitude());
        for (final IndexableField indexableField : spatialStrategy.createIndexableFields(shape)) {
            // logger.info("Adding spatial field : " + indexableField);
            document.add(indexableField);
        }
        // Store this field any way
        document.add(new StoredField(spatialStrategy.getFieldName(), spatialContext.toString(shape)));
    }

    @Override
    public void initialize() {
        spatialContext = SpatialContext.GEO;
        SpatialPrefixTree spatialPrefixTree = new GeohashPrefixTree(spatialContext, maxGeohashLevels);
        this.spatialStrategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, IConstants.POSITION_FIELD_NAME);

        if (!THREAD.isInitialized()) {
            THREAD.initialize();
        }
        final String name = "wait-for-data-load";
        THREAD.submit(name, new Runnable() {
            public void run() {
                try {
                    THREAD.sleep(10000);
                    Collection<GeoCity> geoCities = dataBase.find(GeoCity.class, 0, Integer.MAX_VALUE);
                    if (geoCities != null) {
                        for (final GeoCity geoCity : geoCities) {
                            Long hash = HASH.hash(geoCity.getName().toLowerCase());
                            GEO_CITY.put(hash, geoCity);
                        }
                        logger.info("Loaded country/city map : " + GEO_CITY.size());
                    }
                } finally {
                    THREAD.destroy(name);
                }
            }
        });
    }

}