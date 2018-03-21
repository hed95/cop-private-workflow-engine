package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private RestTemplate restTemplate;
    private String referenceDataEndpoint;
    private ObjectMapper objectMapper;


    public User findByUserId(String userId) {
        String response = restTemplate.getForEntity(String.format("%s/_QUERIES/read/get-active-user?email=%s",
                referenceDataEndpoint, userId), String.class).getBody();
        JSONArray o = new JSONArray(response);
        if (o.length() == 0) {
            throw new ForbiddenException("No active user found in store");
        }
        if (o.length() > 1) {
            throw new ForbiddenException("Cannot have multiple active session for user");
        }
        try {
            return objectMapper.readValue(o.getJSONObject(0).get("row_to_json").toString(), User.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public List<User> allUsers() {
        String response = restTemplate.getForEntity(String.format("%s/_QUERIES/read/get-active-users", referenceDataEndpoint), String.class).getBody();
        JSONArray o = new JSONArray(response);
        try {
            return objectMapper.readValue(o.getJSONObject(0).get("array_to_json").toString(), new TypeReference<List<User>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public List<User> findByQuery(UserQuery query) {

        if (query.getId() != null) {
            return Collections.singletonList(findByUserId(query.getId()));
        }

        List<User> users = allUsers();
        if (query.getFirstName() != null) {
            users.removeIf(user -> !user.getFirstName().equals(query.getFirstName()));
        }
        if (query.getLastName() != null) {
            users.removeIf(user -> !user.getLastName().equals(query.getLastName()));
        }
        if (query.getEmail() != null) {
            users.removeIf(user -> !user.getEmail().equals(query.getEmail()));
        }
        if (query.getGroupId() != null) {
            users.removeIf(user -> !user.isMemberOf(query.getGroupId()));
        }
        return new ArrayList<>();
    }
}
