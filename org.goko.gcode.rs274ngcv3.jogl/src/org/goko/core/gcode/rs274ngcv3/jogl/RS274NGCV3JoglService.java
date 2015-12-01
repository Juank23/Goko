package org.goko.core.gcode.rs274ngcv3.jogl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.goko.core.common.exception.GkException;
import org.goko.core.common.exception.GkTechnicalException;
import org.goko.core.common.service.IGokoService;
import org.goko.core.common.utils.CacheById;
import org.goko.core.common.utils.SequentialIdGenerator;
import org.goko.core.gcode.element.IGCodeProvider;
import org.goko.core.gcode.execution.ExecutionToken;
import org.goko.core.gcode.execution.ExecutionTokenState;
import org.goko.core.gcode.rs274ngcv3.IRS274NGCService;
import org.goko.core.gcode.rs274ngcv3.context.GCodeContext;
import org.goko.core.gcode.rs274ngcv3.element.GCodeProvider;
import org.goko.core.gcode.rs274ngcv3.element.IModifier;
import org.goko.core.gcode.rs274ngcv3.element.InstructionProvider;
import org.goko.core.gcode.rs274ngcv3.event.RS274WorkspaceDeleteEvent;
import org.goko.core.gcode.rs274ngcv3.event.RS274WorkspaceEvent;
import org.goko.core.gcode.rs274ngcv3.jogl.internal.Activator;
import org.goko.core.gcode.rs274ngcv3.jogl.renderer.RS274GCodeRenderer;
import org.goko.core.gcode.service.IExecutionService;
import org.goko.core.log.GkLog;
import org.goko.core.math.BoundingTuple6b;
import org.goko.core.workspace.service.IWorkspaceEvent;
import org.goko.core.workspace.service.IWorkspaceListener;
import org.goko.core.workspace.service.IWorkspaceService;
import org.goko.tools.viewer.jogl.utils.render.basic.BoundsRenderer;

