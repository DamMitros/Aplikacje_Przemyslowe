package com.example.zad1;

import com.example.zad1.exception.ApiException;
import com.example.zad1.model.Employee;
import com.example.zad1.service.ApiService;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

public class ApiServiceTest {

    @Test
    void shouldFetchAndMapEmployees_whenHttp200_validJson() throws Exception {
        String json = """
                [
                  {
                    "id": 1,
                    "name": "Leanne Graham",
                    "username": "Bret",
                    "email": "Sincere@april.biz",
                    "address": {
                      "street": "Kulas Light",
                      "suite": "Apt. 556",
                      "city": "Gwenborough",
                      "zipcode": "92998-3874",
                      "geo": {
                        "lat": "-37.3159",
                        "lng": "81.1496"
                      }
                    },
                    "phone": "1-770-736-8031 x56442",
                    "website": "hildegard.org",
                    "company": {
                      "name": "Romaguera-Crona",
                      "catchPhrase": "Multi-layered client-server neural-net",
                      "bs": "harness real-time e-markets"
                    }
                  },
                  {
                    "id": 2,
                    "name": "Ervin Howell",
                    "username": "Antonette",
                    "email": "Shanna@melissa.tv",
                    "address": {
                      "street": "Victor Plains",
                      "suite": "Suite 879",
                      "city": "Wisokyburgh",
                      "zipcode": "90566-7771",
                      "geo": {
                        "lat": "-43.9509",
                        "lng": "-34.4618"
                      }
                    },
                    "phone": "010-692-6593 x09125",
                    "website": "anastasia.net",
                    "company": {
                      "name": "Deckow-Crist",
                      "catchPhrase": "Proactive didactic contingency",
                      "bs": "synergize scalable supply-chains"
                    }
                  },
                  {
                    "id": 3,
                    "name": "Clementine Bauch",
                    "username": "Samantha",
                    "email": "Nathan@yesenia.net",
                    "address": {
                      "street": "Douglas Extension",
                      "suite": "Suite 847",
                      "city": "McKenziehaven",
                      "zipcode": "59590-4157",
                      "geo": {
                        "lat": "-68.6102",
                        "lng": "-47.0653"
                      }
                    },
                    "phone": "1-463-123-4447",
                    "website": "ramiro.info",
                    "company": {
                      "name": "Romaguera-Jacobson",
                      "catchPhrase": "Face to face bifurcated interface",
                      "bs": "e-enable strategic applications"
                    }
                  }
                ]
                """;
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(json);
        when(client.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");
        List<Employee> employees = apiService.fetchEmployeesFromApi();
        assertEquals(3, employees.size());
    }

    @Test
    void shouldThrowApiException_whenHttpNot200() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(500);
        when(client.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");

        assertThrows(ApiException.class, apiService::fetchEmployeesFromApi);
    }

    @Test
    void shouldThrowApiException_whenInvalidJson() throws Exception {
        String invalidJson = "{ invalid json ";
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(invalidJson);
        when(client.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");

        assertThrows(ApiException.class, apiService::fetchEmployeesFromApi);
    }

    @Test
    void shouldHandleEmptyArray_whenValidJson() throws Exception {
        String emptyJsonArray = "[]";
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(emptyJsonArray);
        when(client.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");
        List<Employee> employees = apiService.fetchEmployeesFromApi();
        assertTrue(employees.isEmpty());
    }

    @Test
    void shouldHandleMissingFields_whenValidJson() throws Exception {
        String jsonWithMissingFields = """
                [
                  {
                    "id": 1,
                    "username": "Bret",
                    "email": "bret@cokolwiek.pl"
                  }
                ]
                
                """;
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(jsonWithMissingFields);
        when(client.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())
        ).thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");
        List<Employee> employees = apiService.fetchEmployeesFromApi();
        assertEquals(1, employees.size());
        Employee emp = employees.get(0);
        assertNull(emp.getFullName());
        assertNull(emp.getCompanyName());
    }

    @Test
    void shouldHandleCompanyObject_withNullCompanyName() throws Exception {
        String json = """
            [
              {
                "id": 1,
                "name": "Leanne Graham",
                "email": "leanne@example.com",
                "company": { "name": null }
              }
            ]
            """;

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(json);
        when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        assertEquals(1, employees.size());
        Employee emp = employees.get(0);
        assertEquals("Leanne Graham", emp.getFullName());
        assertEquals("leanne@example.com", emp.getEmail());
        assertNull(emp.getCompanyName());
    }

    @Test
    void shouldHandleCompanyWhenIsStringValue() throws Exception {
        String json = """
            [
              {
                "id": 1,
                "name": "Leanne Graham",
                "email": "leanne2@example.com",
                "company": "NotAnObject"
              }
            ]
            """;

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(json);
        when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        ApiService apiService = new ApiService(client, "https://nonexist");
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        assertEquals(1, employees.size());
        Employee emp = employees.get(0);
        assertEquals("Leanne Graham", emp.getFullName());
        assertEquals("leanne2@example.com", emp.getEmail());
        assertNull(emp.getCompanyName());
    }

    @Test
    void shouldHandleNullCompanyObject() throws Exception {
        String json = """
                [
                  {
                    "id": 1,
                    "name": "Leanne Graham",
                    "email": "leanne@cokolwiek.pl",
                    "company": null
                  }
                ]
                """;

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(json);
        when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any
                ())).thenReturn(response);
        ApiService apiService = new ApiService(client, "https://nonexist");
        List<Employee> employees = apiService.fetchEmployeesFromApi();
        assertEquals(1, employees.size());
        Employee emp = employees.get(0);
        assertNull(emp.getCompanyName());
        }
    }