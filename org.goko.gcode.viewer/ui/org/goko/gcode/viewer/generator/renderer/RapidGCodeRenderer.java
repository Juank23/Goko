/*
 *
 *   Goko
 *   Copyright (C) 2013  PsyKo
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.goko.gcode.viewer.generator.renderer;

import java.math.BigDecimal;

import javax.media.opengl.GL2;
import javax.vecmath.Point3d;

import org.goko.core.gcode.bean.GCodeCommandState;
import org.goko.core.gcode.bean.GCodeContext;
import org.goko.gcode.rs274ngcv3.command.RapidLinearMotionGCodeCommand;
import org.goko.gcode.viewer.generator.AbstractGCodeGlRenderer;

public class RapidGCodeRenderer extends AbstractGCodeGlRenderer<RapidLinearMotionGCodeCommand> {

	private static Point3d G00_COLOR = new Point3d(1,0.27,0);


	/** (inheritDoc)
	 * @see org.goko.gcode.viewer.generator.AbstractGCodeGlRenderer#getRenderedCommandClass()
	 */
	@Override
	public Class<RapidLinearMotionGCodeCommand> getRenderedCommandClass() {
		return RapidLinearMotionGCodeCommand.class;
	}

	/** (inheritDoc)
	 * @see org.goko.gcode.viewer.generator.AbstractGCodeGlRenderer#render(org.goko.core.gcode.bean.GCodeCommand, javax.media.opengl.GL2)
	 */
	@Override
	public void render(GCodeContext context, RapidLinearMotionGCodeCommand command, GL2 gl) {
		gl.glEnable(GL2.GL_LINE_STIPPLE);
		gl.glLineStipple(4, (short)0xAAAA);

		gl.glBegin(GL2.GL_LINE_STRIP);

		// Let's redraw the current position with the accurate color
		if(command.getState().isState(GCodeCommandState.EXECUTED) || command.getState().isState(GCodeCommandState.SENT)){
			gl.glColor3d(0.4,0.4,0.4);
		}else{
			gl.glColor3d(G00_COLOR.x, G00_COLOR.y, G00_COLOR.z);
		}
		gl.glVertex3d(context.getPositionX().doubleValue(), context.getPositionY().doubleValue(), context.getPositionZ().doubleValue());

		Double x = context.getPositionX().doubleValue();
		Double y = context.getPositionY().doubleValue();
		Double z = context.getPositionZ().doubleValue();
		if(!context.isAbsolute()){
			x = add(x, command.getEndpointX());
			y = add(y, command.getEndpointY());
			z = add(z, command.getEndpointZ());
		}else{
			if(command.getEndpointX() != null){
				x = command.getEndpointX().doubleValue();
			}
			if(command.getEndpointY() != null){
				y = command.getEndpointY().doubleValue();
			}
			if(command.getEndpointZ() != null){
				z= command.getEndpointZ().doubleValue();
			}
		}
		if(command.getState().isState(GCodeCommandState.EXECUTED) || command.getState().isState(GCodeCommandState.SENT)){
			gl.glColor3d(0.4,0.4,0.4);
		}else{
			gl.glColor3d(G00_COLOR.x, G00_COLOR.y, G00_COLOR.z);
		}
		gl.glVertex3d(x, y, z);
		gl.glEnd();
		gl.glDisable(GL2.GL_LINE_STIPPLE);
		gl.glFlush();
	}

	protected double add(double position, BigDecimal delta){
		if( delta == null){
			return position;
		}
		return position + delta.doubleValue();
	}

}