package api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.UNDEFINED_PORT;
import static io.restassured.RestAssured.given;



public class ReqresTest {
    private final static String URL = "https://reqres.in/";

    @Test
    public void checkAvatarContainsIdTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        //1 способ сравнивать значения напрямую из экземпляров класса
        List<UserData> users = given()
                .when()
                .get("api/users?page=2")
                .then()
                .log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        //проверка аватар содержит айди
        users.forEach(x-> Assertions.assertTrue(x.getAvatar().contains(x.getId().toString())));
        //проверка почты оканчиваются на reqres.in
        Assertions.assertTrue(users.stream().allMatch(x->x.getEmail().endsWith("@reqres.in")));

    }

    @Test
    public void successUserRegTest(){
        Integer UserId = 4;
        String UserPassword = "QpwL5tke4Pnpja7X4";
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        Register user = new Register("eve.holt@reqres.in","pistol");
        SuccessReg successUserReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then()
                .log().all()
                .extract().as(SuccessReg.class);
        Assertions.assertNotNull(successUserReg.getId());
        Assertions.assertNotNull(successUserReg.getToken());
        Assertions.assertEquals(UserId, successUserReg.getId());
        Assertions.assertEquals(UserPassword, successUserReg.getToken());
    }
    @Test

    public void unSuccessUserRegTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpecError400());
        Register peopleSecond = new Register("sydney@fife","");
        UnsuccessReg unSuccessUserReg = given()
                .body(peopleSecond)
                .when()
                .post("/api/register")
                .then()
                .log().body()
                .extract().as(UnsuccessReg.class);
        Assertions.assertNotNull(unSuccessUserReg.getError());
        Assertions.assertEquals("Missing password", unSuccessUserReg.getError());
    }
    @Test
    public void checkSortedYearsTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        List<ColorsData> data = given()
                .when()
                .get("/api/unknown")
                .then()
                .log().all()
                .extract().body().jsonPath().getList("data", ColorsData.class);

        List<Integer> dataYears = data.stream().map(ColorsData::getYear).collect(Collectors.toList());
        List<Integer> sortedDataYears = dataYears.stream().sorted().collect(Collectors.toList());
        Assertions.assertEquals(dataYears, sortedDataYears);
        System.out.println(dataYears);
        System.out.println(sortedDataYears);
    }
    @Test
    public void deleteUserTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecUnique(204));
        given().when().delete("/api/users/2")
                .then()
                .log().all();
    }
    @Test
    public void checkServerAndPcDateTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        UserTime user = new UserTime("morpheus","zion resident");
        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("/api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);

        String regex = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex,"");

        Assertions.assertEquals(response.getUpdatedAt().replaceAll(regex,""),currentTime);
    }


}
