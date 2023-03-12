package com.example.takehome;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class CountryController {
    private static final String GRAPHQL_URL = "https://countries.trevorblades.com/graphql";
    private static final String QUERY_TEMPLATE = "{\n    \"operationName\": null,\n    \"variables\": {},\n    \"query\": \"{country(code:\\\"%s\\\"){continent {name countries{ code}}}}\"\n}";

    @PostMapping("/api/countries")
    public ResponseEntity<Map<String, Object>> getCountries(@RequestBody Map<String, Object> requestBody) {
        List<String> countryCodes = (List<String>) requestBody.get("country_codes");
        if (countryCodes == null || countryCodes.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> uniqueCountryCodes = countryCodes.stream().distinct().collect(Collectors.toList());

        String query = String.format(QUERY_TEMPLATE, uniqueCountryCodes.get(0));

        ResponseData response = postGraphQL(query);
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        if (response.data == null || response.data.country == null || response.data.country.continent == null) {
            return ResponseEntity.notFound().build();
        }

        String continentName = response.data.country.continent.name;
        List<String> continentCountryCodes = response.getAllCountryCodes();
        List<String> groupContryCodes = uniqueCountryCodes.stream()
                .filter(code -> continentCountryCodes.contains(code))
                .collect(Collectors.toList());
        List<String> otherContryCodes = uniqueCountryCodes.stream()
                .filter(code -> !continentCountryCodes.contains(code))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("countries", groupContryCodes);
        data.put("name", continentName);
        data.put("otherCountries", otherContryCodes);
        Map<String, Object> result = new HashMap<>();
        result.put("continent", data);

        return ResponseEntity.ok(result);
    }

    private ResponseData postGraphQL(String query) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(query, mediaType);
        Request request = new Request.Builder()
                .url(GRAPHQL_URL)
                .method("POST", body)
                .build();
        try {
            okhttp3.Response responses = client.newCall(request).execute();
            if ((responses.code()) == 200) {
                // Get response
                String jsonData = responses.body().string();
                Gson gson = new Gson();
                return gson.fromJson(jsonData, ResponseData.class);
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Continent {
    public String name;
    public ArrayList<Country> countries;
}

class Country {
    public String code;
    public Continent continent;
}

class Data {
    public Country country;
}

class ResponseData {
    public Data data;

    public List<String> getAllCountryCodes() {
        List<String> countryCodes = new ArrayList<>();
        if (this.data != null && this.data.country != null && this.data.country.continent != null && this.data.country.continent.countries != null) {
            for (Country country : this.data.country.continent.countries) {
                if (country != null && country.code != null) {
                    countryCodes.add(country.code);
                }
            }
        }
        return countryCodes;
    }
}

