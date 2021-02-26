package org.oskari.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.GetWFSFeaturesHandlerTest;
import org.oskari.example.PostStatus;
import org.oskari.example.UPTRoles;
import org.oskari.example.st.LayersSTHandler;
import org.oskari.example.st.STLayersHandler;

@OskariActionRoute("UPTImportPublicLayerData")
public class UPTImportPublicLayerData extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;
  private static String user_uuid;
  private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);

  private JSONArray errors;
  private ObjectMapper Obj;
  private static GetWFSFeaturesHandlerTest testFeatures = new GetWFSFeaturesHandlerTest();

  @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    stURL = PropertyUtil.get("db.url");
    stUser = PropertyUtil.get("db.username");
    stPassword = PropertyUtil.get("db.password");

    user_uuid = params.getUser().getUuid();
    errors = new JSONArray();
    Obj = new ObjectMapper();
    testFeatures.init();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
    /* try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "with study_area as(\n" +
        "    select geometry from user_layer_data where user_layer_id=?\n" +
        "), user_layers as(\n" +
        "    select distinct st_layers.id as id, st_layers.st_layer_label, st_layer_label as label ,st_layers.user_layer_id,layer_field,layer_mmu_code,is_public\n" +
        "    from st_layers\n" +
        "    inner join user_layer_data on user_layer_data.user_layer_id = st_layers.user_layer_id\n" +
        "    inner join user_layer on user_layer.id = user_layer_data.user_layer_id\n" +
        "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
        "    , study_area\n" +
        "    where \n" +
        "    (user_layer.uuid=? or upt_user_layer_scope.is_public=1) and\n" +
        "    st_intersects(study_area.geometry,user_layer_data.geometry)\n" +
        ")\n" +
        "select id, st_layer_label, label,user_layer_id,layer_field,layer_mmu_code,is_public from user_layers"
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setLong(1, study_area);
      statement.setString(2, user_uuid);

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + statement.toString())
          )
        )
      );
      //statement.setLong(2, user_id);
      boolean status = statement.execute();
      if (status) {
        ResultSet data = statement.getResultSet();
        while (data.next()) {
          STLayers layer = new STLayers();
          layer.id = data.getLong("id");
          layer.label = data.getString("label");
          layer.st_layer_label = data.getString("st_layer_label");
          layer.user_layer_id = data.getLong("user_layer_id");
          layer.layer_field = data.getString("layer_field");
          layer.layer_mmu_code = data.getString("layer_mmu_code");
          modules.add(layer);
        }
      } else {
        STLayers layer = new STLayers();
        layer.id = -1L;
        layer.label = statement.toString();
        modules.add(layer);
      }

      JSONArray out = new JSONArray();
      for (STLayers index : modules) {
        //Convert to Json Object
        ObjectMapper Obj = new ObjectMapper();
        final JSONObject json = JSONHelper.createJSONObject(
          Obj.writeValueAsString(index)
        );
        out.put(json);
      }

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Layers executed"))
        )
      );

      ResponseHelper.writeResponse(params, out);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    } */
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area, user_uuid);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    study_area = Long.parseLong("6");
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }
}
