package com.zubtsov.elasticsearchsample1.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Deprecated
@Controller
@EnableAutoConfiguration
public class AllowCORSProxy {

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> home(@RequestBody String query) throws IOException {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders elasticHeaders = new HttpHeaders();
        elasticHeaders.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(query, elasticHeaders);

        String response = restTemplate.postForEntity("http://localhost:9200/_search", entity, String.class).getBody();

        ResponseEntity<String> responseEntity = new ResponseEntity<>(response, headers, HttpStatus.OK);

        return responseEntity;
    }

    public static void main(String[] args) {
        SpringApplication.run(AllowCORSProxy.class, args);
    }
}
