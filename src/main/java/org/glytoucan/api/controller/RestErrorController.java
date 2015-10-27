package org.glytoucan.api.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glytoucan.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class RestErrorController implements ErrorController {

	private static final Log logger = LogFactory.getLog(RestErrorController.class);
	
    private static final String PATH = "/error";

    @Autowired
	private final ErrorAttributes errorAttributes = null;
    
    @RequestMapping(value = PATH)
    public @ResponseBody Message error(HttpServletRequest request) {
    	
    	Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
		HttpStatus status = getStatus(request);
		
    	logger.debug("errors:>" + body.toString());
    	return null;
//		Message msg = new Message();
//		msg.setError("");
//		msg.setMessage("errors:>" + body.toString());
//		msg.setPath(request.getRequestURI());
//		msg.setStatus(String.valueOf(status.value()));
//		msg.setTimestamp(new Date());
//		return msg;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
    
    private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request
				.getAttribute("javax.servlet.error.status_code");
		if (statusCode != null) {
			try {
				return HttpStatus.valueOf(statusCode);
			}
			catch (Exception ex) {
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
        
    private Map<String, Object> getErrorAttributes(HttpServletRequest request,
			boolean includeStackTrace) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		return this.errorAttributes.getErrorAttributes(requestAttributes,
				includeStackTrace);
	}
    
    private boolean getTraceParameter(HttpServletRequest request) {
		String parameter = request.getParameter("trace");
		if (parameter == null) {
			return false;
		}
		return !"false".equals(parameter.toLowerCase());
	}
}