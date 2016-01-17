package org.goko.autoleveler.bean.grid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.goko.autoleveler.bean.AbstractOffsetMapProbeBuilder;
import org.goko.autoleveler.bean.IHeightMap;
import org.goko.autoleveler.bean.IHeightMapProbeBuilder;
import org.goko.core.common.exception.GkException;
import org.goko.core.common.measure.Units;
import org.goko.core.common.measure.quantity.Length;
import org.goko.core.common.measure.quantity.LengthUnit;
import org.goko.core.math.Tuple6b;

public class GridHeightMapBuilder extends AbstractOffsetMapProbeBuilder implements IHeightMapProbeBuilder {
	/** The generated grid offset map */
	private GridHeightMap gridOffsetMap;
	/** The spatial repartition of positions */
	private int[][] vertices;
	/** The indexed list of position */
	private List<Tuple6b> probedPositions;

	/** The start point of this map */
	private Tuple6b start;
	/** The end point of this map */
	private Tuple6b end;
	/** The X axis step size */
	private Length stepSizeX;
	/** The Y axis step size */
	private Length stepSizeY;

	public GridHeightMapBuilder() {
		this.start = new Tuple6b();
		this.end   = new Tuple6b();
		this.stepSizeX = Length.valueOf(2, LengthUnit.INCH);
		this.stepSizeY = Length.ZERO;		
		setClearanceHeight(Length.ZERO);
		setProbeFeedrate(Length.ZERO);
		setProbeLowerHeight(Length.ZERO);
		setProbeStartHeight(Length.ZERO);
	}
	/** (inheritDoc)
	 * @see org.goko.autoleveler.bean.IHeightMapBuilder#getMap()
	 */
	@Override
	public IHeightMap getMap() throws GkException {
		if(gridOffsetMap == null){
			buildMap();
		}
		return gridOffsetMap;
	}


	private void buildMap() {
//		Tuple6b lStart = new Tuple6b();
//		Tuple6b lEnd   = new Tuple6b();
//		lStart.min(start, end);
//		lEnd.min(start, end);
		List<Tuple6b> lOffsets = new ArrayList<>();
		lOffsets.add( new Tuple6b(new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("5"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("15"), new BigDecimal("0"), new BigDecimal("5"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("30"), new BigDecimal("0"), new BigDecimal("8"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("0"), new BigDecimal("15"), new BigDecimal("1"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("15"), new BigDecimal("15"), new BigDecimal("1"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("30"), new BigDecimal("15"), new BigDecimal("4"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("0"), new BigDecimal("30"), new BigDecimal("10"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("15"), new BigDecimal("30"), new BigDecimal("10"), Units.MILLIMETRE));
		lOffsets.add( new Tuple6b(new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("14"), Units.MILLIMETRE));

		int[][] lVertices = new int[][]{{0,3,6},{1,4,7},{2,5,8}};
		gridOffsetMap = new GridHeightMap(lVertices, lOffsets);
	}

	/** (inheritDoc)
	 * @see org.goko.autoleveler.bean.IHeightMapProbeBuilder#getProbePositions()
	 */
	@Override
	public List<Tuple6b> getProbePositions() {
		// TODO Auto-generated method stub
		return null;
	}


	/** (inheritDoc)
	 * @see org.goko.autoleveler.bean.IHeightMapProbeBuilder#registerProbePosition(org.goko.core.math.Tuple6b)
	 */
	@Override
	public void registerProbePosition(Tuple6b probedPosition) {
		// TODO Auto-generated method stub

	}


	/**
	 * @return the start
	 */
	public Tuple6b getStart() {
		return start;
	}


	/**
	 * @param start the start to set
	 */
	public void setStart(Tuple6b start) {
		this.start = start;
	}


	/**
	 * @return the end
	 */
	public Tuple6b getEnd() {
		return end;
	}


	/**
	 * @param end the end to set
	 */
	public void setEnd(Tuple6b end) {
		this.end = end;
	}


	/**
	 * @return the stepSizeX
	 */
	public Length getStepSizeX() {
		return stepSizeX;
	}


	/**
	 * @param stepSizeX the stepSizeX to set
	 */
	public void setStepSizeX(Length stepSizeX) {
		this.stepSizeX = stepSizeX;
	}


	/**
	 * @return the stepSizeY
	 */
	public Length getStepSizeY() {
		return stepSizeY;
	}


	/**
	 * @param stepSizeY the stepSizeY to set
	 */
	public void setStepSizeY(Length stepSizeY) {
		this.stepSizeY = stepSizeY;
	}
}
