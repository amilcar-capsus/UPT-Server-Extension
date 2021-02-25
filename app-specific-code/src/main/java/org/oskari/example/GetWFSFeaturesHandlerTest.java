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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.*;
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
  public void testGetFeatures(Long studyArea) throws Exception {
    OskariLayer ml = LAYER_SERVICE.find(studyArea.intValue());
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

    /* String layerUrl = ml.getUrl();
    String layerVersion = ml.getVersion();
    String layerTypename = ml.getName();

    String id = "10";
    OskariLayer layer = new OskariLayer();
    layer.setId(Integer.parseInt(id));
    layer.setType(OskariLayer.TYPE_WFS);
    layer.setUrl(layerUrl);
    layer.setName(layerTypename); */

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      studyArea,
      ml,
      bbox,
      webMercator,
      Optional.empty()
    );
    SimpleFeatureIterator iterator = sfc.features();
    try {
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        JSONArray names = new JSONArray();
        JSONArray attributes = new JSONArray(feature.getAttributes());
        JSONObject fullFeature = new JSONObject();
        List<AttributeDescriptor> list = feature
          .getType()
          .getAttributeDescriptors();
        Iterator<AttributeDescriptor> attrIterator = list.iterator();
        try {
          while (attrIterator.hasNext()) {
            AttributeDescriptor attr = attrIterator.next();
            names.put(attr.getLocalName());
          }
        } finally {
          System.out.println("ARRAY: " + names);
        }
        //attributes.put(attributes);
        System.out.println("ID: " + feature.getID());
        System.out.println("Names: " + names);
        System.out.println("Attributes: " + attributes);
        for (int i = 0; i < names.length(); i++) {
          System.out.println("Names: " + names.get(i));
          System.out.println("Attributes: " + attributes.get(i));
          fullFeature.put(
            names.get(i).toString(),
            attributes.get(i).toString()
          );
        }
        System.out.println("Full Feature: " + fullFeature);
      }
    } finally {
      iterator.close();
    }
    CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
  }
}
