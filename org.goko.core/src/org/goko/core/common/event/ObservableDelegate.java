/**
 * 
 */
package org.goko.core.common.event;

/**
 * @author PsyKo
 * @date 25 f�vr. 2016
 */
public class ObservableDelegate<T> extends AbstractObservable<T> implements IObservableDelegate<T> {

	
	/**
	 * @param m_class
	 */
	public ObservableDelegate(Class<T> m_class) {
		super(m_class);
		// TODO Auto-generated constructor stub
	}

	/** (inheritDoc)
	 * @see org.goko.core.common.event.AbstractObservable#getEventDispatcher()
	 */
	@Override
	public T getEventDispatcher() {		
		return super.getEventDispatcher();
	}
}
