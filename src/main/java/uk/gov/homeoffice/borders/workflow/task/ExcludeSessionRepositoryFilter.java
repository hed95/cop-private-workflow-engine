package uk.gov.homeoffice.borders.workflow.task;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExcludeSessionRepositoryFilter extends OncePerRequestFilter {

    private final AntPathRequestMatcher restExternalTaskEndpoint = new AntPathRequestMatcher("/rest/camunda/external-task/**");

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (restExternalTaskEndpoint.matches(httpRequest)) {
            httpRequest.setAttribute("org.springframework.session.web.http.SessionRepositoryFilter.FILTERED", Boolean.TRUE);
        }
        filterChain.doFilter(httpRequest, httpResponse);
    }
}