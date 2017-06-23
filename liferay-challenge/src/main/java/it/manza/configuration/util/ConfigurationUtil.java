
package it.manza.configuration.util;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.configuration.ConfigurationFactoryUtil;
import com.liferay.portal.kernel.configuration.Filter;

public class ConfigurationUtil {

	public static String get(String key) {
		return _configuration.get(key);
	}

	public static String get(String key, Filter filter) {
		return _configuration.get(key, filter);
	}
	
	public static String getOrganizationName(String registrationCode){
		initOrganizations();
		return _organizations.entrySet().stream()
		  .filter(o -> o.getValue().equals(registrationCode))
		  .map(Map.Entry::getKey)
		  .findFirst()
		  .orElse(null);
	}
	
	public static String getRegistrationCode(String organizationName) {
		return get(
			ConfigurationKeys.ORAGANIZATION_REGISTRATION_CODE_PREFIX +
				organizationName +
				ConfigurationKeys.ORAGANIZATION_REGISTRATION_CODE_POSTFIX);
	}
	
	public static Collection<String> getRegistrationCodes(){
		initOrganizations();
		return _organizations.values();
	}
	
	public static void initOrganizations(){
		if (_organizations.isEmpty()) {
			synchronized (_organizations) {
				for (String organizzationName : ConfigurationValues.ORAGANIZATIONS) {
					_organizations.put(
						organizzationName,
						getRegistrationCode(organizzationName));
				}
			}
		}		
	}

	private static final Configuration _configuration =
		ConfigurationFactoryUtil.getConfiguration(
			ConfigurationUtil.class.getClassLoader(), "portlet");
	
	protected static URL getResourceURL(String resourceName) {
		Bundle bundle = FrameworkUtil.getBundle(ConfigurationUtil.class);
		URL entry = bundle.getEntry(resourceName);

		return entry;
	}
	
	private static final Map<String, String> _organizations = new HashMap<>();
}
