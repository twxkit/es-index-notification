package org.elasticsearch.contrib.plugin.helper;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.IOException;

public class AllHandlers {

    public static Handler resourceHandler(String resourceDirectory) {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(resourcePathFor(resourceDirectory));
        return resourceHandler;
    }

    private static String resourcePathFor(String resourceDirectory) {
        return AllHandlers.class.getResource(resourceDirectory).getPath();
    }

    public static Handler basicAuthHandler() {

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user", "admin", "moderator"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        SecurityHandler sh = new SecurityHandler();

        try {
            sh.setUserRealm(new HashUserRealm("MyRealm", resourcePathFor("/realm.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sh.setConstraintMappings(new ConstraintMapping[]{cm});

        WebAppContext webappcontext = new WebAppContext();
        webappcontext.setContextPath("/");
        webappcontext.setWar("/");
        webappcontext.addHandler(sh);

        return webappcontext;
    }

    public static RequestCaptureHandler requestCaptureHandler() {
        return new RequestCaptureHandler();
    }
}