public class RS274NGCV3JoglService implements IGokoService, IWorkspaceListener{
	/** LOG */
	private static final GkLog LOG = GkLog.getLogger(RS274NGCV3JoglService.class);
	/** ID of the service */
	private static final String SERVICE_ID = "org.goko.core.gcode.rs274ngcv3.jogl.RS274NGCV3JoglService";
	/** The list of managed renderer */
	private CacheById<RS274GCodeRenderer> cacheRenderer;
	/** The ID generator for the renderers */
	private SequentialIdGenerator rendererIdSequence;
	/** The workspace service */
	private IWorkspaceService workspaceService;
	/** The RS274 GCode service */
	private IRS274NGCService rs274Service;
	/** The bounds of all the loaded gcode */
	private BoundsRenderer contentBoundsRenderer;
	/** Execution service */
	private IExecutionService<ExecutionTokenState, ExecutionToken<ExecutionTokenState>> executionService;
	
	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#getServiceId()
	 */
	@Override
	public String getServiceId() throws GkException {
		return SERVICE_ID;
	}

	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#start()
	 */
	@Override
	public void start() throws GkException {
		LOG.info("Starting "+getServiceId());
		
		this.cacheRenderer = new CacheById<RS274GCodeRenderer>(new SequentialIdGenerator());
		this.rendererIdSequence = new SequentialIdGenerator();
		LOG.info("Successfully started " + getServiceId());
	}

	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#stop()
	 */
	@Override
	public void stop() throws GkException {
		LOG.info("Stopping "+getServiceId());
		LOG.info("Successfully stopped " + getServiceId());
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.service.IWorkspaceListener#onWorkspaceEvent(org.goko.core.workspace.service.IWorkspaceEvent)
	 */
	@Override
	public void onWorkspaceEvent(IWorkspaceEvent event) throws GkException {		
		if(event.isType(RS274WorkspaceEvent.TYPE)){
			RS274WorkspaceEvent rsEvent = (RS274WorkspaceEvent) event;
			if(rsEvent.getContentType() == RS274WorkspaceEvent.GCODE_MODIFIER_EVENT){
				onModifierEvent(event);
			}else if(rsEvent.getContentType() == RS274WorkspaceEvent.GCODE_PROVIDER_EVENT){
				onProviderEvent(event);
			}
		}
	}	

	protected void onModifierEvent(IWorkspaceEvent event) throws GkException {		
		if(event.isAction(IWorkspaceEvent.ACTION_DELETE)){
			RS274WorkspaceDeleteEvent deleteEvent = (RS274WorkspaceDeleteEvent) event;
			IModifier<?> modifier = (IModifier<?>) deleteEvent.getDeletedObject();
			updateRenderer(modifier.getIdGCodeProvider());
		}else{
			IModifier<GCodeProvider> modifier = rs274Service.getModifier(event.getIdElement());					
			updateRenderer(modifier.getIdGCodeProvider());
		}
		updateContentBounds();
		
	}
	protected void onProviderEvent(IWorkspaceEvent event) throws GkException {		
		if(event.isAction(IWorkspaceEvent.ACTION_CREATE)){
			createRenderer(event.getIdElement());
			updateContentBounds();			
		}else if(event.isAction(IWorkspaceEvent.ACTION_DELETE)){
			removeRenderer(event.getIdElement());
			updateContentBounds();
		}else if(event.isAction(IWorkspaceEvent.ACTION_UPDATE)){
			updateRenderer(event.getIdElement());
			updateContentBounds();
			
		}
	}
	private void updateContentBounds() throws GkException {
		List<RS274GCodeRenderer> lstRenderer = cacheRenderer.get();
		
		if(contentBoundsRenderer != null){
			Activator.getJoglViewerService().removeRenderer(contentBoundsRenderer);
		}
		
		if(CollectionUtils.isNotEmpty(lstRenderer)){
			BoundingTuple6b result = null;
			for (RS274GCodeRenderer renderer : lstRenderer) {
				IGCodeProvider provider = Activator.getRS274NGCService().getGCodeProvider(renderer.getIdGCodeProvider());
				InstructionProvider instructionProvider = Activator.getRS274NGCService().getInstructions(new GCodeContext(), provider);
				BoundingTuple6b bounds = Activator.getRS274NGCService().getBounds(new GCodeContext(), instructionProvider);
				renderer.setBounds(bounds);
				if(result == null){
					result = bounds;
				}else{
					result.add(bounds);
				}
			}
			
			contentBoundsRenderer = new BoundsRenderer(result);			
			Activator.getJoglViewerService().addRenderer(contentBoundsRenderer);
		}
	}

	/**
	 * Creates the renderer for the given GCodeProvider
	 * @param idGCodeProvider the id of the GCodeProvider
	 * @throws GkException GkException
	 */
	public void createRenderer(Integer idGCodeProvider) throws GkException{		
		getRS274NGCService().getGCodeProvider(idGCodeProvider);
		RS274GCodeRenderer renderer = new RS274GCodeRenderer(idGCodeProvider);		
		renderer.setIdGCodeProvider(idGCodeProvider);
		executionService.addExecutionListener(renderer);
		this.cacheRenderer.add(renderer);
		Activator.getJoglViewerService().addRenderer(renderer);		
	}
	
	public void updateRenderer(Integer idGCodeProvider) throws GkException{		
		RS274GCodeRenderer renderer = getRendererByGCodeProvider(idGCodeProvider);				
//		createRenderer(idGCodeProvider);
//		cacheRenderer.remove(renderer); a modifier
//		renderer.destroy();		
		renderer.updateGeometry();
	}
	
	/**
	 * Removes the renderer for the given GCodeProvider
	 * @param idGCodeProvider the id of the GCodeProvider
	 * @throws GkException GkException
	 */
	public void removeRenderer(Integer idGCodeProvider) throws GkException{
		RS274GCodeRenderer renderer = getRendererByGCodeProvider(idGCodeProvider);
		executionService.removeExecutionListener(renderer);
		cacheRenderer.remove(renderer);
		renderer.destroy();
	}
	
	/**
	 * Returns the renderer for the given gcodeProvider
	 * @param idGCodeProvider the id of the gcode provider
	 * @return an RS274GCodeRenderer
	 * @throws GkException GkException
	 */
	public RS274GCodeRenderer getRendererByGCodeProvider(Integer idGCodeProvider) throws GkException{
		for (RS274GCodeRenderer renderer : cacheRenderer.get()) {
			if(ObjectUtils.equals(idGCodeProvider, renderer.getIdGCodeProvider())){
				return renderer;
			}
		}
		throw new GkTechnicalException("Renderer for GCodeProvider with internal id ["+idGCodeProvider+"] does not exist");
	}

	/**
	 * @return the workspaceService
	 */
	public IWorkspaceService getWorkspaceService() {		
		return workspaceService;
	}

	/**
	 * @param workspaceService the workspaceService to set
	 * @throws GkException GkException 
	 */
	public void setWorkspaceService(IWorkspaceService workspaceService) throws GkException {
		if(this.workspaceService != null){
			this.workspaceService.removeWorkspaceListener(this);
		}
		this.workspaceService = workspaceService;
		if(this.workspaceService != null){
			this.workspaceService.addWorkspaceListener(this);
		}
	}
	
	/**
	 * @param service the IRS274NGCService to set
	 */
	public void setRS274NGCService(IRS274NGCService service){
		this.rs274Service = service;
	}
	
	/**
	 * Returns the current IRS274NGCService
	 * @return IRS274NGCService
	 */
	public IRS274NGCService getRS274NGCService(){
		return this.rs274Service;
	}

	/**
	 * @return the executionService
	 */
	public IExecutionService<ExecutionTokenState, ExecutionToken<ExecutionTokenState>> getExecutionService() {
		return executionService;
	}

	/**
	 * @param executionService the executionService to set
	 */
	public void setExecutionService(IExecutionService<ExecutionTokenState, ExecutionToken<ExecutionTokenState>> executionService) {
		this.executionService = executionService;
	}
}