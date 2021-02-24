package org.oskari.example;

import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.control.feature.AbstractWFSFeaturesHandler;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.WFSDescribeFeatureHelper;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.util.ServiceFactory;

public class GetWFSFeaturesHandlerTest {
  private UPTGetWFSFeaturesHandler handler;
  private static String stProjection;
  private static OskariLayerService LAYER_SERVICE = ServiceFactory.getMapLayerService();

  @Before
  public void init() {
    handler = new UPTGetWFSFeaturesHandler();
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
    // PropertyUtil.addProperty("oskari.native.srs", "EPSG:" + stProjection, true);
    PropertyUtil.addProperty("oskari.native.srs", "EPSG:3857", true);
    Envelope envelope = new Envelope(
      -20016250.811,
      19934883.938,
      20097617.684,
      -19772150.192
    );
    ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);

    String layerUrl = ml.getUrl();
    String layerVersion = ml.getVersion();
    String layerTypename = ml.getName();

    String id = "10";
    OskariLayer layer = new OskariLayer();
    layer.setId(Integer.parseInt(id));
    layer.setType(OskariLayer.TYPE_WFS);
    layer.setUrl(layerUrl);
    layer.setName(layerTypename);
    /* String parsedLayerUrl = WFSDescribeFeatureHelper.parseDescribeFeatureUrl(
      layerUrl,
      layerVersion,
      layerTypename
    );

    System.out.println("LAYER URL!!!!!!! " + parsedLayerUrl); */

    System.out.println("LAYER URL!!!!!!! " + layer.getUrl());

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      id,
      layer,
      bbox,
      webMercator,
      Optional.empty()
    );
    SimpleFeatureIterator iterator = sfc.features();
    try {
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        Iterator<AttributeDescriptor> list = feature
          .getType()
          .getAttributeDescriptors();
        while (list.hasNext()) {
          System.out.println("" + list.getLocalName());
        }
        System.out.println("ID: " + feature.getID());
        System.out.println("Attributes: " + feature.getAttributes());
        //System.out.println("Geom: " + feature.getDefaultGeometry());
        System.out.println("FeatureType: " + feature.getFeatureType());
        System.out.println(
          "Attribute descriptors: " +
          feature.getType().getAttributeDescriptors()
        );
        System.out.println("TypeName: " + feature.getType().getTypeName());
      }
    } finally {
      iterator.close();
    }
    //System.out.println("FEATURES!!!!!!!!!!!!!!!! " + sfc.toString());

    /* CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS)); */
  }

  public void testGetExternalFeatures() throws Exception {
    String id = "10";
    OskariLayer layer = new OskariLayer();
    layer.setId(Integer.parseInt(id));
    layer.setType(OskariLayer.TYPE_WFS);
    layer.setUrl("https://geo.stat.fi/geoserver/tilastointialueet/wfs");
    layer.setName("tilastointialueet:kunta1000k");
    CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
    PropertyUtil.addProperty("oskari.native.srs", "EPSG:3067", true);
    Envelope envelope = new Envelope(2775356, 2875356, 8441866, 8541866);
    ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      id,
      layer,
      bbox,
      webMercator,
      Optional.empty()
    );

    SimpleFeatureIterator iterator = sfc.features();
    try {
      System.out.println("Entering try section!!!!!!!!!");
      while (iterator.hasNext()) {
        System.out.println("While section!!!!!!!!!");
        SimpleFeature feature = iterator.next();
        System.out.println("ID: " + feature.getID());
      }
    } finally {
      System.out.println("Finally section!!!!!!!!!");
      iterator.close();
    }

    CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
  }
}
