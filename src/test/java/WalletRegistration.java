import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;

public class WalletRegistration {

    public static Properties propMain = new Properties();
    String nid_front = "";
    String nid_back = "";

    String profilePicture = "";
    String token = "";

    public String login_token = "";

    String wallet_no = "01765841127";

    Connection conn = null;

    Statement statement;
    ResultSet rs;

    String user_id = "";


    public void dbConnectionForProfile() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection("jdbc:postgresql://10.9.0.77:5432/backend_db", "shihab", "shihab@123");

        if (conn != null)
        {
            System.out.println("connection established for profile table" + "\n");
        }
        else {
            System.out.println("connection failed for profile table" + "\n");
        }
    }

    public void fetchUserIdFromProfile() throws SQLException, ClassNotFoundException {
        dbConnectionForProfile();
        String query = String.format("select * from profile pr where pr.wallet_no = '%s'", wallet_no);

        statement = conn.createStatement();
        rs = statement.executeQuery(query);

        while (rs.next())
        {
            user_id = rs.getString("user_id");
            System.out.println("user_id is : " + user_id);
        }
    }

    @Test(priority = 0)
    void signUp() throws ParseException {
        JSONObject requestBody = new JSONObject();

        requestBody.put("device_id", "c02cf9a54e41c047");
        requestBody.put("mobile_number", wallet_no);
        requestBody.put("name", wallet_no);
        requestBody.put("pin", "PIN");
        requestBody.put("role", "CUSTOMER");
        requestBody.put("uuid", "bd5b6dbd-f479-433e-8fb3-d4eda8fa5906");

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", "Basic cHJvZ290aV9xYTpwcjBnMHQxQDIwMnR3bw==").
                header("x-auth-token", "GyE7nOpiJMnIiTAFUIocjJ8tEpLLBMSyamKqlKx3").
                header("x-device-id", "bd5b6dbd-f479-433e-8fb3-d4eda8fa5906").
                header("x-user-mobile", wallet_no).
                body(requestBody.toJSONString()).
                when().
                post("https://stgqa.tallykhata.com/wallet/api/tp-proxy/wallet/signup");

        String signUpResponse = response.getBody().asString();

        System.out.println(signUpResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 1)
    void nidClear()
    {
        Response response = get("http://10.9.0.77:6060/tallypay-backdoor-service/api/backend/nid/6015326314/reset-nid");
        String nidClearResponse = response.getBody().asString();
        System.out.println(nidClearResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 2)
    void nid_front() throws ParseException, IOException, SQLException, ClassNotFoundException {

        fetchUserIdFromProfile();

        FileInputStream fisDev = new FileInputStream(System.getProperty("user.dir") + "/src/test/java/walletReg.properties");
        propMain.load(fisDev);

        nid_front = propMain.getProperty("nid_front");
        token = propMain.getProperty("token");

        JSONObject requestBody = new JSONObject();

        requestBody.put("nidNo", "6015326314");
        requestBody.put("nidType", "LEGACY_NID");
        requestBody.put("remarks", "test");
        requestBody.put("requestId", "123456789_4571");
        requestBody.put("photoFrontSide", nid_front);

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", token).
                body(requestBody.toJSONString()).
                when().
                post("http://10.9.0.77:6060/nobopay-backend/api/document/nid-front/user/" + user_id);

        String nidFrontResponse = response.getBody().asString();

        System.out.println(nidFrontResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 3)
    void nid_back() throws ParseException, IOException {

        FileInputStream fisDev = new FileInputStream(System.getProperty("user.dir") + "/src/test/java/walletReg.properties");
        propMain.load(fisDev);

        nid_back = propMain.getProperty("nid_back");
        token = propMain.getProperty("token");

        JSONObject requestBody = new JSONObject();

        requestBody.put("nidNo", "6015326314");
        requestBody.put("nidType", "LEGACY_NID");
        requestBody.put("requestId", "123456789_4571");
        requestBody.put("photoBackSide", nid_back);

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", token).
                body(requestBody.toJSONString()).
                when().
                post("http://10.9.0.77:6060/nobopay-backend/api/document/nid-back/user/" + user_id);

        String nidBackResponse = response.getBody().asString();

        System.out.println(nidBackResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 4)
    void confirm_nid_info() throws ParseException, IOException {

        JSONObject requestBody = new JSONObject();

        requestBody.put("userId", user_id);
        requestBody.put("addressPermanent", "Union: ওয়ার্ড নং-০৬, Upazilla: পল্লবী, Post-Code: ১২১৬, Post-Office: পল্লবী পোস্ট অফিস, District: ঢাকা, Division: ঢাকা,");
        requestBody.put("addressPresent", "Union: ওয়ার্ড নং-০৬, Upazilla: পল্লবী, Post-Code: ১২১৬, Post-Office: পল্লবী পোস্ট অফিস, District: ঢাকা, Division: ঢাকা,");
        requestBody.put("birthday", "17/05/1997");
        requestBody.put("bloodGroup", null);
        requestBody.put("customerNameBn", "এ.বি.এম.শিহাব উদ্দিন");
        requestBody.put("customerNameEn", "A.B.M.Shihab Uddin");
        requestBody.put("fatherName", "এ.বি.এম.সালাহউদ্দিন");
        requestBody.put("gender", null);
        requestBody.put("monthlyIncome", null);
        requestBody.put("motherName", "শওকত আরা জাহান");
        requestBody.put("occupation", null);
        requestBody.put("spouseName", "");
        requestBody.put("nidNo", "6015326314");

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", token).
                body(requestBody.toJSONString()).
                when().
                post("http://10.9.0.77:6060/nobopay-backend/api/document/nid-info");

        String confirmNidResponse = response.getBody().asString();

        System.out.println(confirmNidResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 5)
    void face_image() throws ParseException, IOException {

        FileInputStream fisDev = new FileInputStream(System.getProperty("user.dir") + "/src/test/java/walletReg.properties");
        propMain.load(fisDev);

        profilePicture = propMain.getProperty("profilePicture");
        token = propMain.getProperty("token");

        JSONObject requestBody = new JSONObject();

        requestBody.put("nidNo", "6015326314");
        requestBody.put("requestId", "123456789_4571");
        requestBody.put("profilePicture", profilePicture);

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", token).
                body(requestBody.toJSONString()).
                when().
                post("http://10.9.0.77:6060/nobopay-backend/api/document/face-image/user/" + user_id);

        String profilePictureResponse = response.getBody().asString();

        System.out.println(profilePictureResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 6)
    void pin_set() throws ParseException, IOException {

        JSONObject requestBody = new JSONObject();

        requestBody.put("new_pin", "1590");
        requestBody.put("uuid", "bd5b6dbd-f479-433e-8fb3-d4eda8fa5906");
        requestBody.put("wallet_no", wallet_no);

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", "Basic cHJvZ290aV9xYTpwcjBnMHQxQDIwMnR3bw==").
                header("x-auth-token", "GyE7nOpiJMnIiTAFUIocjJ8tEpLLBMSyamKqlKx3").
                header("x-device-id", "bd5b6dbd-f479-433e-8fb3-d4eda8fa5906").
                header("x-user-mobile", wallet_no).
                body(requestBody.toJSONString()).
                when().
                put("https://stgqa.tallykhata.com/wallet/api/tp-proxy/pin/set");

        String pinSetResponse = response.getBody().asString();

        System.out.println(pinSetResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 7)
    public void login() throws ParseException {
        JSONObject requestBody = new JSONObject();

        requestBody.put("app_type", "TALLYKHATA");
        requestBody.put("device_id", "1c2ea1a42bcd5c93");
        requestBody.put("device_type", "ANDROID");
        requestBody.put("mobile_number", wallet_no);
        requestBody.put("password", "1590");
        requestBody.put("uuid", "0069a6b1-b510-443b-b51b-db84889ef9f6");

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", "Basic cHJvZ290aV9xYTpwcjBnMHQxQDIwMnR3bw==").
                header("x-auth-token", "GyE7nOpiJMnIiTAFUIocjJ8tEpLLBMSyamKqlKx3").
        body(requestBody.toJSONString()).
                when().
                post("https://stgqa.tallykhata.com/wallet/api/tp-proxy/user/login");

        String loginResponse = response.getBody().asString();
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(loginResponse);

        login_token = "token " + (String) obj.get("token");

        System.out.println(login_token);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test(priority = 8)
    public void attach_mfs() throws ParseException {
        JSONObject requestBody = new JSONObject();

        requestBody.put("otpVerified", true);
        requestBody.put("accountType", "MFS");
        requestBody.put("account_type", "MFS");
        requestBody.put("mfs_type", "BKASH");
        requestBody.put("wallet_no", wallet_no);

        Response response = given().
                header("content-type", "application/json").
                header("Authorization", login_token).
                body(requestBody.toJSONString()).
                when().
                post("http://10.9.0.77:6060/nobopay-api-gateway/api/v1/account");

        String attachMfsResponse = response.getBody().asString();

        System.out.println(attachMfsResponse);

        Assert.assertEquals(200, response.getStatusCode());
    }
}
