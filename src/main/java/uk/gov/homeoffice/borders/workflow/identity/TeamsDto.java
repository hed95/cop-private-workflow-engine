package uk.gov.homeoffice.borders.workflow.identity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Container to hold data that is being returned by Ref Data Service
 */
@Data
public class TeamsDto {

    private List<Team> data = new ArrayList<>();
}
