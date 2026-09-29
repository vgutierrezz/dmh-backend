import io.restassured.http.ContentType;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestProfile extends BaseIntegrationTest {

    @DisplayName("CP-PER-001 - Consultar perfil")
    @Order(1)
    @Test
    void shouldGetProfileSuccessfully() {

        TestUser user = createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body(
                        "userId",
                        equalTo(user.id().toString())
                )
                .body("balance", notNullValue())
                .body("cvu", notNullValue())
                .body("alias", notNullValue());
    }

    @DisplayName("CP-PER-002 - Usuario inexistente")
    @Order(2)
    @Test
    void shouldReturn404WhenUserDoesNotExist() {

        TestUser user =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .when()
                .get("/api/accounts/user/999999999")
                .then()
                .statusCode(404);
    }

    @DisplayName("CP-PER-003 - Visualizar CVU")
    @Order(3)
    @Test
    void shouldReturnValidCVU() {

        TestUser user =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam(
                        "userId",
                        user.id()
                )
                .when()
                .get("/api/accounts/user/{userId}")
                .then()
                .statusCode(200)
                .body(
                        "cvu",
                        matchesPattern("\\d{22}")
                );
    }

    @DisplayName("CP-PER-004 - Visualizar Alias")
    @Order(4)
    @Test
    void shouldReturnValidAlias() {

        TestUser user =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam(
                        "userId",
                        user.id()
                )
                .when()
                .get("/api/accounts/user/{userId}")
                .then()
                .statusCode(200)
                .body(
                        "alias",
                        matchesPattern(
                                "^[a-zA-Z]+\\.[a-zA-Z]+\\.[a-zA-Z]+$"
                        )
                );
    }

    @DisplayName("CP-PER-005 - Actualizar alias")
    @Order(5)
    @Test
    void shouldUpdateAliasSuccessfully() {

        TestUser user = createAndAuthenticateUser();

        String alias = generateUniqueAlias();

        String request = """
            {
              "alias":"%s"
            }
            """.formatted(alias);

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/accounts/" + user.id())
                .then()
                .statusCode(200)
                .body(
                        "alias",
                        equalTo(alias)
                );
    }

    @DisplayName("CP-PER-006 - Alias duplicado")
    @Order(6)
    @Test
    void shouldReturn409WhenAliasAlreadyExists() {

        String duplicatedAlias =
                generateUniqueAlias();

        TestUser userOne =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(userOne.token())
                )
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "alias":"%s"
                    }
                    """.formatted(
                        duplicatedAlias
                ))
                .when()
                .patch(
                        "/api/accounts/" + userOne.id()
                )
                .then()
                .statusCode(200);

        TestUser userTwo =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(userTwo.token())
                )
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "alias":"%s"
                    }
                    """.formatted(
                        duplicatedAlias
                ))
                .when()
                .patch(
                        "/api/accounts/" + userTwo.id()
                )
                .then()
                .statusCode(409);
    }

    @DisplayName("CP-PER-007 - Cuenta inexistente")
    @Order(7)
    @Test
    void shouldReturn404WhenAccountDoesNotExist() {

        TestUser user =
                createAndAuthenticateUser();

        String request = """
                {
                  "alias":"nuevo.alias"
                }
                """;

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/accounts/999999999")
                .then()
                .statusCode(404);
    }
}