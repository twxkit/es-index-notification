package org.elasticsearch.contrib.plugin.helper;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestCaptureHandler extends DefaultHandler {

    private String requestBody;
    private String contentType;

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        contentType = request.getContentType();
        requestBody = IOUtils.toString(request.getInputStream());
        Request base_request = request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        if (!(response.isCommitted() || base_request.isHandled())) {
            base_request.setHandled(true);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    public String requestBody() {
        return requestBody;
    }

    public String contentType() {
        return contentType;
    }
}
