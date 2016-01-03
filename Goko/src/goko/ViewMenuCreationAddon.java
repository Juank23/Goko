package goko;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

@Creatable
public class ViewMenuCreationAddon implements EventHandler{
	public static final String VIEW_MENU_ENTRY_TAG = "view";
	public static final String VIEW_NAME_PARAMETER = "org.goko.commands.toggleView.viewName";
	@Inject
	private EPartService partService;
	@Inject
	private EModelService modelService;
	@Inject
	@Optional
	private MApplication application;
	@Inject
	private ECommandService commandService;

	@Override
	public void handleEvent(Event event) {
		List<MMenu> lstViewSubmenu = modelService.findElements(application, "goko.menu.window.view", MMenu.class, new ArrayList<String>(), EModelService.IN_MAIN_MENU);
		MMenu viewSubmenu = lstViewSubmenu.get(0);

		Collection<MPart> parts = partService.getParts();
		Iterator<MPart> iterator = parts.iterator();
		List<MMenuElement> children = new ArrayList<MMenuElement>();
		
		while (iterator.hasNext()) {
			MPart mPart = iterator.next();
			if(mPart.getTags().contains(VIEW_MENU_ENTRY_TAG)){

				MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem();
				item.setLabel(mPart.getLabel());
				item.setTooltip(mPart.getLabel());
				item.setIconURI(mPart.getIconURI());
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(VIEW_NAME_PARAMETER, mPart.getElementId());
				ParameterizedCommand command = commandService.createCommand("goko.command.toggleView", parameters);
				item.setWbCommand(command);
				children.add(item);
			}
		}
		Collections.sort(children, new MenuLabelComparator());
		viewSubmenu.getChildren().addAll(children);
		
	}
}

class MenuLabelComparator implements Comparator<MMenuElement>{

	/** (inheritDoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(MMenuElement arg0, MMenuElement arg1) {		
		return StringUtils.defaultString(arg0.getLabel()).compareTo(arg1.getLabel());
	}
	
}