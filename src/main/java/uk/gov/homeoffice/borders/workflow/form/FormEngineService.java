package uk.gov.homeoffice.borders.workflow.form;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.config.FormEngineRefBean;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;

@Slf4j
public class FormEngineService {

    private FormEngineRefBean formEngineRefBean;

    private RestTemplate restTemplate;

    public FormEngineService(FormEngineRefBean formEngineRefBean, RestTemplate restTemplate) {
        this.formEngineRefBean = formEngineRefBean;
        this.restTemplate = restTemplate;
    }

    public String getFormId(String formKey) {
        if (formKey != null) {
            String url = formEngineRefBean.getUrl() + "/form?name=" + formKey;
            FormListDto forms = restTemplate.getForEntity(url, FormListDto.class).getBody();
            if (forms == null || forms.getTotal() == 0) {
                throw new InternalWorkflowException("Could not find form for name " + formKey);
            }
            return forms.getForms().get(0).getId();
        }
        return null;
    }
}
