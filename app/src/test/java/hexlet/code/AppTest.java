package hexlet.code;

import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrlCheck;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import okhttp3.mockwebserver.MockWebServer;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

class AppTest {

    private static Javalin app;
    private static String baseUrl;


    public static String readingFileToString(String pathToFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(pathToFile)));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }


    @Nested
    class BasicTests {

        @Test
        void testCreate() {
            String someUrl = "https://www.youtube.com/watch?v=iJ_2fJWfh1c";
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                            .field("url", someUrl).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);
            Url urlFromDB = new QUrl().name.contains("https://www.youtube.com").findOne();
            assertThat(urlFromDB.getName()).isNotNull();
            assertThat(urlFromDB.getName()).isEqualTo("https://www.youtube.com");
        }

        @Test
        void testCreateUrlWithPort() {
            String someUrl = "https://ru.hexlet.io:12345/courses/java-basics";
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", someUrl).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);

            Url urlFromDB = new QUrl().name.contains("https://ru.hexlet.io:12345").findOne();
            assertThat(urlFromDB.getName()).isNotNull();
            assertThat(urlFromDB.getName()).isEqualTo("https://ru.hexlet.io:12345");
        }

        @Test
        void testShowAll() {
            String someUrl = "https://foobar.com/stydies";
            Unirest.post(baseUrl + "/urls").field("url", someUrl).asEmpty();

            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://foobar.com");

        }

        @Test
        void testShowOne() {
            String someUrl = "https://en.wikipedia.org/wiki/Java_(programming_language)";
            Unirest.post(baseUrl + "/urls").field("url", someUrl).asEmpty();

            int currentId = new QUrl().name.contains("https://en.wikipedia.org").findOne().getId();
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + currentId).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://en.wikipedia.org");
        }

        @Test
        void testCheckUrl() throws IOException {
            MockWebServer mockServer = new MockWebServer();
            mockServer.start();
            String testingUrl = mockServer.url("/").toString();

            String body = readingFileToString("src/test/resources/fakeSiteContent1.html");
            MockResponse mockResponse = new MockResponse()
                    .setResponseCode(200)
                    .setBody(body);
            mockServer.enqueue(mockResponse);

            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", testingUrl).asEmpty();

            Url url  = new QUrl().name.contains(testingUrl.substring(0, testingUrl.length() - 1)).findOne();


            HttpResponse<String> responseCheck = Unirest.post(baseUrl + "/urls/" + url.getId() + "/checks").asString();


            HttpResponse<String> responseGet = Unirest.get(baseUrl + "/urls/" + url.getId()).asString();

            UrlCheck urlcheck = new QUrlCheck().findOne();

            assertThat(urlcheck.getTitle()).isEqualTo("Foobar_title");
            assertThat(urlcheck.getH1()).isEqualTo("Ut enim ad minim veniam");
            assertThat(urlcheck.getDescription()).isEqualTo("Lorem ipsum dolor sit amet, consectetur "
                                + "adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua");

            mockServer.shutdown();

        }
    }

}
