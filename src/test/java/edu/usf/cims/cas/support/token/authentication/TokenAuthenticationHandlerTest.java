package edu.usf.cims.cas.support.token.authentication;

import edu.clayton.cas.support.token.keystore.JSONKeystore;
import org.jasig.cas.authentication.HandlerResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.constraints.AssertTrue;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenAuthenticationHandlerTest {
    private String b64Token =
            "9WV+J40K+tfISLlliYwx320WrfUpfkPN2uCelSOxaN+JgfdPSr9E4qYTbvei0mmEXcUNczygVbW6\n" +
                    "Qk8BpsMPqTnos9TWx8NPLKk1ghykEES1gOVCcUzMqx4C+0sFbUsSs3Ory8KBjNzM/eAv0Cd0ZKnn\n" +
                    "mbo5Y94Pl+ZQzDd+sWCBD3swENa018rNyQ1IGVbYHRbooJvtfsY/aJu+GPkL3+zE4YxKYnBZYqxV\n" +
                    "d7+T4iE=";

    private TokenAuthenticationHandler handler;
    private TokenCredential validCredentials;
    private TokenCredential invalidCredentials;

    @Before
    public void setup() {
        this.handler = new TokenAuthenticationHandler();
        this.validCredentials = new TokenCredential(
                "jsumners",
                this.b64Token,
                "alphabet_key"
        );
        this.invalidCredentials = new TokenCredential(
                "jsumners",
                this.b64Token,
                "number_key"
        );
    }

    @Test
    public void testSupports() {
        assertTrue(this.handler.supports(validCredentials));
    }

    @Test
    public void testAuthSuccess() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("testHandlerStoreWithGoodKey.json");
        File keystoreFile = new File(url.toURI());
        JSONKeystore jsonKeystore = new JSONKeystore(keystoreFile);

        this.handler.setKeystore(jsonKeystore);
        this.handler.setMaxDrift(Integer.MAX_VALUE);

        HandlerResult result = this.handler.doAuthentication(this.validCredentials);

        assertEquals(result.getPrincipal().getId(), "jsumners");
    }

    @Test(expected = java.security.GeneralSecurityException.class)
    public void testAuthFailure() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("testHandlerStoreWithBadKey.json");
        File keystoreFile = new File(url.toURI());
        JSONKeystore jsonKeystore = new JSONKeystore(keystoreFile);

        this.handler.setKeystore(jsonKeystore);
        this.handler.setMaxDrift(Integer.MAX_VALUE);

        this.handler.doAuthentication(this.invalidCredentials);
    }
}