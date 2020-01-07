package com.fujitsu.hope.voip;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

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

	// (1) 基本的な利用例
	//@RequestMapping(value = "/add", method = RequestMethod.GET, consumes = "application/json")
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@ResponseBody
	public String addAction() throws IOException, InterruptedException, URISyntaxException{


		//HTTP2を使えるHTTPClientを生成
        HttpClient httpClient = HttpClient.newBuilder()
        		.version(HttpClient.Version.HTTP_2).build();

        System.out.println(httpClient.version());

        //Sample https://www.mkyong.com/java/java-11-httpclient-examples/

        Map<Object, Object> data = new HashMap<>();
        data.put("data", "test");



        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(requestBody))
                .uri(URI.create("https://api.sandbox.push.apple.com:443"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("authorization", "bearer eyAia2lkIjogIjhZTDNHM1JSWDciIH0.eyAiaXNzIjogIkM4Nk5WOUpYM0QiLCAiaWF0I" +
                		"jogIjE0NTkxNDM1ODA2NTAiIH0.MEYCIQDzqyahmH1rz1s-LFNkylXEa2lZ_aOCX4daxxTZkVEGzwIhALvkClnx5m5eAT6" +
                		"Lxw7LZtEQcH6JENhJTMArwLf3sXwi")
                .header("apns-push-type" , "alert")
                .header("apns-id"        , "eabeae54-14a8-11e5-b60b-1697f925ec7b")
                .header("apns-expiration", "0")
                .header("apns-priority"  , "10")
                .header("apns-topic"     , "com.example.MyApp")
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
