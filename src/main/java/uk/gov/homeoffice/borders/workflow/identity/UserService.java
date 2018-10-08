package uk.gov.homeoffice.borders.workflow.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;
import uk.gov.homeoffice.borders.workflow.shift.ShiftInfo;

import javax.annotation.Resource;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
public class UserService {

    private RestTemplate restTemplate;
    private PlatformDataUrlBuilder platformDataUrlBuilder;
    //Self reference to enable methods to be called within this service and be proxied by Spring
    @Resource
    private UserService self;


    @Autowired
    public UserService(RestTemplate restTemplate, PlatformDataUrlBuilder platformDataUrlBuilder) {
        this.platformDataUrlBuilder = platformDataUrlBuilder;
        this.restTemplate = restTemplate;
    }

    /**
     * Find user from using shift details
     * @param userId
     * @return user
     */
    @Cacheable(value="shifts", key="#userId", unless="#result == null")
    public ShiftUser findByUserId(String userId) {
        List<ShiftInfo> shiftDetails;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {
            shiftDetails = restTemplate
                    .exchange(URI.create(platformDataUrlBuilder.shiftUrlByEmail(userId)), HttpMethod.GET, new HttpEntity<>(headers),
                            new ParameterizedTypeReference<List<ShiftInfo>>() {
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

    private ShiftUser getStaff(final ShiftInfo shiftInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "application/vnd.pgrst.object+json");
        HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ShiftUser> response = restTemplate.exchange(platformDataUrlBuilder.getStaffUrl(shiftInfo.getStaffId()),
                HttpMethod.GET, requestEntity, ShiftUser.class);

        return ofNullable(response.getBody()).map(user -> {
            List<Team> teams = restTemplate
                    .exchange(platformDataUrlBuilder.teamChildren(),
                            HttpMethod.GET,
                            new HttpEntity<>(Collections.singletonMap("id", shiftInfo.getTeamId())),
                            new ParameterizedTypeReference<List<Team>>() {}).getBody();

            user.setTeams(ofNullable(teams).orElse(new ArrayList<>()));
            user.setEmail(shiftInfo.getEmail());
            user.setPhone(shiftInfo.getPhone());
            return user;
        }).orElseThrow(() -> new InternalWorkflowException("Could not find shift user"));

    }

    public List<ShiftUser> findByQuery(UserQuery query) {
        if (query.getId() != null) {
            return Collections.singletonList(self.findByUserId(query.getId()));
        }

        String url = resolveQueryUrl(query);

        List<ShiftInfo> shifts = restTemplate.exchange(url,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<ShiftInfo>>() {
                }, new HashMap<>()).getBody();

        List<String> staffIds = shifts.stream().map(ShiftInfo::getStaffId).collect(Collectors.toList());

        Map<String, ShiftInfo> idsToInfo =
                shifts.stream().collect(Collectors.toMap(ShiftInfo::getStaffId, item -> item));

        List<ShiftUser> users = restTemplate.exchange(platformDataUrlBuilder.staffViewIn(staffIds),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<ShiftUser>>() {
                }).getBody();

        return users.stream().map( (ShiftUser u) -> {
            u.setPhone(idsToInfo.get(u.getId()).getPhone());
            return u;
        }).collect(Collectors.toList());
    }

    private String resolveQueryUrl(UserQuery query) {
        String url = null;
        if (query.getGroupId() != null) {
            url = platformDataUrlBuilder.queryShiftByTeamId(query.getGroupId());
        }

        if (query.getCommand() != null) {
            url = platformDataUrlBuilder.queryShiftByCommandId(query.getCommand());
        }

        if (query.getLocation() != null) {
            url = platformDataUrlBuilder.queryShiftByLocationId(query.getLocation());
        }

        if (query.getSubCommand() != null) {
            url = platformDataUrlBuilder.queryShiftBySubCommandId(query.getSubCommand());
        }

        if (url == null) {
            throw new IllegalArgumentException("Could not determine url for query");
        }
        return url;
    }


}
