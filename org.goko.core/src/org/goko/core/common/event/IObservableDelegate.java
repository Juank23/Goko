/**
 * 
 */
package org.goko.core.common.event;

/**
 * @author PsyKo
 * @date 25 f�vr. 2016
 */
public interface IObservableDelegate<T> extends IObservable<T>{

	T getEventDispatcher();
}
