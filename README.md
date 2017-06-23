# Custom Create Account

Two Simple Liferay modules for override the CreateAccountMVCActionCommand to add user to relative Organization.

login-fragment is a OSGi Fragment Module to export private com.liferay.login.web package and a hook for create_account.jsp. 
liferay-challenge is a OSGi module with an activator for create Organizations and a Organization Custom Field on deploy, also implement CustomCreateAccountMVCActionCommand for override CustomCreateAccountMVCActionCommand.

In portlet.properties file are configurated two Organizzation (ITALY and IRELAND), the registration code is registration-italy and registration-ireland

### Technologies used
 - Liferay 7
 - Java 8
 - Gradle
 - OSGi

### Requirement

 - JDK 1.8
 - Liferay DXP SP14

### Build

```
cd liferay-challenge
./gradlew clean build

cd login-fragment
./gradlew clean build
```



License
----

MIT
