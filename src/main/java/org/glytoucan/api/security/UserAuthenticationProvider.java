package org.glytoucan.api.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserAuthenticationProvider implements AuthenticationProvider{

	private static final Log logger = LogFactory
			.getLog(UserAuthenticationProvider.class);
	
//	@Autowired
//	UserProcedure userProcedure;

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
		if (StringUtils.isNotBlank(username) && username.equals("254")) {
			if (StringUtils.isNotBlank(password) && password.equals("JDUkMjAxNTEwMjIwMTIyNDckMmdWSDVyaERoZkg1OG9odTdjTENCR01wd1pxVmNUZ1hLN3VwY0ZkT0NIRA==")) {
				grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
			}
			Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
			return auth;
		}
		throw new BadCredentialsException("failed credentials");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
