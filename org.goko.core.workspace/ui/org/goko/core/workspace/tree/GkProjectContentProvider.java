package org.goko.core.workspace.tree;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.goko.core.common.exception.GkException;
import org.goko.core.log.GkLog;
import org.goko.core.workspace.bean.GkProject;
import org.goko.core.workspace.bean.ProjectContainer;
import org.goko.core.workspace.bean.ProjectContainerUiProvider;
import org.goko.core.workspace.internal.Activator;

public class GkProjectContentProvider implements ITreeContentProvider {
	/** LOG */
	private static final GkLog LOG = GkLog.getLogger(GkProjectContentProvider.class);
	
	public GkProjectContentProvider() {		
	}
	
	/** (inheritDoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		try{
			List<ProjectContainerUiProvider> uiProviders = Activator.getWorkspaceUIService().getProjectContainerUiProvider();
			for (ProjectContainerUiProvider uiProvider : uiProviders) {			
				try {
					uiProvider.getContentProvider().dispose();					
				} catch (GkException e) {
					LOG.error(e);
				}
			}
		}catch(GkException e){
			LOG.error(e);
		}
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		List<ProjectContainer> containers = ((GkProject)inputElement).getProjectContainer();
		return containers.toArray();
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ProjectContainer){
			try {
				ProjectContainer container = (ProjectContainer) parentElement;
				ProjectContainerUiProvider uiProvider = Activator.getWorkspaceUIService().findProjectContainerUiProvider(container.getType());
				if(uiProvider != null){
					return uiProvider.getContentProvider().getChildren(parentElement);				
				}
			} catch (GkException e) {					
				LOG.error(e);
			}
		}
		return null;
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if(element instanceof ProjectContainer){
			try {
				ProjectContainer container = (ProjectContainer) element;
				ProjectContainerUiProvider uiProvider = Activator.getWorkspaceUIService().findProjectContainerUiProvider(container.getType());
				if(uiProvider != null){
					return uiProvider.getContentProvider().getParent(element);				
				}
			} catch (GkException e) {					
				LOG.error(e);
			}
		}
		return null;
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof ProjectContainer){
			try {
				ProjectContainer container = (ProjectContainer) element;
				ProjectContainerUiProvider uiProvider = Activator.getWorkspaceUIService().findProjectContainerUiProvider(container.getType());
				if(uiProvider != null){
					return uiProvider.getContentProvider().hasChildren(element);				
				}
			} catch (GkException e) {					
				LOG.error(e);
			}
		}	
		return false;
	}

}
