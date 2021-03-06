package tests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import models.DatasetModel;
import models.Photo;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import services.DatasetsService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestsOnGetDataset extends BaseTest {

    @Test(groups = "positiveTest")
    public void dataPositiveProjectionQuery() {
        DatasetsService datasetsService = new DatasetsService(properties);
        checkDataPosNotNull(datasetsService);
        checkDataPosProjectionQuery(datasetsService);
    }

    @Test(groups = "negativeTest")
    public void dataNegativeProjectionQuery() {
        DatasetsService datasetsService = new DatasetsService(properties, true);
        checkDataNegProjectionQuery(datasetsService);
    }

    private void checkDataPosNotNull(DatasetsService datasetsService, String... fields) {
        //без соль приходит пустой массив, но не null!
        RequestSpecification requestSpecification = datasetsService.requestBuilder()
                .build();
        ValidatableResponse response = datasetsService.executeGetDatasets(requestSpecification)
                .then()
                .assertThat()
                .body("size()", notNullValue());

        response.statusCode(HttpStatus.SC_OK);
    }

    private void checkDataPosProjectionQuery(DatasetsService datasetsService, String... fields) {
        RequestSpecification requestSpecification = datasetsService.requestBuilder()
                .sol(1002)
                .build();
        ValidatableResponse response = datasetsService.executeGetDatasets(requestSpecification)
                .then()
                .assertThat()
                .body("photos.sol", everyItem(is(1002)));

        response.statusCode(HttpStatus.SC_OK);
    }

    private void checkDataNegProjectionQuery(DatasetsService datasetsService, String... fields) {
        RequestSpecification requestSpecification = datasetsService.requestBuilder()
                .sol(1000)
                .getFields(fields)
                .build();
        ValidatableResponse response = datasetsService.executeGetDatasets(requestSpecification)
                .then()
                .assertThat()
                .body("error.code", containsString("API_KEY_MISSING"));

        response.statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test(groups = "models")
    public void getDatasetPhotos() {
        DatasetsService datasetsService = new DatasetsService(properties);
        RequestSpecification requestSpecification = datasetsService.requestBuilder()
                .sol(1001)
                .camera("MAST")
                .build();

        DatasetModel dataset = datasetsService.getDataset(requestSpecification);
        for (Photo photo: dataset.getPhotos()) {
            assertThat(photo.getSol(), is(1001));
            assertThat(photo.getCamera().getName(), containsString("MAST"));
            assertThat(photo.getImgSrc(), notNullValue());
        }
    }
}
