/**
 * 
 */
package org.goko.core.workspace.io;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author PsyKo
 * @date 9 d�c. 2015
 */
@Root(name="container")
public class XmlProjectContainer {
	/** The type of this container */
	@Attribute(name="type")
	private String type;
}
