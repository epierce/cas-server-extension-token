# Token Authentication Module for Jasig CAS Server 4

This module allows you to authenticate and pass attributes for a user with a AES encrypted token instead of a password.   

## How is the token generated?

#### Example code for generating authentication tokens in multiple languages is available in the [cas-token-auth-examples](https://github.com/epierce/cas-token-auth-examples) repository

The token is a AES encrypted JSON object:

```
{   
    "generated":      1338575644294,
    "credentials":  {
      "username":      "epierce",
      "firstname":     "Eric",
      "lastname":      "Pierce",
      "email":         "epierce@mail.usf.edu"
    }
}
```

The _generated_ field is the timestamp in milliseconds, this value is compared against the system time to verify the age of the token.  The _username_ value is also compared to the _username_ request parameter to ensure this token belongs to this user.

All properties of the _credentials_ object will be available to the CAS attribute repository. That is, if you have a property named "ProviderName" in the _credentials_ object, then it will be available under that name for the attribute repository (e.g. `<entry key="CredentialProviderAttribute" value="ProviderName" />`).

## Using Strong encryption (AES-256) in Java
Due to US export restrictions, Java does not include the ability to use strong encryption.  To enable AES-256 support, you must install the [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  Installation instructions and more information on the export restrictions are available in the `README.txt` included in the JCE package.

## Adding Token authentication support to CAS

Use the Maven Overlay Method for configuring CAS.  The wiki article on how to configure it is [here](https://wiki.jasig.org/display/CASUM/Best+Practice+-+Setting+Up+CAS+Locally+using+the+Maven2+WAR+Overlay+Method)

### Add the Maven dependency
Add the following block to the `pom.xml` in your CAS overlay

```
<dependency>
  <groupId>edu.usf.cims</groupId>
  <artifactId>cas-server-extension-token</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Configure Authentication
To authenticate using a token, add a `TokenAuthenticationHandler` bean to `deployerConfigContext.xml`: 

```
<bean id="tokenAuthenticationHandler"
          class="edu.usf.cims.cas.support.token.authentication.TokenAuthenticationHandler"
          p:maxDrift="60"
          p:keystore-ref="jsonKeystore"
          p:usernameAttribute="username"/>

```    
    
* **maxDrift**: Number of seconds (+/-) to allow for clock drift when validating the timestamp of the token.
* **usernameAttribute**: Token attribute that contains the username for this principal

Next, define the following bean to load a `keystore.json` file from `/WEB-INF/classes/`:

```
<bean class="edu.clayton.cas.support.token.keystore.JSONKeystore"
      id="jsonKeystore"
      p:storeFile="classpath:keystore.json" />
```

#### [JSON Keystore format](id:keystore)
The _keystore.json_ file is simply a JSON array of key objects with two properties: _name_ and _data_. For example, the following JSON defines two keys:

```
[
  {
    "name" : "key_one",
    "data" : "LFYgTAXquZEsyAvDUqygDxkQMdgdmHit"
  },
  {
    "name" : "key_two",
    "data" : "ImTshO39fvk0vRiSUmBn1EKptws7Jwwn"
  }
]
```
        
The _name_ property of a key could be anything. The _name_ property is matched against the value of the "token_service" parameter that services provide when requesting authorization. For example, `https://cas.example.com/?token_service=key_two&auth_token=…` will attempt to use the above "key_two" to decrypt the given "auth_token".

The _data_ property is the AES key that will be used to decrypt the provided token.  The key must be **EXACTLY** 32 characters

#### requiredTokenAttributes (Optional)
This bean defines the attributes that **must** be present on the _credentials_ object of the encrypted token in order for the token to be valid. **NOTE: The attribute defined in `usernameAttribute` is always required, wether it is defined in this list or not.** You can define any other required attributes by adding a `requiredTokenAttributes` bean like this:

```
<bean id="requiredTokenAttributes" class="java.util.ArrayList">
  <constructor-arg>
    <list>
      <value>sAMAccountName</value>
      <value>sn</value>
      <value>givenName</value>
    </list>
  </constructor-arg>
</bean>
```
If you want to define required attributes, the `TokenAuthenticationHandler` will need to be adjusted accordingly. For example:

```
<bean id="tokenAuthenticationHandler"
          class="edu.usf.cims.cas.support.token.authentication.TokenAuthenticationHandler"
          p:maxDrift="60"
          p:keystore-ref="jsonKeystore"
          p:usernameAttribute="sAMAccountName"
          p:requiredTokenAttributes-ref="requiredTokenAttributes" />
```

Finally, You need to configure the `authenticationManager` bean to use the token authentication as one of the allowed authentication handlers and include information (authenticationMethod, token generation time and token generator) about the authentication in the principal's attributes:

```
    <bean id="authenticationManager" class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
        <constructor-arg>
            <map>
                <!--
                   | IMPORTANT
                   | Every handler requires a unique name.
                   | If more than one instance of the same handler class is configured, you must explicitly
                   | set its name to something other than its default name (typically the simple class name).
                   -->
                <entry key-ref="proxyAuthenticationHandler" value-ref="proxyPrincipalResolver" />
                <entry key-ref="primaryAuthenticationHandler" value-ref="primaryPrincipalResolver" />
                <entry key-ref="tokenAuthenticationHandler"><null/></entry>
            </map>
        </constructor-arg>

        <property name="authenticationMetaDataPopulators">
           <util:list>
              <bean class="edu.usf.cims.cas.support.token.authentication.TokenAuthenticationMetaDataPopulator" />
           </util:list>
        </property>

        <!--
           | Defines the security policy around authentication. Some alternative policies that ship with CAS:
           |
           | * NotPreventedAuthenticationPolicy - all credential must either pass or fail authentication
           | * AllAuthenticationPolicy - all presented credential must be authenticated successfully
           | * RequiredHandlerAuthenticationPolicy - specifies a handler that must authenticate its credential to pass
           -->
        <property name="authenticationPolicy">
            <bean class="org.jasig.cas.authentication.AnyAuthenticationPolicy" />
        </property>
    </bean>

```
**Note:**  The `TokenAuthenticationHandler` doesn't need a principal resolver because the principal and attributes are extracted at the time of authentication.  Also, please read the information on [authentication policies](https://github.com/Jasig/cas/wiki/Configuring-Authentication-Components) if you haven't done so already.
        
  
### Add `tokenAuthAction` to the CAS webflow
Add `tokenAuthAction` to `login-webflow.xml`. It should be placed at the top of the file, just before the `ticketGratingTicketExistsCheck` decision-state:

```
<action-state id="tokenAuthAction">
  <evaluate expression="tokenAuthAction" />
  <transition on="success" to="sendTicketGrantingTicket" />
  <transition on="error" to="ticketGrantingTicketExistsCheck" />
</action-state>
```

To define the `tokenAuthAction` bean, add it to `cas-servlet.xml`:

```
<bean id="tokenAuthAction" class="edu.usf.cims.cas.support.token.web.flow.TokenAuthAction">
  <property name="centralAuthenticationService" ref="centralAuthenticationService" />
</bean>
```
