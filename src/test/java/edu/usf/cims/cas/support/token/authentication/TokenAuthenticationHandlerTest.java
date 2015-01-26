package edu.usf.cims.cas.support.token.authentication;

import edu.clayton.cas.support.token.keystore.JSONKeystore;
import edu.clayton.cas.support.token.keystore.Key;
import edu.clayton.cas.support.token.util.Crypto;
import org.jasig.cas.authentication.HandlerResult;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenAuthenticationHandlerTest {

    private TokenAuthenticationHandler handler;
    private TokenCredential validCredentials;
    private TokenCredential invalidCredentials;

    private Key clientKey = new Key("alphabet_key", "EKyrqGJnFQrUzohURXsJprFgBAKAPtrv");

    @Before
    public void setup() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("testTokenAttributes.json");
        File file = new File(url.toURI());
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];

        fis.read(buffer);
        fis.close();

        JSONObject tokenData = new JSONObject();
        tokenData.put("generated", (new Date()).getTime());
        tokenData.put("credentials", new JSONObject(new String(buffer)));

        String b64Token = Crypto.encryptWithKey(tokenData.toString(), new String(this.clientKey.data()));

        this.handler = new TokenAuthenticationHandler();
        this.validCredentials = new TokenCredential(
                "auser",
                b64Token,
                "alphabet_key"
        );
        this.invalidCredentials = new TokenCredential(
                "auser",
                b64Token,
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
        this.handler.setUsernameAttribute("username");

        HandlerResult result = this.handler.doAuthentication(this.validCredentials);

        assertEquals(result.getPrincipal().getId(), "auser");
    }

    @Test(expected = java.security.GeneralSecurityException.class)
    public void testAuthFailure() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("testHandlerStoreWithBadKey.json");
        File keystoreFile = new File(url.toURI());
        JSONKeystore jsonKeystore = new JSONKeystore(keystoreFile);

        this.handler.setKeystore(jsonKeystore);
        this.handler.setMaxDrift(Integer.MAX_VALUE);
        this.handler.setUsernameAttribute("username");

        this.handler.doAuthentication(this.invalidCredentials);
    }
}