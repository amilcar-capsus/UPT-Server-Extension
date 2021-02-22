package org.oskari.example;

import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import java.util.Optional;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GetWFSFeaturesHandlerTest {
  private GetWFSFeaturesHandler handler;

  @Before
  public void init() {
    handler = new GetWFSFeaturesHandler();
    handler.init();
  }

  @Test
  @Ignore("Depends on an outside resource")
  public void testGetFeatures() throws Exception {
    OskariLayer ml = LAYER_SERVICE.find(6);
    CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
    PropertyUtil.addProperty("oskari.native.srs", "EPSG:" + stProjection, true);
    Envelope envelope = new Envelope(
      -13149614.848125,
      4383204.949375,
      -12523442.7125,
      5009377.085
    );
    ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);

    SimpleFeatureCollection sfc = featuresList.featureClient.getFeatures(
      params.getRequiredParam("layer_id"),
      ml,
      bbox,
      webMercator,
      Optional.empty()
    );
    System.out.println("FEATURES!!!!!!!!!!!!!!!! " + featuresList.toString());

    CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
  }
}
