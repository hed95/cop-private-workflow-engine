package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.text.ParseException;
import java.util.Date;

import static java.util.Optional.ofNullable;

@Slf4j
@AllArgsConstructor
public class TaskFilterCriteriaMethodArgumentResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(TaskCriteria.class);
    }

    @Override
    public TaskCriteria resolveArgument(MethodParameter parameter,
                                        ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest,
                                        WebDataBinderFactory binderFactory) throws Exception {

        TaskCriteria taskCriteria = new TaskCriteria();
        ofNullable(webRequest.getParameter("name")).ifPresent(taskCriteria::setName);
        ofNullable(webRequest.getParameter("assignee")).ifPresent(taskCriteria::setAssignee);

        ofNullable(webRequest.getParameter("dueBefore")).map(this::parseDate).ifPresent(taskCriteria::setDueBefore);
        ofNullable(webRequest.getParameter("dueAfter")).map(this::parseDate).ifPresent(taskCriteria::setDueAfter);

        ofNullable(webRequest.getParameter("createdAfter")).map(this::parseDate).ifPresent(taskCriteria::setCreatedAfter);
        ofNullable(webRequest.getParameter("createdBefore")).map(this::parseDate).ifPresent(taskCriteria::setCreatedBefore);

        taskCriteria.setAssignedToMeOnly(candidateResponsibility(webRequest, "assignedToMeOnly"));
        taskCriteria.setUnassignedOnly(candidateResponsibility(webRequest, "unassignedOnly"));
        taskCriteria.setTeamOnly(candidateResponsibility(webRequest, "teamOnly"));

        return taskCriteria;
    }

    private Date parseDate(String dateToParse) {
        try {
            return DateUtils.parseDate(dateToParse, new String[]{
                    JacksonConfigurator.DEFAULT_DATE_FORMAT,
                    DateFormatUtils.ISO_DATE_FORMAT.getPattern()
            });
        } catch (ParseException e) {
            log.error("Failed to parse date", e);
            return null;
        }
    }

    private Boolean candidateResponsibility(NativeWebRequest webRequest, String candidateResponsibility) {
        return ofNullable(webRequest.getParameter(candidateResponsibility))
                .map(Boolean::valueOf).orElse(Boolean.FALSE);
    }
}
