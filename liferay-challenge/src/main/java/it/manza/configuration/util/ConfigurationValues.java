
package it.manza.configuration.util;

import com.liferay.portal.kernel.util.StringPool;

public class ConfigurationValues {

	public static final String[] ORAGANIZATIONS =
		ConfigurationUtil.get(ConfigurationKeys.ORAGANIZATIONS).split(
			StringPool.COMMA);
}
