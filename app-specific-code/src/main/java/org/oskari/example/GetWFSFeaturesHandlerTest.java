package org.oskari.example;

import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.control.feature.AbstractWFSFeatureHandler;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.PropertyUtil;
import java.util.Optional;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.util.ServiceFactory;

public class GetWFSFeaturesHandlerTest extends AbstractWFSFeatureHandler {
  private GetWFSFeaturesHandler handler;
  private static String stProjection;
  private static OskariLayerService LAYER_SERVICE = ServiceFactory.getMapLayerService();

  @Before
  public void init() {
    handler = new GetWFSFeaturesHandler();
    handler.init();
    stProjection =
      PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
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

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      "6",
      ml,
      bbox,
      webMercator,
      Optional.empty()
    );
    System.out.println("FEATURES!!!!!!!!!!!!!!!! " + handler.toString());

    CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
  }
}
