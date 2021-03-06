package org.goko.gcode.rs274ngcv3.xml.bean.modifier;

import org.goko.core.common.io.xml.quantity.XmlAngle;
import org.goko.core.common.io.xml.quantity.XmlLength;
import org.goko.gcode.rs274ngcv3.xml.bean.XmlGCodeModifier;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.DerivedType;

@DerivedType(parent=XmlGCodeModifier.class, name="modifier:translate")
public class XmlTranslateModifier extends XmlGCodeModifier {
	@Attribute
	private XmlLength x;
	@Attribute
	private XmlLength y;
	@Attribute
	private XmlLength z;
	/** A translation (not required for backward compatibility with 0.3.0 project files) */
	@Attribute(required=false)
	private XmlAngle a;
	/** B translation (not required for backward compatibility with 0.3.0 project files) */
	@Attribute(required=false)
	private XmlAngle b;
	/** C translation (not required for backward compatibility with 0.3.0 project files) */
	@Attribute(required=false)
	private XmlAngle c;

	/**
	 * @return the x
	 */
	public XmlLength getX() {
		return x;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(XmlLength x) {
		this.x = x;
	}
	/**
	 * @return the y
	 */
	public XmlLength getY() {
		return y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(XmlLength y) {
		this.y = y;
	}
	/**
	 * @return the z
	 */
	public XmlLength getZ() {
		return z;
	}
	/**
	 * @param z the z to set
	 */
	public void setZ(XmlLength z) {
		this.z = z;
	}
	/**
	 * @return the a
	 */
	public XmlAngle getA() {
		return a;
	}
	/**
	 * @param a the a to set
	 */
	public void setA(XmlAngle a) {
		this.a = a;
	}
	/**
	 * @return the b
	 */
	public XmlAngle getB() {
		return b;
	}
	/**
	 * @param b the b to set
	 */
	public void setB(XmlAngle b) {
		this.b = b;
	}
	/**
	 * @return the c
	 */
	public XmlAngle getC() {
		return c;
	}
	/**
	 * @param c the c to set
	 */
	public void setC(XmlAngle c) {
		this.c = c;
	}

}
