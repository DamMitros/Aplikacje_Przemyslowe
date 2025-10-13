package com.example.zad1.service;

import com.example.zad1.exception.ApiException;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ApiService {
    private static final String URL = "https://jsonplaceholder.typicode.com/users";

    public List<Employee> fetchEmployeesFromApi() throws ApiException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                throw new ApiException("Błąd HTTP: " + statusCode);
            }

            String responseBody = response.body();
            JsonArray arr = JsonParser.parseString(responseBody).getAsJsonArray();
            List<Employee> result = new ArrayList<>();
            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();

                String fullName = safeGetAsString(obj, "name");
                String email = safeGetAsString(obj, "email");
                String companyName = safeGetAsString(obj.getAsJsonObject("company"), "name");

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