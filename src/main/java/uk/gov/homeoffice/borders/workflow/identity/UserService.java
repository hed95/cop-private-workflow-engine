package uk.gov.homeoffice.borders.workflow.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.shift.ShiftInfo;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public User findByUserId(String userId) {
        List<ShiftInfo> shiftDetails = restTemplate
                .exchange(platformDataUrlBuilder.shiftUrl(userId), HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<ShiftInfo>>() {
                        }, new HashMap<>()).getBody();
        if (shiftDetails != null && shiftDetails.size() == 1) {
            return getStaff(shiftDetails.get(0));
        } else {
            return null;
        }
    }

    private User getStaff(ShiftInfo shiftInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "application/vnd.pgrst.object+json");
        HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);

        User user = restTemplate.exchange(platformDataUrlBuilder.getStaffUrl(shiftInfo.getStaffId()),
                HttpMethod.GET, requestEntity, User.class).getBody();


        List<Team> teams = restTemplate
                .exchange(platformDataUrlBuilder.teamChildren(),
                        HttpMethod.POST,
                        new HttpEntity<>(Collections.singletonMap("id", shiftInfo.getTeamId())),
                        new ParameterizedTypeReference<List<Team>>() {}).getBody();

        user.setTeams(teams);
        user.setEmail(shiftInfo.getEmail());
        return user;

    }

    public List<User> findByQuery(UserQuery query) {
        if (query.getId() != null) {
            return Collections.singletonList(self.findByUserId(query.getId()));
        }

        String url = resolveQueryUrl(query);

        List<ShiftInfo> shifts = restTemplate.exchange(url,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<ShiftInfo>>() {
                }, new HashMap<>()).getBody();

        List<String> staffIds = shifts.stream().map(ShiftInfo::getStaffId).collect(Collectors.toList());

        return restTemplate.exchange(platformDataUrlBuilder.staffViewIn(staffIds),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {}).getBody();

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
