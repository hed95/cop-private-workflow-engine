package uk.gov.homeoffice.borders.workflow;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.homeoffice.borders.workflow.config.RefDataBean;
import uk.gov.homeoffice.borders.workflow.identity.TeamQuery;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RefDataUrlBuilder {
    private static final String LOCATION = "/location"; // ref
    private static final String TEAM = "/team"; // ref

    private RefDataBean refDataBean;

    public String teamById(String teamId) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(refDataBean.getUrl()))
                .path("/team?teamcode=eq.{teamId}")
                .buildAndExpand(Collections.singletonMap("teamId", teamId))
                .toString();

    }

    public String teamByIds(String... teamIds) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(refDataBean.getUrl()))
                .path(TEAM)
                .query("teamcode=in.({teamIds})")
                .buildAndExpand(Collections.singletonMap("teamIds", teamIds))
                .toString();

    }

    public String teamQuery(TeamQuery team) {
        Map<String, Object> variables = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .uri(URI.create(refDataBean.getUrl()))
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
            String idsToProcess = String.join(",",idsForProcessing);
            variables.put("ids", idsToProcess);
            builder.query("teamid=in.({ids})");
        });

        return builder
                .buildAndExpand(variables)
                .toString();

    }

    public String teamChildren(final Collection<String> teamIds) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(refDataBean.getUrl()))
                .path(TEAM)
                .query("parentteamid=in.({ids})")
                .buildAndExpand(Collections.singletonMap("ids", teamIds))
                .toString();
    }


    public String getLocation(String currentLocationId) {
        return UriComponentsBuilder
                .newInstance()
                .uri(URI.create(refDataBean.getUrl()))
                .path(LOCATION)
                .query("locationid=eq.{currentLocationId}")
                .buildAndExpand(Collections.singletonMap("currentLocationId", currentLocationId))
                .toString();
    }

}
