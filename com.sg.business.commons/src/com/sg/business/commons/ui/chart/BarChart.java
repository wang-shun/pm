package com.sg.business.commons.ui.chart;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.Position;
//import org.eclipse.birt.chart.model.attribute.RiserType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;

public class BarChart extends AbstractChart {

	public static Chart getChart(String[] xAxisText, String[] bsText,
			double[][] bsValue,String subType,int shift) {
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setType("Bar Chart"); //$NON-NLS-1$
		chart.setSubType(subType); //$NON-NLS-1$
		chart.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
		chart.setSeriesThickness(2);

		// Plot
		chart.getBlock().setBackground(ColorDefinitionImpl.TRANSPARENT());
		Plot p = chart.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());

		// Title
		chart.getTitle().setVisible(false);

		// Legend
		chart.getLegend().getText().getFont().setSize(LEGEND_FONT_SIZE );
		chart.getLegend().setPosition(Position.BELOW_LITERAL);
		chart.getLegend().setOrientation(Orientation.HORIZONTAL_LITERAL);

		// X-Axis
		Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		xAxisPrimary.getLabel().getCaption().getFont().setSize(TICK_FONT_SIZE);
		xAxisPrimary.getLabel().getCaption()
				.setColor(ColorDefinitionImpl.create(0x9d, 0x9d, 0x9d));

		// Y-Axis
		Axis yAxisPrimary = chart.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.getLabel().getCaption().getFont().setSize(TICK_FONT_SIZE);
		yAxisPrimary.getLabel().getCaption()
				.setColor(ColorDefinitionImpl.create(0x9d, 0x9d, 0x9d));

		// Data Set
		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);
		TextDataSet categoryValues = TextDataSetImpl.create(xAxisText);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();

		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		// Y-Sereis
		//TODO 需要改变颜色
		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		setSeriesColor(sdY);
		sdY.getSeriesPalette( ).shift( shift );
		yAxisPrimary.getSeriesDefinitions().add(sdY);

		for (int i = 0; i < bsValue.length; i++) {
			NumberDataSet orthoValues = NumberDataSetImpl.create(bsValue[i]);
			OrthogonalSampleData sdOrthogonal = DataFactory.eINSTANCE
					.createOrthogonalSampleData();
			sdOrthogonal.setDataSetRepresentation("");//$NON-NLS-1$
			sdOrthogonal.setSeriesDefinitionIndex(0);
			sd.getOrthogonalSampleData().add(sdOrthogonal);

			BarSeries bs = (BarSeries) BarSeriesImpl.create();
			bs.setSeriesIdentifier(bsText[i]);
			bs.setDataSet(orthoValues);
			bs.getLabel().setVisible(false);
			if(subType == "Stacked"){
				bs.setStacked(true);
//			} else {
//				bs.setRiser( RiserType.TUBE_LITERAL );
			}
//			bs.getLabel().getCaption().getFont().setSize(FONT_SIZE);
			sdY.getSeries().add(bs);
		}
		chart.setSampleData(sd);
		return chart;
	}
	
}
