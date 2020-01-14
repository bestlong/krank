package org.crank.tags;

import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.FaceletContext;
import javax.faces.component.UIComponent;
import javax.faces.FacesException;
import javax.el.ELException;
import java.io.IOException;
import java.util.*;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kregester
 * Date: Oct 24, 2008
 * Time: 11:26:59 AM
 * Package: org.crank.tags
 * Copyright Vantage Media 2008
 */

/**
 * <security:authorize ifAllGranted|ifAnyGranted|ifNotGranted></secruity:authorize>
 */
public final class SpringSecurityAuthorize extends TagHandler {

//    private final Logger log = LogManager.getLogger(SpringSecurityAuthorize.class);
    private final TagAttribute ifAllGranted;
    private final TagAttribute ifAnyGranted;
    private final TagAttribute ifNotGranted;

    public SpringSecurityAuthorize(final TagConfig config) {
        super(config);
        this.ifAnyGranted = this.getAttribute("ifAnyGranted");
        this.ifAllGranted = this.getAttribute("ifAllGranted");
        this.ifNotGranted = this.getAttribute("ifNotGranted");
    }

    public void apply(FaceletContext faceletContext, UIComponent uiComponent) throws IOException, FacesException, ELException {
        /* Get the name of the value binding. */
        String ifAllGrantedValue = (this.ifAllGranted != null) ? this.ifAllGranted.getValue() : null;
        String ifNotGrantedValue = (this.ifNotGranted != null) ? this.ifNotGranted.getValue() : null;
        String ifAnyGrantedValue = (this.ifAnyGranted != null) ? this.ifAnyGranted.getValue() : null;

        if (((null == ifAllGranted) || "".equals(ifAllGranted)) && ((null == ifAnyGranted) || "".equals(ifAnyGranted))
                && ((null == ifNotGranted) || "".equals(ifNotGranted))) {
            return;
        }

        final Collection granted = SpringSecurityUtils.getPrincipalAuthorities();

        //test if user does not have certain roles assigned
        if ((null != ifNotGrantedValue) && !"".equals(ifNotGrantedValue)) {
            Set parsedAuthoritiesString = SpringSecurityUtils.parseAuthoritiesString(ifNotGrantedValue);
            Set grantedCopy = SpringSecurityUtils.removeAll(granted, parsedAuthoritiesString);
            if (!grantedCopy.isEmpty() && !granted.containsAll(SpringSecurityUtils.parseAuthoritiesString(ifNotGrantedValue))) {
                this.nextHandler.apply(faceletContext, uiComponent);
                return;
            }
        }

        //test if user has all roles assigned
        if ((null != ifAllGrantedValue) && !"".equals(ifAllGrantedValue)) {
            Set parsedAuthoritiesString = SpringSecurityUtils.parseAuthoritiesString(ifAllGrantedValue);
            if (granted.containsAll(parsedAuthoritiesString)) {
                this.nextHandler.apply(faceletContext, uiComponent);
                return;
            }
        }

        //test if user has any of the roles assigned
        if ((null != ifAnyGrantedValue) && !"".equals(ifAnyGrantedValue)) {
            Set parsedAuthoritiesString = SpringSecurityUtils.parseAuthoritiesString(ifAnyGrantedValue);
            Set grantedCopy = SpringSecurityUtils.retainAll(granted, parsedAuthoritiesString);
            if (!grantedCopy.isEmpty()) {
                this.nextHandler.apply(faceletContext, uiComponent);
                return;
            }
        }
    }
}