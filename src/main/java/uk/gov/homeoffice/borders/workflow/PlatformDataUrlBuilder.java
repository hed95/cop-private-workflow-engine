package uk.gov.homeoffice.borders.workflow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import uk.gov.homeoffice.borders.workflow.config.PlatformDataBean;
import uk.gov.homeoffice.borders.workflow.identity.TeamQuery;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Central place for building urls for Platform Data.
 * This ensures updates to urls happen in one place.
 */

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PlatformDataUrlBuilder {

    private static final String SHIFT = "/shift";
    private static final String SHIFT_HISTORY = "/shifthistory";
    private static final String RPC_STAFF_DETAILS = "/rpc/staffdetails";
    private static final String COMMENTS = "/comment";
    private static final String LOCATION = "/rf_location";
    private static final String TEAM = "/team";
    private static final String RPC_TEAM_CHILDREN ="/rpc/teamchildren";


    private PlatformDataBean platformDataBean;

    public String shiftUrlByEmail(String email) {
        email = UriUtils.encode(email, StandardCharsets.UTF_8);
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(SHIFT)
                .query("email=eq.{email}")
                .buildAndExpand(Collections.singletonMap("email", email))
                .toUriString();
    }

    public String shiftUrlById(String id) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(SHIFT)
                .query("shiftid=eq.{id}")
                .buildAndExpand(Collections.singletonMap("id", id))
                .toString();
    }

    public String shiftHistoryById(String id) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(SHIFT_HISTORY)
                .query("shifthistoryid=eq.{id}")
                .buildAndExpand(Collections.singletonMap("id", id))
                .toString();
    }

    public String teamById(String teamId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path("/team?teamcode=eq.{teamId}")
                .buildAndExpand(Collections.singletonMap("teamId", teamId))
                .toString();

    }

    public String teamByIds(String... teamIds) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(TEAM)
                .query("teamcode=in.({teamIds})")
                .buildAndExpand(Collections.singletonMap("teamIds", teamIds))
                .toString();

    }

    public String teamQuery(TeamQuery team) {
        Map<String, Object> variables = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(TEAM);

        ofNullable(team.getName()).ifPresent(name -> {
            variables.put("name", name);
            builder.query("teamname=eq.{name}");
        });

        ofNullable(team.getId()).ifPresent(id -> {
            variables.put("id", id);
            builder.query("teamid=eq.{id}");
        });

        ofNullable(team.getNameLike()).ifPresent(nameLike -> {
            variables.put("nameLike", nameLike);
            builder.query("teamname=like.{nameLike}");
        });
        ofNullable(team.getIds()).ifPresent(ids -> {
            List<String> idsForProcessing = Arrays.stream(ids).map(id -> "\"" + id + "\"").collect(Collectors.toList());
            String idsToProcess = StringUtils.join(idsForProcessing, ",");
            variables.put("ids", idsToProcess);
            builder.query("teamid=in.({ids})");
        });

        return builder
                .buildAndExpand(variables)
                .toString();

    }


    public String queryShiftByTeamId(String teamId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(SHIFT)
                .query("teamid=eq.{teamId}")
                .buildAndExpand(Collections.singletonMap("teamId", teamId))
                .toString();

    }

    public String queryShiftByLocationId(String locationId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(SHIFT)
                .query("locationid=eq.{locationId}")
                .buildAndExpand(Collections.singletonMap("locationId", locationId))
                .toString();

    }


    public String getStaffUrl() {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(RPC_STAFF_DETAILS)
                .build()
                .toString();
    }

    public String teamChildren() {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(RPC_TEAM_CHILDREN)
                .build()
                .toString();
    }


    public String getCommentsById(String taskId) {
        return UriComponentsBuilder
                .newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(COMMENTS)
                .query("taskid=eq.{taskId}&order=createdon.desc")
                .buildAndExpand(Collections.singletonMap("taskId", taskId))
                .toString();
    }

    public String comments() {
        return UriComponentsBuilder
                .newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(COMMENTS)
                .build()
                .toString();
    }

    public String getLocation(String currentLocationId) {
        return UriComponentsBuilder
                .newInstance()
                .uri(URI.create(platformDataBean.getUrl()))
                .path(LOCATION)
                .query("locationid=eq.{currentLocationId}")
                .buildAndExpand(Collections.singletonMap("currentLocationId", currentLocationId))
                .toString();
    }
}
