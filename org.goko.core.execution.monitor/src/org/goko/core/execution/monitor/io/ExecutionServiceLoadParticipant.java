/**
 * 
 */
package org.goko.core.execution.monitor.io;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.goko.core.common.exception.GkException;
import org.goko.core.common.io.xml.IXmlPersistenceService;
import org.goko.core.common.service.IGokoService;
import org.goko.core.execution.monitor.io.bean.XmlExecutionService;
import org.goko.core.execution.monitor.io.bean.XmlExecutionToken;
import org.goko.core.execution.monitor.service.ExecutionServiceImpl;
import org.goko.core.gcode.execution.ExecutionQueueType;
import org.goko.core.gcode.execution.ExecutionToken;
import org.goko.core.gcode.service.IGCodeProviderRepository;
import org.goko.core.log.GkLog;
import org.goko.core.workspace.io.IProjectLocation;
import org.goko.core.workspace.service.AbstractProjectLoadParticipant;
import org.goko.core.workspace.service.IMapperService;
import org.goko.core.workspace.service.IProjectLoadParticipant;

/**
 * @author PsyKo
 * @date 1 janv. 2016
 */
public class ExecutionServiceLoadParticipant extends AbstractProjectLoadParticipant<XmlExecutionService> implements IProjectLoadParticipant, IGokoService {
	/** LOG */
	private static final GkLog LOG = GkLog.getLogger(ExecutionServiceLoadParticipant.class);
	/** Service ID */
	private static final String SERVICE_ID = "org.goko.core.execution.monitor.io.ExecutionServiceLoadParticipant";
	/** XML persistence service */
	private IXmlPersistenceService xmlPersistenceService;
	/** Mapper service */
	private IMapperService mapperService;
	/** The target execution service */
	private ExecutionServiceImpl executionService;
	/** GCode provider repository */
	private IGCodeProviderRepository gcodeRepository;
	/** Load priority */
	private static final int LOAD_PRIORITY = 500;
		
	/**
	 * Constructor
	 */
	public ExecutionServiceLoadParticipant() {
		super(XmlExecutionService.class);
	}

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
		LOG.info("Starting  "+getServiceId());
		
		LOG.info("Successfully started "+getServiceId());
	}
	
	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#stop()
	 */
	@Override
	public void stop() throws GkException {
		// TODO Auto-generated method stub
		
	}
	/** (inheritDoc)
	 * @see org.goko.core.workspace.service.IProjectLoadParticipant#getPriority()
	 */
	@Override
	public int getPriority() {		
		return LOAD_PRIORITY;
	}
	
	/** (inheritDoc)
	 * @see org.goko.core.workspace.service.IProjectLoadParticipant#getContainerType()
	 */
	@Override
	public String getContainerType() {		
		return XmlExecutionService.CONTAINER_TYPE;
	}
	
	/** (inheritDoc)
	 * @see org.goko.core.workspace.service.IProjectLoadParticipant#clearContent()
	 */
	@Override
	public void clearContent() throws GkException {
		executionService.clearExecutionQueue(ExecutionQueueType.DEFAULT);
		executionService.clearExecutionQueue(ExecutionQueueType.SYSTEM);
	}
	
	
	@Override
	public void loadContainer(XmlExecutionService container, IProjectLocation input, IProgressMonitor monitor) throws GkException {
		ArrayList<XmlExecutionToken> lstToken = container.getLstExecutionToken();
		
		if(CollectionUtils.isNotEmpty(lstToken)){
			for (XmlExecutionToken xmlExecutionToken : lstToken) {
				ExecutionToken executionToken = mapperService.load(xmlExecutionToken, ExecutionToken.class);
				if(executionToken != null){
					executionService.addToExecutionQueue(executionToken);
				}
			}
		}
	}

	/**
	 * @return the xmlPersistenceService
	 */
	public IXmlPersistenceService getXmlPersistenceService() {
		return xmlPersistenceService;
	}

	/**
	 * @param xmlPersistenceService the xmlPersistenceService to set
	 */
	public void setXmlPersistenceService(IXmlPersistenceService xmlPersistenceService) {
		this.xmlPersistenceService = xmlPersistenceService;
	}

	/**
	 * @return the executionService
	 */
	public ExecutionServiceImpl getExecutionService() {
		return executionService;
	}

	/**
	 * @param executionService the executionService to set
	 */
	public void setExecutionService(ExecutionServiceImpl executionService) {
		this.executionService = executionService;
	}

	/**
	 * @return the gcodeRepository
	 */
	public IGCodeProviderRepository getGcodeRepository() {
		return gcodeRepository;
	}

	/**
	 * @param gcodeRepository the gcodeRepository to set
	 */
	public void setGcodeRepository(IGCodeProviderRepository gcodeRepository) {
		this.gcodeRepository = gcodeRepository;
	}

	/**
	 * @return the mapperService
	 */
	public IMapperService getMapperService() {
		return mapperService;
	}

	/**
	 * @param mapperService the mapperService to set
	 */
	public void setMapperService(IMapperService mapperService) {
		this.mapperService = mapperService;
	}
}
