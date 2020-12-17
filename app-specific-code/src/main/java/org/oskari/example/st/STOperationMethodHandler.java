package org.oskari.example.st;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;

import org.oskari.annotation.OskariActionRoute;
import org.oskari.control.ActionException;
import org.oskari.control.ActionParameters;
import org.oskari.control.RestActionHandler;
import org.oskari.log.LogFactory;
import org.oskari.log.Logger;
import org.oskari.util.JSONHelper;
import org.oskari.util.PropertyUtil;
import org.oskari.util.ResponseHelper;
import org.oskari.example.UPTRoles;

@OskariActionRoute("st_operation")
public class STOperationMethodHandler extends RestActionHandler {

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
        
        String errorMsg = "Operation method get";
        Long user_id = params.getUser().getId();

        ArrayList<STOperationMethod> modules = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("UPTAdmin") && !roles.contains("UPTUser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, value, language, label, created, updated\n"
                    + "FROM public.st_join_options");

            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));

            ResultSet data = statement.executeQuery();
            while (data.next()) {
                STOperationMethod layer = new STOperationMethod();
                layer.id = data.getInt("id");
                //layer.method = data.getString("join_option");
                layer.value = data.getInt("value");
                layer.language = data.getString("language");
                layer.label = data.getString("label");
                modules.add(layer);
            };

            JSONArray out = new JSONArray();
            for (STOperationMethod index : modules) {
                //Convert to Json Object
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                out.put(json);
            }
            ResponseHelper.writeResponse(params, out);
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        
        String errorMsg = "Operation method get";
        Long user_id = params.getUser().getId();

        Integer value = Integer.parseInt(params.getRequiredParam("value"));
        String language = params.getRequiredParam("language");
        String label = params.getRequiredParam("label");
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("UPTAdmin") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO public.st_join_options(\n"
                    + "value, language, label)\n"
                    + "VALUES (?, ?, ?, ?);");
            
            statement.setLong(1, value);
            statement.setString(2, language);
            statement.setString(3, label);
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            
            ResultSet data = statement.executeQuery();
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Operation method registered"))));
            ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));

        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        
        String errorMsg = "Operation method put";
        Long user_id = params.getUser().getId();

        Integer id = Integer.parseInt(params.getRequiredParam("id"));
        Integer value = Integer.parseInt(params.getRequiredParam("value"));
        String language = params.getRequiredParam("language");
        String label = params.getRequiredParam("label");
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("UPTAdmin") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE public.st_join_options\n"
                    + "SET value=?, language=?, label=?\n"
                    + "WHERE id=?;");

            statement.setLong(2, value);
            statement.setString(3, language);
            statement.setString(4, label);
            statement.setInt(5, id);
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            
            statement.execute();

            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Operation method registered"))));
            ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        
        String errorMsg = "Operation method put";
        Long user_id = params.getUser().getId();

        Integer id = Integer.parseInt(params.getRequiredParam("id"));
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("UPTAdmin") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM public.st_join_options\n"
                    + "WHERE id=?;");
            statement.setInt(1, id);
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            
            statement.execute();

        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));                
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STOperationMethodHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }
}
