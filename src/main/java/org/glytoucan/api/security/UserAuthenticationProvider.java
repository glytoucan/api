package org.glytoucan.api.security;

import static java.util.Arrays.asList;
import static org.springframework.security.oauth2.common.AuthenticationScheme.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.SparqlException;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.service.UserProcedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;

import com.github.fromi.openidconnect.security.UserInfo;

public class UserAuthenticationProvider implements AuthenticationProvider{

	private static final Log logger = LogFactory
			.getLog(UserAuthenticationProvider.class);
	
	@Autowired
	UserProcedure userProcedure;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		
		String username = authentication.getName();
        String password = authentication.getCredentials().toString();

		logger.debug("username:>" + username + "<");
		logger.debug("password:>" + password + "<");

//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        try {
//        	UserEntity user = userManager.getUserByLoginId (username, true, true); // only validated and active users can use the system
//        	if (user == null) {
//        		throw new BadCredentialsException("User " + username + " not found.");
//        	}
//        	
//        	if (passwordEncoder.matches(password, user.getPassword()) || password.equals("token")) {
//        		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
//        		//check the user's roles
//        		Set<RoleEntity> userRoles = user.getRoles();
//        		for (Iterator<RoleEntity> iterator = userRoles.iterator(); iterator
//						.hasNext();) {
//					RoleEntity roleEntity = (RoleEntity) iterator.next();
//					grantedAuths.add(new SimpleGrantedAuthority("ROLE_" + roleEntity.getRoleName()));
//				}
//                Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
//                // set user's last logged in date
//                userManager.setLoggedinDate(user, new Date());
//                return auth;
//        	}
//        	else {
//        		throw new BadCredentialsException("Wrong password!");
//        	}
//        } catch (UserNotFoundException une) {
//        	throw new BadCredentialsException("Username not found. The user might still be waiting for approval", une);
//        }
		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
//		if (StringUtils.isNotBlank(username) && username.equals("254")) {
//			if (StringUtils.isNotBlank(password) && password.equals("JDUkMjAxNTEwMjIwMTIyNDckMmdWSDVyaERoZkg1OG9odTdjTENCR01wd1pxVmNUZ1hLN3VwY0ZkT0NIRA==")) {
//				grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
//			}
//			Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
//			return auth;
//		}
		
		// check user id and hash
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			try {
				if (userProcedure.checkApiKey(username, password)) {
					if (StringUtils.equals(username, "1")) {
						grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
					}
					grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
					Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
					return auth;
				}
			} catch (UserException e) {
				throw new BadCredentialsException("system error when checking email", e);
			}
			
	    	DefaultOAuth2AccessToken defToken = new DefaultOAuth2AccessToken(password);
	    	DefaultOAuth2ClientContext defaultContext = new DefaultOAuth2ClientContext();
	    	defaultContext.setAccessToken(defToken);
	    	OAuth2RestOperations rest = new OAuth2RestTemplate(googleOAuth2Details(), defaultContext);
	        final ResponseEntity<UserInfo> userInfoResponseEntity = rest.getForEntity("https://www.googleapis.com/oauth2/v2/userinfo", UserInfo.class);
	        logger.debug("userInfo:>" + userInfoResponseEntity.toString());
	        UserInfo user = userInfoResponseEntity.getBody();
			if (StringUtils.equals(user.getEmail(), adminEmail)) {
				grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
				Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
				return auth;
			}
		}
		throw new BadCredentialsException("failed credentials with id:>" + username + "<\nhash:>" + password );
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	
    public OAuth2ProtectedResourceDetails googleOAuth2Details() {
        AuthorizationCodeResourceDetails googleOAuth2Details = new AuthorizationCodeResourceDetails();
        googleOAuth2Details.setAuthenticationScheme(form);
        googleOAuth2Details.setClientAuthenticationScheme(form);
        googleOAuth2Details.setClientId(clientId);
        googleOAuth2Details.setClientSecret(clientSecret);
        googleOAuth2Details.setUserAuthorizationUri("https://accounts.google.com/o/oauth2/auth");
        googleOAuth2Details.setAccessTokenUri("https://www.googleapis.com/oauth2/v3/token");
        googleOAuth2Details.setScope(asList("email"));
        return googleOAuth2Details;
    }
    
    @Value("${admin.email:glytoucan@gmail.com}")
    private String adminEmail;
    
	@Value("${google.oauth2.clientId}")
    private String clientId;

    @Value("${google.oauth2.clientSecret}")
    private String clientSecret;
}