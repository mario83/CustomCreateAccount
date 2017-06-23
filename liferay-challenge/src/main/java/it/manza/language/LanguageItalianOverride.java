package it.manza.language;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.language.UTF8Control;

@Component(
		immediate = true, 
		property = {
				"language.id=it_IT"
			}, 
		service = ResourceBundle.class
)
public class LanguageItalianOverride extends ResourceBundle {
	@Override
	protected Object handleGetObject(String key) {
		return _resourceBundle.getObject(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return _resourceBundle.getKeys();
	}

	private final ResourceBundle _resourceBundle = ResourceBundle.getBundle(
		"content.Language_it", Locale.ITALIAN, UTF8Control.INSTANCE);
}
