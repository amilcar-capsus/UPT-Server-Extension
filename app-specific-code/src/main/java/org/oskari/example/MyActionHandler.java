package org.oskari.example;

import org.oskari.annotation.OskariActionRoute;
import org.oskari.control.*;
import org.oskari.control.ActionException;
import org.oskari.control.ActionParameters;
import org.oskari.control.ActionParamsException;
import org.oskari.control.RestActionHandler;
import org.oskari.log.LogFactory;
import org.oskari.log.Logger;
import org.oskari.util.ResponseHelper;

/**
 * Dummy Rest action route
 */
@OskariActionRoute("MyAction")
public class MyActionHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(MyActionHandler.class);

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        LOG.info(params.getUser(), "accessing route", getName());
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        ResponseHelper.writeResponse(params, "Hello " + params.getUser().getFullName());
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
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


}
