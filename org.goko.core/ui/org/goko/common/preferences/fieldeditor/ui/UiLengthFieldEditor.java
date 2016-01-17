/**
 * 
 */
package org.goko.common.preferences.fieldeditor.ui;

import org.eclipse.swt.widgets.Composite;
import org.goko.common.preferences.fieldeditor.ui.converter.StringToLengthConverter;
import org.goko.common.preferences.fieldeditor.ui.converter.StringToQuantityConverter;
import org.goko.core.common.measure.quantity.Length;

/**
 * @author PsyKo
 * @date 15 janv. 2016
 */
public class UiLengthFieldEditor extends UiQuantityFieldEditor<Length> {

	/**
	 * @param parent
	 * @param style
	 */
	public UiLengthFieldEditor(Composite parent, int style) {
		super(parent, style);
	}

	/** (inheritDoc)
	 * @see org.goko.common.preferences.fieldeditor.ui.UiQuantityFieldEditor#getQuantityConverter()
	 */
	@Override
	protected StringToQuantityConverter<Length> getQuantityConverter() {
		return new StringToLengthConverter(this);
	}

}
