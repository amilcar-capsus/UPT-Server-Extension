package org.oskari.example.st;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.control.userlayer.UserLayerHandlerHelper;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;
import org.oskari.log.AuditLog;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.map.userlayer.service.UserLayerDbServiceMybatisImpl;
import org.oskari.map.userlayer.service.UserLayerException;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.AbstractLayerAdminHandler;
import fi.nls.oskari.domain.map.UserDataStyle;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;



@OskariActionRoute("STGeoJsonHandlerBack")
public class STGeoJsonHandlerBack extends AbstractLayerAdminHandler {

    private static final Logger LOG = LogFactory.getLogger(STLayerSave.class);
    
    private static final String PROPERTY_USERLAYER_MAX_FILE_SIZE_MB = "userlayer.max.filesize.mb";
    private static final String PROPERTY_TARGET_EPSG = "oskari.native.srs";

    private static final String KEY_NAME = "layer-name";
    private static final String KEY_DESC = "layer-desc";
    private static final String KEY_SOURCE = "layer-source";
    private static final String KEY_STYLE = "layer-style";
    
    private static final int KB = 1024 * 1024;
    private static final int MB = 1024 * KB;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB; 
    private static final String PARAM_SOURCE_EPSG_KEY = "sourceEpsg";
    private final String targetEPSG = PropertyUtil.get(PROPERTY_TARGET_EPSG, "EPSG:4326");


    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);
    private final int userlayerMaxFileSize = PropertyUtil.getOptional(PROPERTY_USERLAYER_MAX_FILE_SIZE_MB, 10) * MB;

    private UserLayerDbService userLayerService;
    
    private JSONArray errors;
    private ObjectMapper Obj;

    public void setUserLayerService(UserLayerDbService userLayerService) {
        this.userLayerService = userLayerService;
    }

    @Override
    public void init() {
        if (userLayerService == null) {
            userLayerService = new UserLayerDbServiceMybatisImpl();
        }
    }
    
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        ResponseHelper.writeResponse(params, "json servcie action.... should be a post with a json file... " + params.getUser().getFullName());
    }
    
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();


        String sourceEPSG = params.getHttpParam(PARAM_SOURCE_EPSG_KEY);
        List<FileItem> fileItems = getFileItems(params.getRequest());
        SimpleFeatureCollection fc;
        Map<String, String> formParams;
        Set<String> validFiles = new HashSet<>();
        FileItem jsonfile = null;
        
        for(int i=0; i<fileItems.size(); i++){
//            LOG.info(" fileItems size : "+fileItems.size());
            LOG.info("Form/file item "+i+":  "+ fileItems.get(i));
        }
  
        try {
            CoordinateReferenceSystem sourceCRS = decodeCRS(sourceEPSG);
            CoordinateReferenceSystem targetCRS = decodeCRS(targetEPSG);
            jsonfile = fileItems.stream()
                    .filter(f -> !f.isFormField())
                    .findAny() // Find the file or exit
                    .orElseThrow(() -> new ActionParamsException("No file entries in FormData "));
            LOG.info("Using namefile:", jsonfile.getName(), "as the input GEOJSON file."+" with size "+jsonfile.getSize());
                    
//            ResponseHelper.writeResponse(params,"received "+jsonfile.getName()+" with size "+jsonfile.getSize());
            
            ObjectMapper om = new ObjectMapper();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String,Object>>() {};
            Map<String, Object> geojson = null;

//            File file = new File("temp.geojson");
//            try{
//                jsonfile.write(file);
//            }catch(Exception ex){
//                LOG.error("Failed writing the fileitem");
//            }
            try (InputStream in = jsonfile.getInputStream()) {
                geojson = om.readValue(in, typeRef);
                if(geojson.containsKey("0")){
                    LOG.info("Received geojson seems to be a ST geojson array.... will try to grab geojson at position 0 or fail...");
                    geojson = (Map<String, Object>)geojson.get("0");
                    ArrayList tmpArr = (ArrayList) geojson.get("features");
                    if(tmpArr.size() > 0) {
                        Map<String, Object> tmp = (Map<String, Object>)tmpArr.get(0);   
                        tmp = (Map<String, Object>) tmp.get("properties");
                        
                        LOG.info(tmp.get("value") + " gives "+ (tmp.get("value") instanceof Double));
                        if(!(tmp.get("value") instanceof Double)){
                            if( tmp.get("value") instanceof Integer)
                                tmp.put("value", new Double((Integer)tmp.get("value")));
                        }
                    }                            
//                    LOG.info(geojson);
                }
            }catch(IOException ioex){
                LOG.error("Failed reading the input stream");            
            }
            
            try{
            CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
            
//            SimpleFeatureType modified = DataUtilities.createSubType(schema, new String[] {""});            
//            schema = DataUtilities.createSubType(schema, new String[]{"propertyMap"} );
                    
            LOG.info(schema);
            
            SimpleFeatureCollection original = GeoJSONReader2.toFeatureCollection(geojson, schema);

            formParams = getFormParams(fileItems);
            
            LOG.info("before postprocess size is reported as : "+original.size());

//            SimpleFeature f = null;
//            SimpleFeatureCollection retyped = new UserLayerWFSHelper().postProcess(original);
//
//            LOG.info("after postprocess size is reported as : "+retyped.size());
//            
                try (SimpleFeatureIterator it = original.features()) {
                    while (it.hasNext()) {
                        SimpleFeature feature = it.next();
                        LOG.info(feature.getID() +" "+feature.getAttribute("value")+" de "+feature.getAttributeCount());
////                        ResponseHelper.writeResponse(params,"going through: "+feature.toString());
                    }
                }
                
            LOG.info("Info for userlayer creation: "+original+"   ,   "+params.getUser().getUuid()+"    ,    "+formParams);
            UserLayer userLayer = store(original, params.getUser().getUuid(), formParams);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", jsonfile.getName())
                    .withParam("id", userLayer.getId())
                    .added(AuditLog.ResourceType.USERLAYER);

                writeResponse(params, userLayer);
                
            } catch(Exception ex){
                LOG.error("Error while storing json layer... ", ex);
            }
                        

        } catch (UserLayerException e) {
            if (!validFiles.isEmpty()){ // avoid to override with empty list
                e.addContent(UserLayerException.InfoType.FILES, validFiles);
            }
            LOG.error("User uuid:", params.getUser().getUuid(),
                    "zip:", jsonfile == null ? "no file" : jsonfile.getName(),
                    "info:", e.getOptions().toString());

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", jsonfile.getName())
                    .withMsg(e.getMessage())
                    .errored(AuditLog.ResourceType.USERLAYER);

            throw new ActionParamsException(e.getMessage(), e.getOptions());
        } catch (ActionException e) {
            LOG.error("User uuid:", params.getUser().getUuid(),
                    "zip:", jsonfile == null ? "no file" : jsonfile.getName(),
                    "files found ("+ validFiles.size() + ") including:",
                    validFiles.stream().collect(Collectors.joining(",")));
            throw e;
        } finally {
            fileItems.forEach(FileItem::delete);
        }
    }
    
 
    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            upload.setSizeMax(userlayerMaxFileSize);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
        }
    }
    
    
    private CoordinateReferenceSystem decodeCRS(String epsg) throws UserLayerException {
        try {
            return epsg == null ? null : CRS.decode(epsg);
        } catch (Exception e) {
            throw new UserLayerException("Failed to decode CoordinateReferenceSystem from " + epsg,
                    UserLayerException.ErrorType.INVALID_EPSG);
        }
    }
    
    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }
    
     private UserLayer store(SimpleFeatureCollection fc, String uuid, Map<String, String> formParams)
            throws UserLayerException {
            UserLayer userLayer = createUserLayer(fc, uuid, formParams);
            LOG.info("1...");
            userLayer.setStyle(createUserLayerStyle(formParams));
            LOG.info("2...");
            List<UserLayerData> userLayerDataList = UserLayerDataService.createUserLayerData(fc, uuid);
            LOG.info("3...");
            userLayer.setFeatures_count(userLayerDataList.size());
            LOG.info("4...");
            userLayer.setFeatures_skipped(fc.size() - userLayerDataList.size());
            LOG.info("5...");
            userLayerService.insertUserLayer(userLayer, userLayerDataList);
            LOG.info("6...");
            return userLayer;
    }

    private UserLayer createUserLayer(SimpleFeatureCollection fc, String uuid, Map<String, String> formParams) {
        String name = formParams.get(KEY_NAME);
        String desc = formParams.get(KEY_DESC);
        String source = formParams.get(KEY_SOURCE);
        return UserLayerDataService.createUserLayer(fc, uuid, name, desc, source);
    }

    private UserDataStyle createUserLayerStyle(Map<String, String> formParams)
            throws UserLayerException {
        JSONObject styleObject = null;
        if (formParams.containsKey(KEY_STYLE)) {
            styleObject = JSONHelper.createJSONObject(formParams.get(KEY_STYLE));
        }
        return UserLayerDataService.createUserLayerStyle(styleObject);
    }

    private void writeResponse(ActionParameters params, UserLayer ulayer) {
        String mapSrs = params.getHttpParam(ActionConstants.PARAM_SRS);
        JSONObject userLayer = UserLayerDataService.parseUserLayer2JSON(ulayer, mapSrs);

        JSONHelper.putValue(userLayer, "featuresCount", ulayer.getFeatures_count());
        JSONObject permissions = UserLayerHandlerHelper.getPermissions();
        JSONHelper.putValue(userLayer, "permissions", permissions);
        //add warning if features were skipped
        if (ulayer.getFeatures_skipped() > 0) {
            JSONObject featuresSkipped = new JSONObject();
            JSONHelper.putValue(featuresSkipped, "featuresSkipped", ulayer.getFeatures_skipped());
            JSONHelper.putValue(userLayer, "warning", featuresSkipped);
        }
        ResponseHelper.writeResponse(params, userLayer);
    }
    
    
}
