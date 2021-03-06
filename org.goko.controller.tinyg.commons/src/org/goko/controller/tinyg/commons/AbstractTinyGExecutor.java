/**
 *
 */
package org.goko.controller.tinyg.commons;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.goko.core.common.exception.GkException;
import org.goko.core.controller.bean.MachineState;
import org.goko.core.execution.monitor.executor.AbstractStreamingExecutor;
import org.goko.core.gcode.element.GCodeLine;
import org.goko.core.gcode.element.IGCodeProvider;
import org.goko.core.gcode.execution.ExecutionToken;
import org.goko.core.gcode.execution.ExecutionTokenState;
import org.goko.core.gcode.execution.IExecutionToken;
import org.goko.core.gcode.execution.IExecutor;
import org.goko.core.gcode.service.IGCodeExecutionListener;
import org.goko.core.log.GkLog;

/**
 * TinyG executor implementation
 *
 * @author PsyKo
 * @date 20 nov. 2015
 */
public class AbstractTinyGExecutor<T extends ITinyGControllerService<?>> extends AbstractStreamingExecutor<ExecutionTokenState, IExecutionToken<ExecutionTokenState>> implements IExecutor<ExecutionTokenState, IExecutionToken<ExecutionTokenState>>, IGCodeExecutionListener<ExecutionTokenState, IExecutionToken<ExecutionTokenState>>{
	/** LOG */
	private static final GkLog LOG = GkLog.getLogger(AbstractTinyGExecutor.class);
	/** The number of command sent but not confirmed */
	private AtomicInteger pendingCommandCount;
	ConcurrentLinkedQueue<GCodeLine> queue;
	/** The underlying service */
	private T tinygService;
	/** Required space in TinyG planner buffer to send a new command */
	private int requiredBufferSpace = 5;

