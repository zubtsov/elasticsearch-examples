package com.zubtsov.elasticsearchsample1.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//TODO: replace with configurated reverse proxy
@Controller
@EnableAutoConfiguration
public class AllowCORSProxy {
    //TODO: extract exact address
    @RequestMapping(value = "/**", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<String> home(HttpServletRequest request) throws IOException {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");

        RestTemplate restTemplate = new RestTemplate();
        String requestString = request.getRequestURL()
                .append("?")
                .append(request.getQueryString())
                .toString()
                .replace("8080", "8983"); //TODO: refactor

        String response = restTemplate.getForEntity(requestString, String.class).getBody();

        ResponseEntity<String> responseEntity = new ResponseEntity<>(response, headers, HttpStatus.OK);

        return responseEntity;
    }

    public static void main(String[] args) {
        SpringApplication.run(AllowCORSProxy.class, args);
    }
}
