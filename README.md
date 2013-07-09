# Token Authentication Module for Jasig CAS Server 3.x

This module allows you to authenticate and pass attributes for a user with a AES128-encrypted token instead of a password.   

## How is the token generated?

#### Example code for generating authentication tokens in multiple languages is available in the [cas-token-auth-examples](https://github.com/epierce/cas-token-auth-examples) repository

The token is a AES-128 encrypted JSON object:

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

## Adding Token authentication support to CAS

### Use the Maven Overlay Method for configuring CAS
The wiki article on how to configure it is [here](https://wiki.jasig.org/display/CASUM/Best+Practice+-+Setting+Up+CAS+Locally+using+the+Maven2+WAR+Overlay+Method)

### Download the `cas-server-extension-token` project
```
git clone https://github.com/epierce/cas-server-extension-token.git
```

### Build the server extension
```         
cd cas-server-extension-token
mvn clean package install
```

### Add the Maven dependency
Add the following block to the `pom.xml` in your CAS overlay

```
<dependency>
  <groupId>edu.usf.cims</groupId>
  <artifactId>cas-server-extension-token</artifactId>
  <version>0.4</version>
</dependency>
```

### Configure Authentication
To authenticate using a token, add the `TokenAuthenticationHandler` bean to the list of authentication handlers in `deployerConfigContext.xml`: 

```
<property name="authenticationHandlers">
  <list>
    <bean class="org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler"
      p:httpClient-ref="httpClient" />
    <bean class="edu.usf.cims.cas.support.token.authentication.handler.support.TokenAuthenticationHandler"
      p:maxDrift="60"
      p:keystore-ref="jsonKeystore" />
  </list>
 </property>
```    
    
* **maxDrift**: Number of seconds (+/-) to allow for clock drift  when validating the timestamp of the token.

You'll also need to add `TokenCredentialsToPrincipalResolver` to the list of principal resolvers:

```
<property name="credentialsToPrincipalResolvers">
  <list>
    <bean class="edu.usf.cims.cas.support.token.authentication.principal.TokenCredentialsToPrincipalResolver" />  
    <bean class="org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver" />
  </list>
</property>
```

Finally, define the following bean (to load a `keystore.json` file from `/WEB-INF/classes/`):

```
<bean class="edu.clayton.cas.support.token.keystore.JSONKeystore"
      id="jsonKeystore"
      p:storeFile="classpath:keystore.json" />
```

### [JSON Keystore format](id:keystore)
Where a _keystore.json_ file is simply a JSON array of key objects with two properties: _name_ and _data_. For example, the following JSON defines two keys:

```
[
  {
    "name" : "alphabet_key",
    "data" : "abcdefghijklmnop"
  },
  {
    "name" : "number_key",
    "data" : "1234567890123456"
  }
]
```
        
The _name_ property of a key could be anything. The _name_ property is matched against the value of the "token_service" parameter that services provide when requesting authorization. For example, `https://cas.example.com/?token_service=number_key&auth_token=…` will attempt to use the above "number_key" to decrypt the given "auth_token".

The _data_ property is the AES128 key that will be used to decrypt the provided token.  The key must be **EXACTLY** 16 characters
        
### Configure Attribute Population and Repository
To convert the profile data received from the decrypted token, configure the `authenticationMetaDataPopulators` property on the `authenticationManager` bean:

```
<property name="authenticationMetaDataPopulators">
  <list>
    <bean class="edu.usf.cims.cas.support.token.authentication.TokenAuthenticationMetaDataPopulator" />
  </list>
</property>
```

You'll also need to configure the `attributeRepository` bean:

``` 
<bean id="attributeRepository" 
  class="org.jasig.services.persondir.support.StubPersonAttributeDao">
  <property name="backingMap">
  <map>
    <entry key="FamilyName" value="FamilyName" />
    <entry key="GivenName" value="GivenName" />
    <entry key="Email" value="Email" />
    </map>
  </property>
</bean>
```
Note: To release the attributes to CAS clients, you'll need to configure the [Service Manager](https://wiki.jasig.org/display/CASUM/Services+Management)

  
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