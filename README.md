# Custom Create Account

Two Simple Liferay module for override CreateAccountMVCActionCommand to add user to relative Organization.

login-fragment is a OSGi Fragment Module to export private com.liferay.login.web package and create_account.jsp hook.
liferay-challenge is a OSGi module with an activator for create Organizations and a Organization Custom Field on deploy, also implement CustomCreateAccountMVCActionCommand for override CustomCreateAccountMVCActionCommand.

### Technologies used
 - Java 8
 - Gradle
 - OSGi

### Requirement

 - JDK 1.8
 - Liferay DXP SP 14

### Build

```
./liferay-challenge/gradlew clean build
./login-fragment/gradlew clean build
```



License
----

MIT
