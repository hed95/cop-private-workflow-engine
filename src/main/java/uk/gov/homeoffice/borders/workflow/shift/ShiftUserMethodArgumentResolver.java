package uk.gov.homeoffice.borders.workflow.shift;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.homeoffice.borders.workflow.exception.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;

@Slf4j
@AllArgsConstructor
public class ShiftUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private IdentityService identityService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ShiftUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        WorkflowAuthentication currentAuthentication = (WorkflowAuthentication) identityService.getCurrentAuthentication();
        if (currentAuthentication == null) {
            throw new ForbiddenException("No current authentication detected.");
        }
        if (currentAuthentication.getUser() == null) {
            throw new ForbiddenException("No active shift detected for user.");
        }
        return currentAuthentication.getUser();
    }
}
