package uk.gov.homeoffice.borders.workflow.shift;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Container to hold data that is being returned by Ref Data Service
 */
@Data
public class LocationsDto {

    private List<Location> data = new ArrayList<>();
}
