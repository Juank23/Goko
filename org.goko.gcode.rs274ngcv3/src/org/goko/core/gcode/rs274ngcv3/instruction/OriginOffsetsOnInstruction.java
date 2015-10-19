package org.goko.core.gcode.rs274ngcv3.instruction;

import org.goko.core.gcode.rs274ngcv3.element.InstructionType;

public class OriginOffsetsOnInstruction extends AbstractInstruction{
	/** Constructor */
	public OriginOffsetsOnInstruction() {
		super(InstructionType.ORIGIN_OFFSETS_ON);
	}

//	/** (inheritDoc)
//	 * @see org.goko.core.gcode.element.IInstruction#apply(org.goko.core.gcode.rs274ngcv3.context.GCodeContext)
//	 */
//	@Override
//	public void apply(GCodeContext context) throws GkException {
//		context.setOriginOffsetActive(true);		
//	}

}