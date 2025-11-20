# CORS Configuration Guide

## Overview

Cross-Origin Resource Sharing (CORS) must be properly configured to allow the React frontend (running on `http://localhost:5173`) to communicate with the Spring Boot backend (running on `http://localhost:8080`).

## Why CORS is Needed

By default, web browsers enforce the Same-Origin Policy, which prevents JavaScript running on one origin from accessing resources on a different origin. Since our frontend and backend run on different ports, they are considered different origins:

- **Frontend Origin:** `http://localhost:5173` (Vite dev server)
- **Backend Origin:** `http://localhost:8080` (Spring Boot)

## Spring Boot CORS Configuration

### Option 1: Global CORS Configuration (Recommended)

Create a configuration class in your Spring Boot application:

**File:** `backend/src/main/java/com/photogallery/config/CorsConfig.java`

```java
package com.photogallery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow requests from frontend origin
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all HTTP methods
        config.addAllowedMethod("*");

        // Expose headers that client can access
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Content-Disposition"
        ));

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
```

### Option 2: WebMvcConfigurer Approach

Alternatively, configure CORS by implementing `WebMvcConfigurer`:

**File:** `backend/src/main/java/com/photogallery/config/WebConfig.java`

```java
package com.photogallery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### Option 3: Controller-Level CORS (Not Recommended for This Project)

You can use `@CrossOrigin` annotation on individual controllers, but this is less maintainable:

```java
@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "http://localhost:5173")
public class PhotoController {
    // Controller methods...
}
```

## Configuration Breakdown

### Allowed Origins
```java
config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
```
- Specifies which origins can make requests
- For development: `http://localhost:5173` (Vite default port)
- For production: Update to your production frontend URL

### Allowed Methods
```java
config.addAllowedMethod("*");
```
- Allows all HTTP methods (GET, POST, PUT, DELETE, OPTIONS, etc.)
- Can be restricted to specific methods if needed

### Allowed Headers
```java
config.addAllowedHeader("*");
```
- Allows all request headers
- Important for multipart file uploads and custom headers

### Exposed Headers
```java
config.setExposedHeaders(Arrays.asList(
    "Authorization",
    "Content-Type",
    "Content-Disposition"
));
```
- Headers that the frontend JavaScript can access
- `Content-Disposition` is important for file downloads

### Allow Credentials
```java
config.setAllowCredentials(true);
```
- Allows cookies and authorization headers to be sent
- Required if you add authentication later

### Max Age
```java
config.setMaxAge(3600L);
```
- How long (in seconds) the browser should cache preflight responses
- 3600 = 1 hour (reduces preflight OPTIONS requests)

## Testing CORS Configuration

### 1. Check Preflight Request

When the browser makes a cross-origin request, it first sends an OPTIONS preflight request:

```bash
curl -X OPTIONS http://localhost:8080/api/photos \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  -v
```

Expected response headers:
```
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
Access-Control-Allow-Credentials: true
```

### 2. Test Actual Request

```bash
curl -X GET http://localhost:8080/api/photos \
  -H "Origin: http://localhost:5173" \
  -v
```

Expected response headers should include:
```
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
```

### 3. Frontend Test

In your browser console (with frontend running):

```javascript
fetch('http://localhost:8080/api/photos')
  .then(response => response.json())
  .then(data => console.log('Success:', data))
  .catch(error => console.error('CORS Error:', error));
```

If CORS is configured correctly, you should see the response data, not a CORS error.

## Common CORS Issues and Solutions

### Issue 1: "No 'Access-Control-Allow-Origin' header"

**Error:**
```
Access to fetch at 'http://localhost:8080/api/photos' from origin 'http://localhost:5173'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present.
```

**Solution:**
- Ensure CORS configuration is present and properly registered
- Verify the allowed origin matches exactly (check port number)
- Check that the configuration class has `@Configuration` annotation

### Issue 2: "Credentials Flag is True but Access-Control-Allow-Credentials is Missing"

**Error:**
```
The value of the 'Access-Control-Allow-Origin' header must not be the wildcard '*'
when the request's credentials mode is 'include'.
```

**Solution:**
- Don't use `"*"` for allowed origins when `allowCredentials(true)` is set
- Specify exact origin: `"http://localhost:5173"`

### Issue 3: Preflight Request Fails

**Error:**
```
Response to preflight request doesn't pass access control check
```

**Solution:**
- Ensure Spring Boot is configured to handle OPTIONS requests
- Check that allowed methods include "OPTIONS"
- Verify max-age is set appropriately

### Issue 4: File Upload CORS Issues

**Error:**
```
Request header field content-type is not allowed by Access-Control-Allow-Headers
```

**Solution:**
- Ensure `addAllowedHeader("*")` is configured
- Or specifically allow: `"Content-Type"`, `"Accept"`, `"multipart/form-data"`

## Production Considerations

### Environment-Based Configuration

Use environment variables for allowed origins:

```java
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        // ... rest of configuration
    }
}
```

**application.properties:**
```properties
# Development
cors.allowed-origins=http://localhost:5173

# Production (example)
# cors.allowed-origins=https://yourdomain.com,https://www.yourdomain.com
```

### Security Best Practices

1. **Never use wildcards in production:**
   - Avoid `config.addAllowedOrigin("*")`
   - Specify exact allowed origins

2. **Limit allowed methods:**
   - Only allow methods your API actually uses
   - Example: `.allowedMethods("GET", "POST", "DELETE")`

3. **Restrict allowed headers:**
   - In production, specify exact headers instead of `"*"`
   - Example: `.allowedHeaders("Content-Type", "Authorization")`

4. **Use HTTPS in production:**
   - All origins should use HTTPS
   - Example: `https://yourdomain.com`

5. **Set appropriate max-age:**
   - Balance between performance and security
   - Shorter max-age for frequently changing configurations

## Debugging CORS

### Enable CORS Logging

Add to `application.properties`:

```properties
logging.level.org.springframework.web.cors=DEBUG
```

This will log all CORS-related decisions made by Spring Boot.

### Browser DevTools

1. Open browser DevTools (F12)
2. Go to Network tab
3. Look for OPTIONS preflight requests
4. Check response headers for CORS headers
5. Verify request headers sent by the browser

### Common Headers to Check

**Request Headers:**
- `Origin: http://localhost:5173`
- `Access-Control-Request-Method: POST`
- `Access-Control-Request-Headers: content-type`

**Response Headers:**
- `Access-Control-Allow-Origin: http://localhost:5173`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE`
- `Access-Control-Allow-Headers: *`
- `Access-Control-Allow-Credentials: true`

## Integration with Frontend

### Axios Configuration (if using Axios)

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true, // Important for CORS with credentials
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
```

### Fetch API Configuration

```javascript
fetch('http://localhost:8080/api/photos', {
  method: 'GET',
  credentials: 'include', // Important for CORS with credentials
  headers: {
    'Content-Type': 'application/json',
  },
})
```

## Summary

For this project, use **Option 1 (Global CORS Configuration)** as it provides:
- Centralized configuration
- Easy to maintain
- Proper handling of all endpoints under `/api/**`
- Fine-grained control over CORS settings

The configuration allows the Vite development server (`http://localhost:5173`) to communicate with the Spring Boot backend (`http://localhost:8080`) without CORS issues.
