package com.example.zad1.service;

import com.example.zad1.exception.ApiException;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {
    private final HttpClient client;
    private final Gson gson;
    private final String url;

    @Autowired
    public ApiService(HttpClient client, Gson gson, @Value("${app.api.url}") String url) {
        this.client = client;
        this.gson = gson;
        this.url = url;
    }

    public ApiService(HttpClient client, String url) {
        this.client = client;
        this.gson = new Gson();
        this.url = url;
    }

    public List<Employee> fetchEmployeesFromApi() throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                throw new ApiException("Błąd HTTP: " + statusCode);
            }

            String responseBody = response.body();
            JsonArray arr = gson.fromJson(responseBody, JsonArray.class);

            List<Employee> result = new ArrayList<>();
            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String fullName = safeGetAsString(obj, "name");
                String email = safeGetAsString(obj, "email");
                JsonObject companyObj = null;
                JsonElement companyEl = obj.get("company");
                if (companyEl != null && !companyEl.isJsonNull() && companyEl.isJsonObject()) {
                    companyObj = companyEl.getAsJsonObject();
                }
                String companyName = safeGetAsString(companyObj, "name");
                Position position = Position.PROGRAMISTA;
                int salary = position.getSalary();

                Employee employee = new Employee(fullName, email, companyName, position, salary);
                result.add(employee);
            }
            return result;
        } catch (Exception e) {
            throw new ApiException("Błąd podczas pobierania lub parsowania danych z API", e);
        }
    }

    private static String safeGetAsString(JsonObject obj, String key) {
        if (obj == null) return null;
        JsonElement elem = obj.get(key);
        return (elem != null && !elem.isJsonNull()) ? elem.getAsString() : null;
    }
}