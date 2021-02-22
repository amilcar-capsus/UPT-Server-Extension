package org.oskari.example;

import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.Envelope;
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
  private UPTGetWFSFeaturesHandler handler;

  @Before
  public void init() {
    handler = new UPTGetWFSFeaturesHandler();
    handler.init();
  }

  @Test
  @Ignore("Depends on an outside resource")
  public void testGetFeatures() throws Exception {
    String id = Integer.parseInt(params.getRequiredParam("layer_id"));
    OskariLayer layer = LAYER_SERVICE.find(id);
    layer.setId(Integer.parseInt(params.getRequiredParam("layer_id")));
    layer.setType(OskariLayer.TYPE_WFS);
    layer.setUrl(layer.getUrl());
    layer.setName(layer.getName());
    CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
    PropertyUtil.addProperty("oskari.native.srs", "EPSG:3067", true);
    System.out.println(layer.getCapabilities.toString());
    Envelope envelope = new Envelope(2775356, 2875356, 8441866, 8541866);
    ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      id,
      layer,
      bbox,
      webMercator,
      Optional.empty()
    );

    CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
  }
}
