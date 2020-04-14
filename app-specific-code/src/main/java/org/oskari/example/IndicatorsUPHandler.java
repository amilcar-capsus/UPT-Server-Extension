package org.oskari.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@CrossOrigin(origins = {"*"}, maxAge = 6000)
@OskariActionRoute("IndicatorsUPHandler")
public class IndicatorsUPHandler extends RestActionHandler {
    private static  String upURL;
    private static  String upUser;
    private static  String upPassword;

    private static  String upwsHost;
    private static  String upwsPort;
    private static final Logger log = LogFactory.getLogger(IndicatorsUPHandler.class);

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL=PropertyUtil.get("up.db.URL");
        upUser=PropertyUtil.get("up.db.user");
        upPassword=PropertyUtil.get("up.db.password");

        upwsHost=PropertyUtil.get("upws.db.host");
        upwsPort=PropertyUtil.get("upws.db.port");
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        String errorMsg = "Scenario UP get ";
        
        ResponseEntity<List<IndicatorUP>> returns = null;
        try {
            params.requireLoggedInUser();
            RestTemplate restTemplate = new RestTemplate();
            returns = restTemplate.exchange(
              "http://"+ upwsHost +":"+upwsPort+"/indicator/",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<IndicatorUP>>(){});
            
            Long user_id = params.getUser().getId();
            List<IndicatorUP> response = returns.getBody();
            
            ResultSet indicators=getIndicators(params);
            
            JSONArray out=new JSONArray();
                //Update label field
            while(indicators.next()){
                for (IndicatorUP index : response){
                    if(index!=null && indicators.getString("name")!=null && index.module.equals(indicators.getString("name"))) {
                        index.setLabel(indicators.getString("label"));
                        //Update dependencies field
                        String[] deps;
                        deps=index.dependencies.replace("[","").replace("]","").replaceAll("\"","").split(",");
                        for (String dependency : deps){
                            if(indicators.getString("name")!=null && dependency.equals(indicators.getString("name"))) {
                                index.dependencies=index.dependencies.replaceAll(dependency, indicators.getString("label"));
                            }
                        }
                        //Convert to Json Object
                        ObjectMapper Obj = new ObjectMapper();
                        JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                        out.put(json);
                        break;
                    }
                }
            }
            ResponseHelper.writeResponse(params,out);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
            ResponseHelper.writeResponse(params,returns);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {

        String errorMsg = "Scenario UP get ";
        try {
            params.requireLoggedInUser();
            Long user_id = params.getUser().getId();
            RestTemplate restTemplate = new RestTemplate();
            ResultSet indicators=getIndicators(params);
            ResponseHelper.writeResponse(params,indicators);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
        throw new ActionException("This will be logged including stack trace");
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionParamsException("Notify there was something wrong with the params");
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionDeniedException("Not deleting anything");
    }

    private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
    }

    protected ResultSet getIndicators(ActionParameters params) {
        ResultSet result=null;
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, name, label, tooltip\n" +
                    "	FROM public.up_modules_translation where language=?;"
            );
            statement.setString(1, "english");
            result = statement.executeQuery();
            
            return result;
        } catch (SQLException e) {
        }
        return result;
    }

}
