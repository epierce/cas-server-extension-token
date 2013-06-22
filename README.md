# Token Authentication Module for Jasig CAS Server

This module allows you to authenticate and pass attributes for a user with a AES128-encrypted token instead of a password.   

## How is the token generated?
The token is a AES-128 encrypted JSON object:

```
{   
    "generated":    1338575644294,
    "username":    "epierce",
    "firstname":   "Eric",
    "lastname":    "Pierce",
    "email":       "epierce@mail.usf.edu"
}
```

The _generated_ field is the timestamp in milliseconds.  This value is compared against the system time to verify the age of the token.  The _username_ is also compared to the _username_ request value to ensure this token belongs to this user.

To encrypt the token with Java or PHP, use [PHP-Java-AES-Encrypt](https://github.com/stevenholder/PHP-Java-AES-Encrypt)

Use the Maven Overlay Method for [configuring CAS](https://wiki.jasig.org/display/CASUM/Best+Practice+-+Setting+Up+CAS+Locally+using+the+Maven2+WAR+Overlay+Method)

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
Add the following block to your `pom.xml`

```
<dependency>
  <groupId>edu.usf.cims</groupId>
  <artifactId>cas-server-extension-token</artifactId>
  <version>0.2</version>
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
      p:encryptionKey="1234567891234567" 
      p:maxDrift="120" />
  </list>
 </property>
```    
    
* **encryptionKey**: Encryption key that is shared with the program generating the token (**MUST** be 16 characters)
    
* **maxDrift**: Number of seconds to allow for clock drift when validating the timestamp of the token.

You'll also need to add `TokenCredentialsToPrincipalResolver` to the list of principal resolvers:

```
<property name="credentialsToPrincipalResolvers">
  <list>
    <bean class="edu.usf.cims.cas.support.token.authentication.principal.TokenCredentialsToPrincipalResolver" />  
    <bean class="org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver" />
  </list>
</property>
```
        
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