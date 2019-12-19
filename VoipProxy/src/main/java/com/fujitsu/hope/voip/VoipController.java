package com.fujitsu.hope.voip;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voipproxy")  // (2) リクエストURLの指定
@Scope("request")    // (3) オブジェクトのスコープ
public class VoipController {

	// (1) 基本的な利用例
	//@RequestMapping(value = "/add", method = RequestMethod.GET, consumes = "application/json")
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@ResponseBody
	public String addAction() throws IOException, InterruptedException, URISyntaxException{


        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

        System.out.println(httpClient.version());

        BodyPublisher body = BodyPublishers.noBody();
        HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://api.sandbox.push.apple.com:443")).POST(body).build(); //Create a GET request for the given URI

		BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
		HttpResponse<String> response = HttpClient.newBuilder().build().send(request, bodyHandler);

		System.out.println(response);


	    return "test";
	}

}
