package ikube.index.spatial.enrich;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.model.Indexable;
import ikube.toolkit.Logging;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;

/**
 * This class will add spatial fields to the index based on either the latitude and longitude defined in one of the columns that are
 * indexables or it will go to the spatial web service with the address in the table and search for the location, i.e. the latitude and
 * longitude for the address and use the first result that comes back.
 * 
 * @author Michael Couck
 * @since 12.04.11
 * @version 01.00
 */
public class Enrichment implements IEnrichment {

	private static final Logger LOGGER = Logger.getLogger(Enrichment.class);

	private transient int endTier;
	private transient int startTier;
	private transient IProjector projector;
	private transient CartesianTierPlotter cartesianTierPlotter;

	public Enrichment() {
		projector = new SinusoidalProjector();
		cartesianTierPlotter = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
	}

	@Override
	public int getMinKm(double minKm) {
		return cartesianTierPlotter.bestFit(minKm);
	}

	@Override
	public int getMaxKm(double maxKm) {
		return cartesianTierPlotter.bestFit(maxKm);
	}

	@Override
	public void addSpatialLocationFields(Coordinate coordinate, Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document, startTier, endTier);
	}

	@Override
	public void addCartesianTiers(Coordinate coordinate, Document document, int startTier, int endTier) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(tier, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = cartesianTierPlotter.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Logging.getString("Tier : ", tier, ", box id : ", boxId, ", cartesian tier : ",
						ToStringBuilder.reflectionToString(cartesianTierPlotter, ToStringStyle.SHORT_PREFIX_STYLE)));
			}
			document.add(new Field(cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	@Override
	public Coordinate getCoordinate(Indexable<?> indexable) {
		double latitude = Double.MAX_VALUE;
		double longitude = Double.MAX_VALUE;
		for (Indexable<?> child : indexable.getChildren()) {
			try {
				if (child.getContent() == null) {
					continue;
				}
				if (child.getName().equals(IConstants.LATITUDE)) {
					latitude = Double.parseDouble(child.getContent().toString());
				} else if (child.getName().equals(IConstants.LONGITUDE)) {
					longitude = Double.parseDouble(child.getContent().toString());
				}
			} catch (Exception e) {
				LOGGER.error("Exception getting the lat/long co-ordinates : " + indexable, e);
			}
		}
		if (latitude != Double.MAX_VALUE && longitude != Double.MAX_VALUE) {
			return new Coordinate(latitude, longitude);
		}
		return null;
	}

	@Override
	public StringBuilder buildAddress(Indexable<?> indexable, StringBuilder builder) {
		if (indexable.isAddress()) {
			if (builder.length() > 0) {
				builder.append(" ");
			}
			builder.append(indexable.getContent());
		}
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				buildAddress(child, builder);
			}
		}
		return builder;
	}

	public void setMinKm(final double minKm) {
		this.startTier = getMinKm(minKm);
	}

	public void setMaxKm(final double maxKm) {
		this.endTier = getMaxKm(maxKm);
	}

}