	/**
	 * Constructor
	 * @param tinygService the underlying TinyG service
	 */
	public AbstractTinyGExecutor(T tinygService) {
		super();
		this.tinygService = tinygService;		
		this.pendingCommandCount = new AtomicInteger(0);
		queue = new ConcurrentLinkedQueue<>();
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.execution.IExecutor#createToken(org.goko.core.gcode.element.IGCodeProvider)
	 */
	@Override
	public IExecutionToken<ExecutionTokenState> createToken(IGCodeProvider provider) throws GkException {
		return new ExecutionToken<ExecutionTokenState>(provider, ExecutionTokenState.NONE);
	}

	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#send(org.goko.core.gcode.element.GCodeLine)
	 */
	@Override
	protected void send(GCodeLine line) throws GkException {
		pendingCommandCount.incrementAndGet();
		queue.add(line);
		getToken().setLineState(line.getId(), ExecutionTokenState.SENT);
		tinygService.send(line);				
	}

	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#isReadyForNextLine()
	 */
	@Override
	protected boolean isReadyForNextLine() throws GkException {		
		int actuallyAvailableBuffer = tinygService.getAvailablePlannerBuffer() - pendingCommandCount.intValue();
		return ( !tinygService.isPlannerBufferCheck() || actuallyAvailableBuffer >= requiredBufferSpace);		
	} 

	/**
	 * Notification method when the available buffer space changed
	 * @param availableBufferSpace the available space buffer
	 * @throws GkException GkException
	 */
	public void onBufferSpaceAvailableChange(int availableBufferSpace) throws GkException{
		if(availableBufferSpace >= requiredBufferSpace && isReadyForNextLine()){
			notifyReadyForNextLineIfRequired();
		}
	}

	/**
	 * Notification method called when a line is confirmed by TinyG
	 * @throws GkException
	 */
	public void confirmNextLineExecution() throws GkException{
		List<GCodeLine> lstLines = getToken().getLineByState(ExecutionTokenState.SENT);

		if(CollectionUtils.isNotEmpty(lstLines)){
			GCodeLine line = lstLines.get(0);
			//System.out.println("Confirming line "+line.getId());
			confirmLineExecution(line);
		}
	}

	protected void confirmLineExecution(GCodeLine line) throws GkException{		
		pendingCommandCount.decrementAndGet();
		queue.poll();
		decrementRemainingCommandCount();
		
		getToken().setLineState(line.getId(), ExecutionTokenState.EXECUTED);
		getExecutionService().notifyCommandStateChanged(getToken(), line.getId());
		notifyReadyForNextLineIfRequired();
		notifyTokenCompleteIfRequired();
	}
	/**
	 * Notification method called when a line is throwing error by TinyG
	 * @throws GkException
	 */
	protected void markNextLineAsError() throws GkException{
		pendingCommandCount.decrementAndGet();
		List<GCodeLine> lstLines = getToken().getLineByState(ExecutionTokenState.SENT);
		GCodeLine line = lstLines.get(0);
		getToken().setLineState(line.getId(), ExecutionTokenState.ERROR);
		getExecutionService().notifyCommandStateChanged(getToken(), line.getId());
	}

	/**
	 * Notify the parent executor if the conditions are met
	 * @throws GkException GkException
	 */
	private void notifyTokenCompleteIfRequired() throws GkException {
		if(getToken().getLineCountByState(ExecutionTokenState.SENT) == 0 && pendingCommandCount.get() <= 0 && getRemainingCommands() <= 0){
			notifyTokenComplete();
		}
	}

	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#isTokenComplete()
	 */
	@Override
	public boolean isTokenComplete() throws GkException {
		return super.isTokenComplete() && (tinygService.getState() == MachineState.PROGRAM_END || tinygService.getState() == MachineState.PROGRAM_STOP); // Experimental
	}
	/**
	 * Handles any TinyG Status that is not TG_OK
	 * @param status the received status or <code>null</code> if unknown
	 * @throws GkException GkException
	 */
	public void handleNonOkStatus(ITinyGStatus status) throws GkException {
		if(status == null || status.isError()){
			LOG.warn("Pausing execution queue from TinyG Executor due to received status ["+status.getValue()+"]");
			getExecutionService().pauseQueueExecution();
			markNextLineAsError();
		}else{
			confirmNextLineExecution();
		}
	}

	/**
	 * Method used to confirm all command being marked as error.
	 * This is the result of the user continuing the execution after an error was reported.
	 * If the user continues, it meas the error is ignored.
	 * @throws GkException GkException
	 */
	protected void confirmErrorCommands() throws GkException{
		List<GCodeLine> lstErrorToken = getToken().getLineByState(ExecutionTokenState.ERROR);
		if(CollectionUtils.isNotEmpty(lstErrorToken)){
			for (GCodeLine line : lstErrorToken) {
				pendingCommandCount.decrementAndGet();
				getToken().setLineState(line.getId(), ExecutionTokenState.EXECUTED);
				getExecutionService().notifyCommandStateChanged(getToken(), line.getId());
				notifyReadyForNextLineIfRequired();
				notifyTokenCompleteIfRequired();
			}
		}
	}
	/**
	 * Notify the parent executor if the conditions are met
	 * @throws GkException GkException
	 */
	protected void notifyReadyForNextLineIfRequired() throws GkException{
		if(isReadyForNextLine()){
			notifyReadyForNextLine();
		}
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.execution.IExecutor#isReadyForQueueExecution()
	 */
	@Override
	public boolean isReadyForQueueExecution() throws GkException {
		tinygService.verifyReadyForExecution();
		return tinygService.isReadyForFileStreaming();
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onQueueExecutionStart()
	 */
	@Override
	public void onQueueExecutionStart() throws GkException {
		this.pendingCommandCount.set(0);
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onExecutionStart(org.goko.core.gcode.execution.IExecutionToken)
	 */
	@Override
	public void onExecutionStart(IExecutionToken<ExecutionTokenState> token) throws GkException {}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onExecutionCanceled(org.goko.core.gcode.execution.IExecutionToken)
	 */
	@Override
	public void onExecutionCanceled(IExecutionToken<ExecutionTokenState> token) throws GkException {}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onExecutionPause(org.goko.core.gcode.execution.IExecutionToken)
	 */
	@Override
	public void onExecutionPause(IExecutionToken<ExecutionTokenState> token) throws GkException {}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onExecutionResume(org.goko.core.gcode.execution.IExecutionToken)
	 */
	@Override
	public void onExecutionResume(IExecutionToken<ExecutionTokenState> token) throws GkException {
		// We confirm commands in error state:
		//   - if it's a non blocking warning, it's fine
		//   - if it's an error and the user continue the execution, it assumes the error can be ignored
		// 	 - if it's an error and the user stops the execution, this confirm never happens
		confirmErrorCommands();
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onExecutionComplete(org.goko.core.gcode.execution.IExecutionToken)
	 */
	@Override
	public void onExecutionComplete(IExecutionToken<ExecutionTokenState> token) throws GkException {	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onQueueExecutionComplete()
	 */
	@Override
	public void onQueueExecutionComplete() throws GkException {}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeTokenExecutionListener#onQueueExecutionCanceled()
	 */
	@Override
	public void onQueueExecutionCanceled() throws GkException {
		this.pendingCommandCount.set(0);
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.service.IGCodeLineExecutionListener#onLineStateChanged(org.goko.core.gcode.execution.IExecutionToken, java.lang.Integer)
	 */
	@Override
	public void onLineStateChanged(IExecutionToken<ExecutionTokenState> token, Integer idLine) throws GkException { }
	
	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#stop()
	 */
	@Override
	public void stop() throws GkException {		
		super.stop();	
		tinygService.stopMotion();
	}
	
	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#pause()
	 */
	@Override
	public void pause() throws GkException {		
		super.pause();
		tinygService.pauseMotion();
	}
	
	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#start()
	 */
	@Override
	public void start() throws GkException {		
		super.start();
		tinygService.start();
	}
	
	/** (inheritDoc)
	 * @see org.goko.core.execution.monitor.executor.AbstractStreamingExecutor#resume()
	 */
	@Override
	public void resume() throws GkException {		
		super.resume();
		tinygService.resumeMotion();
	}

	/**
	 * @return the requiredBufferSpace
	 */
	public int getRequiredBufferSpace() {
		return requiredBufferSpace;
	}

	/**
	 * @param requiredBufferSpace the requiredBufferSpace to set
	 */
	public void setRequiredBufferSpace(int requiredBufferSpace) {
		this.requiredBufferSpace = requiredBufferSpace;
	}

}
