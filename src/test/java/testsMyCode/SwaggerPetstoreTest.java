package testsMyCode;

import com.beust.jcommander.Parameter;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;

@Listeners({AllureTestListener.class, LogListener.class})
public class SwaggerPetstoreTest {


    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";
        // Настройка Selenide
        Configuration.baseUrl = "https://petstore.swagger.io";
        Configuration.screenshots = true; // Включаем скриншоты
        Configuration.savePageSource = true; // Сохраняем исходный код страницы
        Configuration.reportsFolder = "target/allure-results";
    }

    @Test
    @Description("Проверка получения информации о питомце по ID")
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение информации о питомце")
    @Feature("Petstore API")
    public void testGetPetById() {
        int petId = 1;
        Response response = getPetById(petId);
        verifyPetResponse(response, petId);
    }

    @Step("Получить информацию о питомце по ID: {petId}")
    private Response getPetById(int petId) {
        Response response = given()
                .pathParam("petId", petId)
                .when()
                .get("/pet/{petId}")
                .then()
                .extract().response();

        // Добавляем вложение в отчет Allure
        Allure.addAttachment("Response Body", response.getBody().asString());
        return response;
    }

    @Step("Проверить ответ на запрос информации о питомце по ID: {petId}")
    private void verifyPetResponse(Response response, int petId) {
        // Проверяем, что статус код 200
        Assert.assertEquals(response.getStatusCode(), 200, "Статус код не 200");

        // Проверяем, что ID питомца совпадает с ожидаемым
        int responsePetId = response.jsonPath().getInt("id");
        Assert.assertEquals(responsePetId, petId, "ID питомца не совпадает с ожидаемым");

        // Добавляем вложение в отчет Allure
        Allure.addAttachment("Pet Response", response.getBody().asString());
    }

    @Test
    @Description("Проверка поиска животных по статусу")
    @Severity(SeverityLevel.NORMAL)
    @Story("Поиск животных по статусу")
    @Feature("Petstore API")
    public void testFindPetsByStatus() {
        String status = "available";
        Response response = findPetsByStatus(status);
        verifyPetsByStatusResponse(response, status);
    }

    @Step("Найти животных по статусу: {0}")
    @Attachment(value = "список животных в JSON формате", type = "application/json", fileExtension = ".txt")
    private Response findPetsByStatus(String status) {
        Response response = given()
                .queryParam("status", status)
                .when()
                .get("/pet/findByStatus")
                .then()
                .extract().response();

        Allure.addAttachment("Response Body", "application/json", response.getBody().asString());
        return response;
    }

    @Step("Проверить ответ на запрос поиска животных по статусу: {status}")
    private void verifyPetsByStatusResponse(Response response, String status) {
        Assert.assertEquals(response.getStatusCode(), 200, "Статус код не 200");
        String[] responseStatuses = response.jsonPath().getList("status").toArray(new String[0]);
        for (String responseStatus : responseStatuses) {
            Assert.assertEquals(responseStatus, status, "Статус животного не совпадает с ожидаемым");
        }
        Allure.addAttachment("Pets by Status Response", response.getBody().asString());
    }

    @Test
    @Description("Тест, который должен упасть и сохранить скриншот")
    @Severity(SeverityLevel.NORMAL)
    @Story("Тест с падением")
    @Feature("Petstore API")
    public void testFailingTestWithScreenshot() {
        open("https://petstore.swagger.io");
        SelenideElement element = $(byXpath("//non-existent-element"));
        element.shouldBe(text("This text does not exist"));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) {
            AllureTestListener.saveScreenshotOnFailure();
        }
    }
}