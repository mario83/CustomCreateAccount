package it.manza.activator;

import java.util.Arrays;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.model.ExpandoTableConstants;
import com.liferay.expando.kernel.service.ExpandoColumnLocalServiceUtil;
import com.liferay.expando.kernel.service.ExpandoTableLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ListTypeConstants;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

import it.manza.configuration.util.ConfigurationUtil;
import it.manza.configuration.util.ConfigurationValues;

public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		_log.info("start LiferayChallengeActivator");
		
		_log.info("check custom field");		
		checkCustomField();
		_log.info("check organizzation");		
		checkOrganizzation();
	}


	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}
	
	private void addExpandoField(
		long companyId, String name, int type, long tableId)
		throws PortalException {
		
		ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(
			tableId, name);
		
		if (Validator.isNull(column)) {
			column = ExpandoColumnLocalServiceUtil.addColumn(
				tableId, name, type);
			
			column.setTypeSettings("visible-with-update-permission=true");

			ExpandoColumnLocalServiceUtil.updateExpandoColumn(column);

			_log.info("Custom field " + name + " created");
		} else {
			_log.info("Custom field " + name + " already existed");
		}
	}
	
	private void checkCustomField() throws PortalException{
		
		long companyId = PortalUtil.getDefaultCompanyId();
		
		ExpandoTable table = ExpandoTableLocalServiceUtil.fetchDefaultTable(
			companyId, Organization.class.getName());

		if (table == null) {
			table = ExpandoTableLocalServiceUtil.addTable(
				companyId, Organization.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME);
		}

		addExpandoField(
			companyId, "RegistrationCode",
			ExpandoColumnConstants.STRING, table.getTableId());
	}
	
	private void checkOrganizzation()
		throws PortalException {

		long companyId = PortalUtil.getDefaultCompanyId();

		long userId = UserLocalServiceUtil.getDefaultUserId(companyId);

		Arrays.stream(ConfigurationValues.ORAGANIZATIONS).forEach(name -> {
			Organization fetchOrganization =
				OrganizationLocalServiceUtil.fetchOrganization(companyId, name);
			if (Validator.isNull(fetchOrganization)) {
				try {
					Organization organization =
						OrganizationLocalServiceUtil.addOrganization(
							userId,
							OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID,
							name, OrganizationConstants.TYPE_ORGANIZATION, 0, 0,
							ListTypeConstants.ORGANIZATION_STATUS_DEFAULT,
							StringPool.BLANK, true, null);
					
					ExpandoBridge expandoBridge =
						organization.getExpandoBridge();
					expandoBridge.setAttribute(
						"RegistrationCode",
						ConfigurationUtil.getRegistrationCode(name), false);
					OrganizationLocalServiceUtil.updateOrganization(
						organization);
					
					_log.info("Organization " + name + " created");
				}
				catch (PortalException e) {
					_log.error(e);
				}
			} else {
				_log.info("Organization " + name + " already existed");
			}
		});
	}

	private static final Log _log = LogFactoryUtil.getLog(Activator.class);
}