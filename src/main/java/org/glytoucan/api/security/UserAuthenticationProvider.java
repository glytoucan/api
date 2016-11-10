package org.glytoucan.api.security;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.SparqlException;
import org.glytoucan.admin.client.UserClient;
import org.glytoucan.admin.client.config.UserClientConfig;
import org.glytoucan.admin.model.User;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserKeyCheckRequest;
import org.glytoucan.admin.model.UserKeyCheckResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserAuthenticationProvider implements AuthenticationProvider {

  private static final Log logger = LogFactory.getLog(UserAuthenticationProvider.class);

  @Autowired
  UserClient userClient;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    logger.debug("username:>" + username + "<");
    logger.debug("password:>" + password + "<");

    List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
    // check user id and hash
    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
      
      org.glytoucan.admin.model.Authentication authModel = new org.glytoucan.admin.model.Authentication();
      authModel.setApiKey(adminKey);
      authModel.setId(adminEmail);

      UserKeyCheckRequest request = new UserKeyCheckRequest();
      request.setAuthentication(authModel);
      request.setContributorId(username);
      request.setApiKey(password);
      UserKeyCheckResponse response = userClient.userKeyCheckRequest(request);
      if (response.isResult()) {
        if (StringUtils.equals(username, adminEmail)) {
          grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
        return auth;
      }
    }
    throw new BadCredentialsException("failed credentials with id:>" + username + "<\nhash:>" + password);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

  @Value("${admin.email:815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d}")
  private String adminEmail;

  @Value("${admin.key}")
  private String adminKey;

  @Value("${google.oauth2.clientId}")
  private String clientId;

  @Value("${google.oauth2.clientSecret}")
  private String clientSecret;
}