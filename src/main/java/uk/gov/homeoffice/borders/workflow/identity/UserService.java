package uk.gov.homeoffice.borders.workflow.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser.ShiftDetails;

import javax.annotation.Resource;
import javax.swing.text.html.Option;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
public class UserService {

    private RestTemplate restTemplate;
    private PlatformDataUrlBuilder platformDataUrlBuilder;
    private RefDataUrlBuilder refDataUrlBuilder;
    //Self reference to enable methods to be called within this service and be proxied by Spring
    @Resource
    private UserService self;


    @Autowired
    public UserService(RestTemplate restTemplate, PlatformDataUrlBuilder platformDataUrlBuilder, RefDataUrlBuilder refDataUrlBuilder) {
        this.restTemplate = restTemplate;
        this.platformDataUrlBuilder = platformDataUrlBuilder;
        this.refDataUrlBuilder = refDataUrlBuilder;
    }

    /**
     * Find user from using shift details
     */
    public PlatformUser findByUserId(String userId) {
        List<ShiftDetails> shiftDetails;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {
            shiftDetails = restTemplate
                    .exchange(platformDataUrlBuilder.shiftUrlByEmail(userId), HttpMethod.GET, new HttpEntity<>(headers),
                            new ParameterizedTypeReference<List<ShiftDetails>>() {
                            }).getBody();
        } catch (Exception e) {
            log.error("Failed to get user", e);
            return null;
        }
        return ofNullable(shiftDetails)
                .filter(s -> !s.isEmpty())
                .map(s -> getStaff(shiftDetails.get(0)))
                .orElse(null);
    }

    private PlatformUser getStaff(final ShiftDetails shiftInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();

        List<PlatformUser> users = restTemplate.exchange(platformDataUrlBuilder.getStaffUrl(),
                HttpMethod.POST, new HttpEntity<>(Collections.singletonMap("argstaffid", shiftInfo.getStaffId()), httpHeaders),
                new ParameterizedTypeReference<List<PlatformUser>>() {
                }).getBody();

        if (CollectionUtils.isEmpty(users)) {
            throw new InternalWorkflowException(String.format("Could not find platform user for %s", shiftInfo.getStaffId()));
        }

        PlatformUser platformUser = users.get(0);
        String teamId = shiftInfo.getTeamId();

        TeamsDto parentTeamsDto = restTemplate.getForEntity(refDataUrlBuilder.teamById(teamId), TeamsDto.class).getBody();
        TeamsDto childTeamsDto = restTemplate.getForEntity(refDataUrlBuilder.teamChildrenByParentTeamId(teamId), TeamsDto.class).getBody();

        List<Team> teams = new ArrayList<>();
        teams.addAll(ofNullable(parentTeamsDto).orElseGet(TeamsDto::new).getData());
        teams.addAll(ofNullable(childTeamsDto).orElseGet(TeamsDto::new).getData());

        platformUser.setTeams(teams);
        platformUser.setShiftDetails(shiftInfo);
        platformUser.setEmail(shiftInfo.getEmail());

        return platformUser;
    }

    public List<PlatformUser> findByQuery(UserQuery query) {
        if (query.getId() != null) {
            return Collections.singletonList(self.findByUserId(query.getId()));
        }
        String url = resolveQueryUrl(query);
        List<ShiftDetails> shifts = restTemplate.exchange(url,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<ShiftDetails>>() {
                }, new HashMap<>()).getBody();
        return ofNullable(shifts).map((shiftDetails -> shiftDetails.stream().map(shift -> {
            PlatformUser platformUser = new PlatformUser();
            platformUser.setShiftDetails(shift);
            platformUser.setId(shift.getStaffId());
            platformUser.setEmail(shift.getEmail());
            return platformUser;
        }).collect(toList()))).orElse(new ArrayList<>());
    }

    private String resolveQueryUrl(UserQuery query) {
        String url = null;
        if (query.getGroupId() != null) {
            url = platformDataUrlBuilder.queryShiftByTeamId(query.getGroupId());
        }

        if (query.getLocation() != null) {
            url = platformDataUrlBuilder.queryShiftByLocationId(query.getLocation());
        }

        if (url == null) {
            throw new IllegalArgumentException("Could not determine url for query");
        }
        return url;
    }


}
