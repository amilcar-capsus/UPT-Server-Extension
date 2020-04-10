package org.oskari.example.st;

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
import org.oskari.example.PostStatus;

@OskariActionRoute("st_operation_options")
public class STOperationOptionsHandler extends RestActionHandler {

    private static String stURL;
    private static String stUser;
    private static String stPassword;
    private static final Logger log = LogFactory.getLogger(STSettingsHandler.class);

    private JSONArray errors;
    private ObjectMapper Obj;
    
    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        stURL = PropertyUtil.get("db.url");
        stUser = PropertyUtil.get("db.username");
        stPassword = PropertyUtil.get("db.password");
        
        errors = new JSONArray();
        Obj = new ObjectMapper();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        String errorMsg = "Operation method get";
        Long user_id = params.getUser().getId();

        ArrayList<STOperationMethod> modules = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, name\n" +
                    "FROM public.st_operation");
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            
            ResultSet data = statement.executeQuery();
            while (data.next()) {
                STOperationMethod layer = new STOperationMethod();
                layer.id = data.getInt("id");
                layer.label = data.getString("name");
                modules.add(layer);
            };

            JSONArray out = new JSONArray();
            for (STOperationMethod index : modules) {
                //Convert to Json Object
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                out.put(json);
            }
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Operation getter executed"))));
            ResponseHelper.writeResponse(params, out);
        } catch (SQLException e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STOperationOptionsHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STOperationOptionsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
                
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(STOperationMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }
}
