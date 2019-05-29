package uk.gov.homeoffice.borders.workflow;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.homeoffice.borders.workflow.config.RefDataBean;

import java.util.Collections;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RefDataUrlBuilder {
    private static final String LOCATION = "/location"; // ref

    private RefDataBean refDataBean;

    public String getLocation(String currentLocationId) {
        return UriComponentsBuilder
                .newInstance()
                .uri(refDataBean.getUrl())
                .path(LOCATION)
                .query("locationid=eq.{currentLocationId}")
                .buildAndExpand(Collections.singletonMap("currentLocationId", currentLocationId))
                .toString();
    }

}
