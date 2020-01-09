package com.fujitsu.hope.voip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/voipproxy")  // (2) リクエストURLの指定
@Scope("request")    // (3) オブジェクトのスコープ
public class VoipController {

	private static String URL = "";

	/**
	 * （トークンベースの認証に必要）このヘッダーの値はbearer <provider_token>です。
	 * ここで、<provider_token>は、指定されたトピックの通知の送信を許可する暗号化されたトークンです。
	 * 証明書ベースの認証を使用する場合、APNsはこのヘッダーを無視します。詳細については、
	 * 「APNへのトークンベースの接続の確立」を参照してください。
	 * {@link} https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/establishing_a_token-based_connection_to_apns
	 */
	private static String AUTH = "bearer eyAia2lkIjogIjhZTDNHM1JSWDciIH0.eyAiaXNzIjogIkM4Nk5WOUpYM0QiLCAiaWF0I" +
    		"jogIjE0NTkxNDM1ODA2NTAiIH0.MEYCIQDzqyahmH1rz1s-LFNkylXEa2lZ_aOCX4daxxTZkVEGzwIhALvkClnx5m5eAT6" +
    		"Lxw7LZtEQcH6JENhJTMArwLf3sXwi";
	/**
	 * （watchOS 6以降で必須。macOS、iOS、tvOS、iPadOSで推奨）
	 * このヘッダーの値は、通知のペイロードの内容を正確に反映する必要があります。
	 * 不一致がある場合、または必要なシステムでヘッダーが欠落している場合、
	 * APNはエラーを返すか、
	 * 通知の配信を遅らせるか、
	 * または完全にドロップします。
	 */
	private static String PUSH_TYPE = "alert";
	/**
	 * 通知の一意のIDである正規のUUID。
	 * 通知の送信時にエラーが発生した場合、APNsはサーバーにエラーを報告するときにこの値を含めます。
	 * 正規のUUIDは32個の小文字の16進数で、8-4-4-4-12の形式でハイフンで区切られた5つのグループに表示されます。
	 * 例は次のようになります：123e4567-e89b-12d3-a456-42665544000。
	 * このヘッダーを省略すると、APNsはUUIDを作成し、応答で返します。
	 */
	private static String APN_ID = "eabeae54-14a8-11e5-b60b-1697f925ec7b";
	/**
	 * 通知が無効になる日付。
	 * この値は、秒（UTC）で表されるUNIXエポックです。
	 * 値がゼロ以外の場合、APNsは通知を保存し、少なくとも1回は配信を試み、指定された日付まで必要に応じて試行を繰り返します。
	 * 値が0の場合、APNsは通知の配信を1回だけ試行し、保存しません。
	 */
	private static String APNS_EXPIRATION = "0";
	/**
	 * 通知の優先度。
	 * このヘッダーを省略すると、APNsは通知優先度を10に設定します。
	 * 通知をすぐに送信するには、10を指定します。
	 * 10の値は、アラートをトリガーする通知、サウンドを再生する通知、アプリのアイコンにバッジを付ける通知に適しています。
	 * コンテンツが利用可能なキーを含むペイロードを持つ通知にこの優先度を指定すると、エラーが発生します。
	 * 5を指定すると、ユーザーのデバイスの電力に関する考慮事項に基づいて通知が送信されます。
	 * この優先度は、コンテンツで利用可能なキーを含むペイロードを持つ通知に使用します。
	 * この優先度の通知はグループ化され、ユーザーのデバイスに一気に配信される場合があります。
	 * また、抑制される場合があり、配信されない場合もあります。
	 */
	private static String APNS_PRIORITY = "10";
	/**
	 * 通知のトピック。
	 * 一般的に、トピックはアプリのバンドルIDですが、プッシュ通知のタイプに基づいて接尾辞が付いている場合があります。
	 */
	private static String APNS_TOPICS = "com.example.MyApp";

	private static String APNS_DEVICE_TOKEN = "00fc13adff785122b4ad28809a3420982341241";



	private void readConfig() {
        Properties properties = new Properties();

        //プロパティファイルのパスを指定する
        String strpass = "c:/apns/apns.properties";

        try {
            InputStream istream = new FileInputStream(strpass);
            properties.load(istream);

            URL = properties.getProperty("APNS_URL") + "/3/device/" + APNS_DEVICE_TOKEN;
            AUTH = properties.getProperty("APNS_AUTH");
            PUSH_TYPE = properties.getProperty("APNS_PUTH_TYPE");
            APN_ID = properties.getProperty("APNS_APN_ID");
            APNS_EXPIRATION = properties.getProperty("APNS_EXPIRATION");
            APNS_PRIORITY = properties.getProperty("APNS_PRIORITY");
            APNS_TOPICS = properties.getProperty("APNS_TOPICS");

            System.out.println("--------------- apn.properties");
            System.out.println("URL=" + URL);
            System.out.println("AUTH=" + AUTH);
            System.out.println("PUSH_TYPE=" + PUSH_TYPE);
            System.out.println("APN_ID=" + APN_ID);
            System.out.println("APNS_EXPIRATION=" + APNS_EXPIRATION);
            System.out.println("APNS_PRIORITY=" + APNS_PRIORITY);
            System.out.println("--------------- apn.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}


	// (1) 基本的な利用例
	//@RequestMapping(value = "/add", method = RequestMethod.GET, consumes = "application/json")
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@ResponseBody
	public String addAction() throws IOException, InterruptedException, URISyntaxException{

		readConfig();


		//HTTP2を使えるHTTPClientを生成
        HttpClient httpClient = HttpClient.newBuilder()
        		.version(HttpClient.Version.HTTP_2).build();

        System.out.println(httpClient.version());

        //Sample https://www.mkyong.com/java/java-11-httpclient-examples/

        Map<Object, Object> data = new HashMap<>();
        data.put("data", "test");


        //リクエストのBODY部分を構築する
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(requestBody))
                .uri(URI.create(URL))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("authorization"  , AUTH)
                .header("apns-push-type" , PUSH_TYPE)
                .header("apns-id"        , APN_ID)
                .header("apns-expiration", APNS_EXPIRATION)
                .header("apns-priority"  , APNS_PRIORITY)
                .header("apns-topic"     , APNS_TOPICS)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

//        BodyPublisher body = BodyPublishers.noBody();

//        HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://api.sandbox.push.apple.com:443")).POST(body).build(); //Create a GET request for the given URI

		//BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
		//HttpResponse<String> response = HttpClient.newBuilder().build().send(request, bodyHandler);

		System.out.println(response);


	    return "test";
	}


}
