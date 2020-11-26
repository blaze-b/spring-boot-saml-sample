package com.maclebtec.security.saml.spring.mvc;

import com.maclebtec.security.saml.spring.security.SAMLUserDetails;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private MetadataManager metadataManager;

    @GetMapping(value = "saml/idpSelection")
    public Map<String, String> findConfiguredIdentityProviders(HttpServletRequest request) {
        if (comesFromDiscoveryFilter(request)) {
            return metadataManager.getIDPEntityNames().stream()
                    .collect(toMap(identity(), this::getAlias));
        } else throw new AuthenticationServiceException("SP Discovery flow not detected");
    }

    @GetMapping(value = "saml/authenticate")
    public Map<String,Object> authenticate(@SAMLUser SAMLUserDetails user){
        Map<String,Object> userDetails = new HashMap<>();
        userDetails.put("userId", user.getUsername());
        userDetails.put("userAttributes", user.getAttributes());
        return userDetails;
    }

    private boolean comesFromDiscoveryFilter(HttpServletRequest request) {
        return request.getAttribute(SAMLConstants.LOCAL_ENTITY_ID) != null &&
                request.getAttribute(SAMLDiscovery.RETURN_URL) != null &&
                request.getAttribute(SAMLDiscovery.RETURN_PARAM) != null;
    }

    @SneakyThrows
    private String getAlias(String entityId) {
        return metadataManager.getExtendedMetadata(entityId).getAlias();
    }

}
