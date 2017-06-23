package it.manza.login.action;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.login.web.constants.LoginPortletKeys;
import com.liferay.login.web.internal.portlet.action.CreateAccountMVCActionCommand;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.session.AuthenticatedSessionManager;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.util.PropsValues;

import it.manza.configuration.util.ConfigurationUtil;

@Component(
	property = {
		"javax.portlet.name=" + LoginPortletKeys.FAST_LOGIN,
		"javax.portlet.name=" + LoginPortletKeys.LOGIN,
		"mvc.command.name=/login/create_account",
		"service.ranking:Integer=100"
	},
	service = MVCActionCommand.class
)
public class CustomCreateAccountMVCActionCommand
	extends CreateAccountMVCActionCommand {
	
	@Override
	protected void addUser(
		ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		try {
			String registrationCode =
				ParamUtil.getString(actionRequest, "registration-code");
			_log.info("registrationCode "+registrationCode);
			
			Collection<String> registrationCodes =
				ConfigurationUtil.getRegistrationCodes();
			
			if(registrationCodes.contains(registrationCode)){
				HttpServletRequest request = _portal.getHttpServletRequest(
					actionRequest);

				HttpSession session = request.getSession();

				ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

				Company company = themeDisplay.getCompany();

				// get organizzation id
				long organizationId =
					OrganizationLocalServiceUtil.getOrganizationId(
						company.getCompanyId(),
						ConfigurationUtil.getOrganizationName(
							registrationCode));
				
				_log.info("organizationId "+ organizationId);
				
				boolean autoPassword = true;
				String password1 = null;
				String password2 = null;
				boolean autoScreenName = isAutoScreenName();
				String screenName = ParamUtil.getString(actionRequest, "screenName");
				String emailAddress = ParamUtil.getString(
					actionRequest, "emailAddress");
				long facebookId = ParamUtil.getLong(actionRequest, "facebookId");
				String openId = ParamUtil.getString(actionRequest, "openId");
				String languageId = ParamUtil.getString(actionRequest, "languageId");
				String firstName = ParamUtil.getString(actionRequest, "firstName");
				String middleName = ParamUtil.getString(actionRequest, "middleName");
				String lastName = ParamUtil.getString(actionRequest, "lastName");
				long prefixId = ParamUtil.getInteger(actionRequest, "prefixId");
				long suffixId = ParamUtil.getInteger(actionRequest, "suffixId");
				boolean male = ParamUtil.getBoolean(actionRequest, "male", true);
				int birthdayMonth = ParamUtil.getInteger(
					actionRequest, "birthdayMonth");
				int birthdayDay = ParamUtil.getInteger(actionRequest, "birthdayDay");
				int birthdayYear = ParamUtil.getInteger(actionRequest, "birthdayYear");
				String jobTitle = ParamUtil.getString(actionRequest, "jobTitle");
				long[] groupIds = null;
				long[] organizationIds = null;
				long[] roleIds = null;
				long[] userGroupIds = null;
				boolean sendEmail = true;

				ServiceContext serviceContext = ServiceContextFactory.getInstance(
					User.class.getName(), actionRequest);

				if (PropsValues.LOGIN_CREATE_ACCOUNT_ALLOW_CUSTOM_PASSWORD) {
					autoPassword = false;

					password1 = ParamUtil.getString(actionRequest, "password1");
					password2 = ParamUtil.getString(actionRequest, "password2");
				}

				boolean openIdPending = false;

				Boolean openIdLoginPending = (Boolean)session.getAttribute(
					WebKeys.OPEN_ID_LOGIN_PENDING);

				if ((openIdLoginPending != null) && openIdLoginPending.booleanValue() &&
					Validator.isNotNull(openId)) {

					sendEmail = false;
					openIdPending = true;
				}

				User user = _userService.addUserWithWorkflow(
					company.getCompanyId(), autoPassword, password1, password2,
					autoScreenName, screenName, emailAddress, facebookId, openId,
					LocaleUtil.fromLanguageId(languageId), firstName, middleName,
					lastName, prefixId, suffixId, male, birthdayMonth, birthdayDay,
					birthdayYear, jobTitle, groupIds, organizationIds, roleIds,
					userGroupIds, sendEmail, serviceContext);

				if (openIdPending) {
					session.setAttribute(
						WebKeys.OPEN_ID_LOGIN, Long.valueOf(user.getUserId()));

					session.removeAttribute(WebKeys.OPEN_ID_LOGIN_PENDING);
				}
				else {

					// Session messages

					if (user.getStatus() == WorkflowConstants.STATUS_APPROVED) {
						SessionMessages.add(
							request, "userAdded", user.getEmailAddress());
						SessionMessages.add(
							request, "userAddedPassword",
							user.getPasswordUnencrypted());
					}
					else {
						SessionMessages.add(
							request, "userPending", user.getEmailAddress());
					}
				}
				
				// Add user to organizzation
				_userLocalService.addOrganizationUser(organizationId, user.getUserId());

				// Send redirect

				sendRedirect(
					actionRequest, actionResponse, themeDisplay, user,
					user.getPasswordUnencrypted());
			} else {
				SessionErrors.add(actionRequest, "RegistrationCodeInvalid");
			}
		}
		catch (Exception e) {
			_log.error(e);
			throw e;
		}

	}

	@Reference(unbind = "-")
	protected void setLayoutLocalService(
		LayoutLocalService layoutLocalService) {
		super.setLayoutLocalService(layoutLocalService);
	}

	@Reference(unbind = "-")
	protected void setUserLocalService(UserLocalService userLocalService) {
		super.setUserLocalService(userLocalService);
		_userLocalService = userLocalService;
	}

	@Reference(unbind = "-")
	protected void setUserService(UserService userService) {
		super.setUserService(userService);
		_userService = userService;
	}

	@Reference(unbind = "-")
	protected void setAuthenticatedSessionManager(AuthenticatedSessionManager sessionMgr) {
		update("_authenticatedSessionManager", sessionMgr);
	}
	
	@Reference(unbind = "-")
	protected void setPortal(Portal portal) {
		update("_portal", portal);
		_portal = portal;
	}

	protected void update(final String fieldName, final Object value) {
		try {
			Field f = getClass().getSuperclass().getDeclaredField(fieldName);

			f.setAccessible(true);

			f.set(this, value);
		} catch (IllegalAccessException e) {
			_log.error("Error updating " + fieldName, e);
		} catch (NoSuchFieldException e) {
			_log.error("Error updating " + fieldName, e);
		}
	}
	
	private Portal _portal;
	private UserLocalService _userLocalService;
	private UserService _userService;
	
	private static final Log _log =
			LogFactoryUtil.getLog(CustomCreateAccountMVCActionCommand.class);
}
