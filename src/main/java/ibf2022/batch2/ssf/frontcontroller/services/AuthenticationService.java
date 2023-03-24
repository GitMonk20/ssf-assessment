package ibf2022.batch2.ssf.frontcontroller.services;

import java.net.http.HttpHeaders;
import java.util.Collections;

import javax.naming.AuthenticationException;

import org.apache.tomcat.util.http.parser.MediaType;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {

    private static int failedLoginAttempts = 0;

    
    public static void authenticate(String username, String password) throws AuthenticationException {
        String authUrl = "https://auth.chuklee.com/api/authenticate";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders(null);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        AuthRequest authRequest = new AuthRequest(username, password);
        HttpEntity<AuthRequest> requestEntity = new HttpEntity<>(authRequest, headers);

        try {
            ResponseEntity<AuthResponse> responseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, requestEntity, AuthResponse.class);
            if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
                throw new AuthenticationException("Authentication failed with status code " + responseEntity.getStatusCodeValue());
            }
            System.out.println(responseEntity.getBody().getMessage());
            failedLoginAttempts = 0;
        } catch (HttpClientErrorException e) {
            failedLoginAttempts++;
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new AuthenticationException("Invalid payload");
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthenticationException("Incorrect username and/or password");
            } else {
                throw new AuthenticationException("Authentication failed with status code " + e.getStatusCode().value());
            }
        } catch (RestClientException e) {
            failedLoginAttempts++;
            throw new AuthenticationException("Failed to authenticate with authentication server");
        }
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
	
	public boolean isLocked(String username) {
        Authentication authentication = ((Object) SecurityContextHolder.getContext()).getAuthentication();
        return authentication != null && authentication.isLocked();
    }
}



	
