package com.sm.controller;

import com.sm.security.SecurityConstants;
import com.sm.controller.response.UserResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersControllerTestContainerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String authorizationToken;

    //@Container
    @ServiceConnection
    private static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4.0");

    static {
        mySQLContainer.start();
    }

    @Order(1)
    @Test
    @DisplayName("The MySQL container is created and is running")
    void testContainerIsRunning() {
        assertTrue(mySQLContainer.isCreated(), "MySQL container has not been created");
        assertTrue(mySQLContainer.isRunning(), "MySQL container is not running");
    }

    @Order(2)
    @Test
    @DisplayName("User can be created")
    //@Order(1)
    void testCreateUser_whenValidDetailsProvided_returnsUserDetails() throws JSONException {
        // Arrange
        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "ali");
        userDetailsRequestJson.put("lastName", "ahmed");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password","12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), headers);

        // Act
        ResponseEntity<UserResponse> createdUserDetailsEntity = testRestTemplate.postForEntity("/users",
                                                                                               request,
                                                                                               UserResponse.class);
        UserResponse createdUserDetails = createdUserDetailsEntity.getBody();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        Assertions.assertEquals(userDetailsRequestJson.getString("firstName"),
                createdUserDetails.getFirstName(),
                "Returned user's first name seems to be incorrect");
        Assertions.assertEquals(userDetailsRequestJson.getString("lastName"),
                createdUserDetails.getLastName(),
                "Returned user's last name seems to be incorrect");
        Assertions.assertEquals(userDetailsRequestJson.getString("email"),
                createdUserDetails.getEmail(),
                "Returned user's email seems to be incorrect");
        Assertions.assertFalse(createdUserDetails.getUserId().trim().isEmpty(),
                "User id should not be empty");

    }

    @Test
    @DisplayName("GET /users requires JWT")
    @Order(3)
    void testGetUsers_whenMissingJWT_returns403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity requestEntity = new HttpEntity(null, headers);

        // Act
        ResponseEntity<List<UserResponse>> response = testRestTemplate.exchange("/users",
                                                                                HttpMethod.GET,
                                                                                requestEntity,
                                                                                new ParameterizedTypeReference<List<UserResponse>>() {
                });

        // Assert
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "HTTP Status code 403 Forbidden should have been returned");
    }

    @Test
    @DisplayName("/login works")
    @Order(4)
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        // Arrange
//        String loginCredentialsJson = "{\n" +
//                "    \"email\":\"test3@test.com\",\n" +
//                "    \"password\":\"12345678\"\n" +
//                "}";
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email","test@test.com");
        loginCredentials.put("password","12345678");

        HttpEntity<String> request = new HttpEntity<>(loginCredentials.toString());

        // Act
        ResponseEntity response = testRestTemplate.postForEntity("/users/login",
                request,
                null);

        authorizationToken = response.getHeaders().
                getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP Status code should be 200");
        Assertions.assertNotNull(authorizationToken,
                "Response should contain Authorization header with JWT");
        Assertions.assertNotNull(response.getHeaders().
                        getValuesAsList("UserID").get(0),
                "Response should contain UserID in a response header");
    }

    @Test
    @Order(5)
    @DisplayName("GET /users works")
    void testGetUsers_whenValidJWTProvided_returnsUsers() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authorizationToken);

        HttpEntity requestEntity = new HttpEntity(headers);

        // Act
        ResponseEntity<List<UserResponse>> response = testRestTemplate.exchange("/users",
                                                                                HttpMethod.GET,
                                                                                requestEntity,
                                                                                new ParameterizedTypeReference<List<UserResponse>>() {
                });

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP Status code should be 200");
        Assertions.assertTrue(response.getBody().size() == 1,
                "There should be exactly 1 user in the list");
    }
}
