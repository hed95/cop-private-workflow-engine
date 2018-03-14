package uk.gov.homeoffice.borders.workflow.identity;

import lombok.Data;

import java.util.List;

@Data
public class UserDetailDto {

    private String id;
    private String email;
    private String mobile;
    private String grade;
    private List<Team> teams;
}
