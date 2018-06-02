package uk.gov.homeoffice.borders.workflow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.homeoffice.borders.workflow.identity.TeamQuery;

import java.net.URI;
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
    public static final String STAFFVIEW = "/staffview";
    private String platformDataUrl;

    public String shiftUrlByEmail(String email) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(SHIFT)
                .query("email=eq.{email}")
                .buildAndExpand(Collections.singletonMap("email", email))
                .toString();
    }
    public String shiftUrlById(String id) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(SHIFT)
                .query("shiftid=eq.{id}")
                .buildAndExpand(Collections.singletonMap("id", id))
                .toString();
    }

    public String teamById(String teamId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path("/team/{teamId}")
                .buildAndExpand(Collections.singletonMap("teamid", teamId))
                .toString();

    }

    public String teamQuery(TeamQuery team) {
        Map<String, Object> variables = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path("/team");

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
                .uri(URI.create(platformDataUrl))
                .path(SHIFT)
                .query("teamid=eq.{teamId}")
                .buildAndExpand(Collections.singletonMap("teamId", teamId))
                .toString();

    }

    public String queryShiftByLocationId(String locationId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(SHIFT)
                .query("locationid=eq.{locationId}")
                .buildAndExpand(Collections.singletonMap("locationId", locationId))
                .toString();

    }

    public String queryShiftByCommandId(String commandId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(SHIFT)
                .query("or=(subcommandid.eq.{commandId}, commandid.eq.{commandId})")
                .buildAndExpand(Collections.singletonMap("commandId", commandId))
                .toString();

    }

    public String getStaffUrl(String staffId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(STAFFVIEW)
                .query("staffid=eq.{staffId}")
                .buildAndExpand(Collections.singletonMap("staffId", staffId))
                .toString();
    }

    public String teamChildren() {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path("/rpc/teamchildren")
                .build()
                .toString();
    }

    public String staffViewIn(List<String> staffIds) {
        String idsToProcess = StringUtils.join(staffIds, ",");
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(STAFFVIEW)
                .query("staffid=in.({ids})")
                .buildAndExpand(Collections.singletonMap("ids", idsToProcess))
                .toString();

    }

    public String queryShiftBySubCommandId(String subCommand) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(platformDataUrl))
                .path(SHIFT)
                .query("subcommandid=eq.{subCommand}")
                .buildAndExpand(Collections.singletonMap("subCommand", subCommand))
                .toString();
    }
}
