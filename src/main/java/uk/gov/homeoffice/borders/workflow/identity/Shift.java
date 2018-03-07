package uk.gov.homeoffice.borders.workflow.identity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Shift {
    private Long expectedLength;
    private LocalDateTime startTime;
}
