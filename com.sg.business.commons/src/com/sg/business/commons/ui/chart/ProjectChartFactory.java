package com.sg.business.commons.ui.chart;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.DialChart;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.Fill;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineDecorator;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.Text;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.GradientImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Dial;
import org.eclipse.birt.chart.model.component.DialRegion;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.AxisImpl;
import org.eclipse.birt.chart.model.component.impl.DialRegionImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.impl.DialChartImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.layout.TitleBlock;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.DialSeries;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.DialSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

import com.mobnut.commons.util.Utils;
import com.mongodb.DBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Project;
import com.sg.business.model.ProjectProvider;
import com.sg.business.model.SalesData;
import com.sg.business.resource.nls.Messages;

public class ProjectChartFactory {

	private static final int STRONG_SIZE = 9;

	private static final int NORMAL_SIZE = 9;

	private static final int SMALL_SIZE = 9;

	// 正常完成
	private static final ColorDefinition color1 = getRGBColorDefinition(Utils.COLOR_GREEN[10]);

	// "超期完成"
	private static final ColorDefinition color2 = getRGBColorDefinition("#00a99d"); //$NON-NLS-1$

	// "进度延迟"
	private static final ColorDefinition color3 = getRGBColorDefinition(Utils.COLOR_RED[5]);

	// "预期延迟"
	private static final ColorDefinition color4 = getRGBColorDefinition(Utils.COLOR_YELLOW[5]);

	// 正常进行
	private static final ColorDefinition color5 = getRGBColorDefinition(Utils.COLOR_BLUE[5]);

	private static final Fill[] fiaBase = { color1, color2, color3, color4,
			color5 };

	public static Chart createPieChart(String pieChartCaption, String[] texts,
			double[] values) {
		// double maxValue = Utils.max(values);
		ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
		chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
		// chart.setMinSlice(maxValue / 50);// 最大的十分之一
		// chart.setMinSliceLabel("其他");
		// chart.setMinSlicePercent(true);
		Text caption = chart.getTitle().getLabel().getCaption();
		caption.setValue(pieChartCaption);
		adjustFont(caption.getFont(), STRONG_SIZE);
		Legend legend = chart.getLegend();
		legend.setItemType(LegendItemType.CATEGORIES_LITERAL);
		legend.setVisible(true);
		adjustFont(legend.getText().getFont(), NORMAL_SIZE);
		TextDataSet categoryValues = TextDataSetImpl.create(texts);//$NON-NLS-1$ //$NON-NLS-2$
		NumberDataSet seriesOneValues = NumberDataSetImpl.create(values);
		// Base Series
		Series series = SeriesImpl.create();
		series.setDataSet(categoryValues);
		SeriesDefinition sd = SeriesDefinitionImpl.create();
		chart.getSeriesDefinitions().add(sd);
		sd.getSeriesPalette().shift(0);
		sd.getSeries().add(series);
		// new colors
		sd.getSeriesPalette().getEntries().clear();
		for (int i = 0; i < fiaBase.length; i++) {
			sd.getSeriesPalette().getEntries().add(fiaBase[i]);
		}

		// Orthogonal Series
		PieSeries sePie = (PieSeries) PieSeriesImpl.create();
		sePie.setDataSet(seriesOneValues);
		sePie.setExplosion(2);
		sePie.setRotation(40);

		sePie.setLabelPosition(Position.INSIDE_LITERAL);// 设置在内部显示数字
		adjustFont(sePie.getLabel().getCaption().getFont(), NORMAL_SIZE);// 设置字体

		SeriesDefinition sdef = SeriesDefinitionImpl.create();
		sd.getSeriesDefinitions().add(sdef);
		sdef.getSeries().add(sePie);

		return chart;
	}

	public static Chart createStackedBarChart(String title,
			String[] deptParameter, double[] deptValue1, String seriesTitle) {
		ChartWithAxes cwaBar = ChartWithAxesImpl.create();
		cwaBar.setType("Bar Chart"); //$NON-NLS-1$
		cwaBar.setSubType("Stacked"); //$NON-NLS-1$
		cwaBar.getBlock().setBackground(ColorDefinitionImpl.TRANSPARENT());
		cwaBar.getBlock().getOutline().setVisible(false);
		Plot p = cwaBar.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());

		// Title
		cwaBar.getTitle().getLabel().getCaption().setValue(title); //$NON-NLS-1$
		adjustFont(cwaBar.getTitle().getLabel().getCaption().getFont(),
				STRONG_SIZE);
		Legend lg = cwaBar.getLegend();
		lg.setItemType(LegendItemType.SERIES_LITERAL);
		adjustFont(lg.getText().getFont(), NORMAL_SIZE);
		// X-Axis
		Axis xAxisPrimary = cwaBar.getPrimaryBaseAxes()[0];

		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		FontDefinition font = xAxisPrimary.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);
		font.setRotation(-30);

		// Y-Axis
		Axis yAxisPrimary = cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		font = yAxisPrimary.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);

		// 取数
		TextDataSet categoryValues = TextDataSetImpl.create(deptParameter);
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(deptValue1);

		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);

		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);

		// 绑定
		cwaBar.setSampleData(sd);

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);

		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		// Y-Series
		BarSeries bs1 = (BarSeries) BarSeriesImpl.create();
		bs1.setDataSet(orthoValues1);
		bs1.setStacked(true);
		bs1.getLabel().setVisible(true);
		if (seriesTitle == null) {
		} else {
			bs1.setSeriesIdentifier(seriesTitle);
		}
		font = bs1.getLabel().getCaption().getFont();
		adjustFont(font, SMALL_SIZE);
		bs1.setLabelPosition(Position.INSIDE_LITERAL);

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(bs1);

		return cwaBar;
	}

	public static Chart create2StackedBarChart(String title,
			String[] deptParameter, double[] deptValue1, double[] deptValue2,
			String[] seriesTitle) {
		ChartWithAxes cwaBar = ChartWithAxesImpl.create();
		cwaBar.setType("Bar Chart"); //$NON-NLS-1$
		cwaBar.setSubType("Stacked"); //$NON-NLS-1$
		cwaBar.getBlock().setBackground(ColorDefinitionImpl.TRANSPARENT());
		cwaBar.getBlock().getOutline().setVisible(false);
		Plot p = cwaBar.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());

		// Title
		cwaBar.getTitle().getLabel().getCaption().setValue(title); //$NON-NLS-1$
		adjustFont(cwaBar.getTitle().getLabel().getCaption().getFont(),
				STRONG_SIZE);
		Legend lg = cwaBar.getLegend();
		lg.setItemType(LegendItemType.SERIES_LITERAL);
		adjustFont(lg.getText().getFont(), NORMAL_SIZE);
		// X-Axis
		Axis xAxisPrimary = cwaBar.getPrimaryBaseAxes()[0];

		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		FontDefinition font = xAxisPrimary.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);
		font.setRotation(-30);

		// Y-Axis
		Axis yAxisPrimary = cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		font = yAxisPrimary.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);

		// 取数
		TextDataSet categoryValues = TextDataSetImpl.create(deptParameter);
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(deptValue1);
		NumberDataSet orthoValues2 = NumberDataSetImpl.create(deptValue2);

		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);

		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);

		OrthogonalSampleData sdOrthogonal2 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal2.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal2.setSeriesDefinitionIndex(1);
		sd.getOrthogonalSampleData().add(sdOrthogonal2);

		// 绑定
		cwaBar.setSampleData(sd);

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);

		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		// Y-Series
		BarSeries bs1 = (BarSeries) BarSeriesImpl.create();
		bs1.setDataSet(orthoValues1);
		bs1.setSeriesIdentifier(seriesTitle[0]);
		bs1.setStacked(true);
		bs1.getLabel().setVisible(true);
		font = bs1.getLabel().getCaption().getFont();
		adjustFont(font, SMALL_SIZE);
		bs1.setLabelPosition(Position.INSIDE_LITERAL);

		BarSeries bs2 = (BarSeries) BarSeriesImpl.create();
		bs2.setDataSet(orthoValues2);
		bs2.setSeriesIdentifier(seriesTitle[1]);
		bs2.setStacked(true);
		bs2.getLabel().setVisible(true);
		font = bs2.getLabel().getCaption().getFont();
		adjustFont(font, SMALL_SIZE);
		bs2.setLabelPosition(Position.INSIDE_LITERAL);

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		// sdY.getSeriesPalette().getEntries().clear();
		// // "预期延迟"
		// ColorDefinition color1 =
		// getRGBColorDefinition(Utils.COLOR_YELLOW[5]);
		// // 正常进行
		// ColorDefinition color2 = getRGBColorDefinition(Utils.COLOR_BLUE[5]);
		// final Fill[] fiaBase = { color1, color2 };
		// for (int i = 0; i < fiaBase.length; i++) {
		// sdY.getSeriesPalette().getEntries().add(fiaBase[i]);
		// }
		// sdY.getSeriesPalette().shift(0);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(bs1);
		sdY.getSeries().add(bs2);

		return cwaBar;
	}

	private static Chart createLineChart(String title, String[] deptParameter,
			double[] deptValue1, double[] deptValue2, String[] seriesTitle) {
		ChartWithAxes cwaLine = ChartWithAxesImpl.create();
		cwaLine.setType("Line Chart"); //$NON-NLS-1$
		cwaLine.setSubType("Overlay"); //$NON-NLS-1$

		// Plot
		cwaLine.getBlock().setBackground(ColorDefinitionImpl.TRANSPARENT());
		Plot p = cwaLine.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());

		// Title
		cwaLine.getTitle().getLabel().getCaption().setValue(title);//$NON-NLS-1$
		FontDefinition font = cwaLine.getTitle().getLabel().getCaption()
				.getFont();
		adjustFont(font, STRONG_SIZE);
		// Legend
		Legend lg = cwaLine.getLegend();
		LineAttributes lia = lg.getOutline();
		font = lg.getText().getFont();
		adjustFont(font, SMALL_SIZE);
		lia.setStyle(LineStyle.SOLID_LITERAL);
		lg.getInsets().setLeft(10);
		lg.getInsets().setRight(10);

		// X-Axis
		Axis xAxisPrimary = cwaLine.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		font = xAxisPrimary.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);
		// Y-Axis
		Axis yAxisPrimary = cwaLine.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		font = yAxisPrimary.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(deptParameter);
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(deptValue1);
		NumberDataSet orthoValues2 = NumberDataSetImpl.create(deptValue2);
		// NumberDataSet orthoValues3 = NumberDataSetImpl.create( deptValue3);

		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);

		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);

		OrthogonalSampleData sdOrthogonal2 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal2.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal2.setSeriesDefinitionIndex(1);
		sd.getOrthogonalSampleData().add(sdOrthogonal2);

		// OrthogonalSampleData sdOrthogonal3 =
		// DataFactory.eINSTANCE.createOrthogonalSampleData( );
		//		sdOrthogonal3.setDataSetRepresentation( "" );//$NON-NLS-1$
		// sdOrthogonal3.setSeriesDefinitionIndex( 2 );
		// sd.getOrthogonalSampleData( ).add( sdOrthogonal3 );

		cwaLine.setSampleData(sd);

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();

		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		// Y-Sereis
		LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
		ls1.setDataSet(orthoValues1);
		ls1.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls1.getMarkers().size(); i++) {
			((Marker) ls1.getMarkers().get(i))
					.setType(MarkerType.CIRCLE_LITERAL);
		}
		ls1.getLabel().setVisible(true);
		ls1.setSeriesIdentifier(seriesTitle[0]);
		font = ls1.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);

		LineSeries ls2 = (LineSeries) LineSeriesImpl.create();
		ls2.setDataSet(orthoValues2);
		ls2.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls2.getMarkers().size(); i++) {
			((Marker) ls2.getMarkers().get(i))
					.setType(MarkerType.TRIANGLE_LITERAL);
		}
		ls2.getLabel().setVisible(true);
		ls2.setSeriesIdentifier(seriesTitle[1]);
		font = ls2.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);

		// LineSeries ls3 = (LineSeries) LineSeriesImpl.create( );
		// ls3.setDataSet( orthoValues3 );
		// ls3.getLineAttributes( ).setColor( ColorDefinitionImpl.CREAM( ) );
		// for ( int i = 0; i < ls3.getMarkers( ).size( ); i++ )
		// {
		// ( (Marker) ls3.getMarkers( ).get( i ) ).setType(
		// MarkerType.TRIANGLE_LITERAL );
		// }
		// ls3.getLabel( ).setVisible( true );
		// font = ls3.getLabel().getCaption().getFont();
		// adjustFont(font, NORMAL_SIZE);

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-2);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(ls1);
		sdY.getSeries().add(ls2);
		// sdY.getSeries( ).add( ls3 );

		return cwaLine;
	}

	public static Chart createCombinnationStackedBarChart(String title,
			String[] deptParameter, double[] deptValue1, double[] deptValue2,
			double[] deptValue3, String[] seriesTitle) {
		ChartWithAxes cwaBar = ChartWithAxesImpl.create();
		cwaBar.setType("Bar Chart"); //$NON-NLS-1$
		cwaBar.setSubType("Side-by-side"); //$NON-NLS-1$

		// Plot
		cwaBar.getBlock().setBackground(ColorDefinitionImpl.TRANSPARENT());
		Plot p = cwaBar.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());

		// Legend
		Legend lg = cwaBar.getLegend();
		LineAttributes lia = lg.getOutline();
		FontDefinition font = lg.getText().getFont();
		adjustFont(font, SMALL_SIZE);
		lia.setStyle(LineStyle.SOLID_LITERAL);
		lg.getInsets().setLeft(10);
		lg.getInsets().setRight(10);

		// Title
		cwaBar.getTitle().getLabel().getCaption().setValue(title);//$NON-NLS-1$
		font = cwaBar.getTitle().getLabel().getCaption().getFont();
		adjustFont(font, STRONG_SIZE);

		// X-Axis
		Axis xAxisPrimary = cwaBar.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
		xAxisPrimary.getTitle().getCaption().setValue("Category Text X-Axis");//$NON-NLS-1$
		xAxisPrimary.setTitlePosition(Position.BELOW_LITERAL);
		font = xAxisPrimary.getLabel().getCaption().getFont();
		font.setRotation(-45);
		adjustFont(font, NORMAL_SIZE);
		xAxisPrimary.setLabelPosition(Position.BELOW_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);

		// Y-Axis
		Axis yAxisPrimary1 = cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary1.getLabel().getCaption().setValue("Price Axis1");//$NON-NLS-1$
		font = yAxisPrimary1.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);
		yAxisPrimary1.setLabelPosition(Position.LEFT_LITERAL);
		yAxisPrimary1.setTitlePosition(Position.LEFT_LITERAL);
		yAxisPrimary1.getTitle().getCaption().setValue("Linear Value Y-Axis1");//$NON-NLS-1$
		yAxisPrimary1.setType(AxisType.LINEAR_LITERAL);
		yAxisPrimary1.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);

		Axis yAxisPrimary2 = AxisImpl.create(Axis.ORTHOGONAL);
		yAxisPrimary2.getLabel().getCaption().setValue("Price Axis2");//$NON-NLS-1$
		font = yAxisPrimary2.getLabel().getCaption().getFont();
		adjustFont(font, NORMAL_SIZE);
		yAxisPrimary2.setLabelPosition(Position.RIGHT_LITERAL);
		yAxisPrimary2.setTitlePosition(Position.RIGHT_LITERAL);
		yAxisPrimary2.getTitle().getCaption().setValue("Linear Value Y-Axis1");//$NON-NLS-1$
		yAxisPrimary2.setType(AxisType.LINEAR_LITERAL);
		yAxisPrimary2.getMajorGrid().setTickStyle(TickStyle.RIGHT_LITERAL);
		xAxisPrimary.getAssociatedAxes().add(yAxisPrimary2);

		// Associate with Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(deptParameter);
		NumberDataSet seriesValues1 = NumberDataSetImpl.create(deptValue1);
		NumberDataSet seriesValues2 = NumberDataSetImpl.create(deptValue2);
		NumberDataSet seriesValues3 = NumberDataSetImpl.create(deptValue3);

		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);

		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);

		OrthogonalSampleData sdOrthogonal2 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal2.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal2.setSeriesDefinitionIndex(1);
		sd.getOrthogonalSampleData().add(sdOrthogonal2);

		cwaBar.setSampleData(sd);

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);

		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		// Y-Series (1)
		BarSeries bs1 = (BarSeries) BarSeriesImpl.create();
		bs1.setDataSet(seriesValues1);
		bs1.setSeriesIdentifier(seriesTitle[0]);//$NON-NLS-1$
		bs1.setStacked(true);
		bs1.getLabel().setVisible(true);
		font = bs1.getLabel().getCaption().getFont();
		adjustFont(font, SMALL_SIZE);
		bs1.setLabelPosition(Position.INSIDE_LITERAL);

		BarSeries bs2 = (BarSeries) BarSeriesImpl.create();
		bs2.setDataSet(seriesValues2);
		bs2.setSeriesIdentifier(seriesTitle[1]);
		bs2.setStacked(true);
		bs2.getLabel().setVisible(true);
		font = bs2.getLabel().getCaption().getFont();
		adjustFont(font, SMALL_SIZE);
		bs2.setLabelPosition(Position.INSIDE_LITERAL);

		// Y-Series (2)
		LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
		ls1.setSeriesIdentifier(seriesTitle[2]);//$NON-NLS-1$
		ls1.setDataSet(seriesValues3);
		ls1.getLineAttributes().setColor(ColorDefinitionImpl.GREEN());
		for (int i = 0; i < ls1.getMarkers().size(); i++) {
			((Marker) ls1.getMarkers().get(i))
					.setType(MarkerType.TRIANGLE_LITERAL);
		}

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		yAxisPrimary1.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(bs1);
		sdY.getSeries().add(bs2);
		SeriesDefinition sdY2 = SeriesDefinitionImpl.create();
		yAxisPrimary2.getSeriesDefinitions().add(sdY2);
		sdY2.getSeries().add(ls1);
		sdY2.getSeriesPalette().shift(-5);

		return cwaBar;
	}

	public static Chart createMeterChart(String chartCaptionText, String label,
			double value) {
		String[] oValues = new String[] { label };

		DialChart chart = (DialChart) DialChartImpl.create();
		chart.setType("Standard Meter"); //$NON-NLS-1$  

		chart.setDialSuperimposition(false);

		// Title/Plot
		chart.getBlock().setBackground(ColorDefinitionImpl.TRANSPARENT());
		chart.getPlot().getClientArea().setVisible(false);
		chart.setCoverage(1);

		TitleBlock title = chart.getTitle();
		title.getOutline().setVisible(false);
		Text caption = title.getLabel().getCaption();
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		caption.setValue(chartCaptionText + (nf.format(value)) + "%");//$NON-NLS-1$
		adjustFont(caption.getFont(), STRONG_SIZE);

		// Legend
		Legend legend = chart.getLegend();
		legend.setVisible(false);

		TextDataSet categoryValues = TextDataSetImpl.create(oValues);//$NON-NLS-1$

		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData base = DataFactory.eINSTANCE.createBaseSampleData();
		base.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(base);

		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);

		chart.setSampleData(sd);

		SeriesDefinition sdBase = SeriesDefinitionImpl.create();
		chart.getSeriesDefinitions().add(sdBase);
		Series seCategory = (Series) SeriesImpl.create();

		seCategory.setDataSet(categoryValues);
		sdBase.getSeries().add(seCategory);

		SeriesDefinition sdCity = SeriesDefinitionImpl.create();

		// Dial 1
		DialSeries seDial1 = (DialSeries) DialSeriesImpl.create();
		seDial1.setDataSet(NumberDataSetImpl.create(new double[] { value }));
		seDial1.setSeriesIdentifier("超期率");//$NON-NLS-1$
		seDial1.getNeedle().setDecorator(LineDecorator.ARROW_LITERAL);

		Dial dial = seDial1.getDial();
		dial.setFill(GradientImpl.create(getRGBColorDefinition("#b5b5b5"), //$NON-NLS-1$
				getRGBColorDefinition("#ffffff"), -90, false)); //$NON-NLS-1$

		dial.setStartAngle(0);
		dial.setStopAngle(180);
		dial.getMinorGrid().getTickAttributes().setVisible(false);
		dial.getLabel().setVisible(false);
		seDial1.getDial().getMajorGrid().getTickAttributes()
				.setColor(ColorDefinitionImpl.BLACK());
		seDial1.getDial().getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		dial.getScale().setMin(NumberDataElementImpl.create(0));
		dial.getScale().setMax(NumberDataElementImpl.create(100));
		dial.getScale().setStep(10);

		DialRegion dregion1 = DialRegionImpl.create();
		dregion1.setFill(GradientImpl.create(
				getRGBColorDefinition(Utils.COLOR_RED[0]),
				getRGBColorDefinition(Utils.COLOR_RED[10]), 45, false));
		dregion1.setOutline(LineAttributesImpl.create(ColorDefinitionImpl
				.BLACK().darker(), LineStyle.SOLID_LITERAL, 1));
		dregion1.setStartValue(NumberDataElementImpl.create(70));
		dregion1.setEndValue(NumberDataElementImpl.create(100));
		dial.getDialRegions().add(dregion1);

		DialRegion dregion2 = DialRegionImpl.create();
		dregion2.setFill(GradientImpl.create(
				getRGBColorDefinition(Utils.COLOR_YELLOW[0]),
				getRGBColorDefinition(Utils.COLOR_YELLOW[10]), 45, true));
		dregion2.setOutline(LineAttributesImpl.create(ColorDefinitionImpl
				.BLACK().darker(), LineStyle.SOLID_LITERAL, 1));
		dregion2.setStartValue(NumberDataElementImpl.create(30));
		dregion2.setEndValue(NumberDataElementImpl.create(70));
		dial.getDialRegions().add(dregion2);

		DialRegion dregion3 = DialRegionImpl.create();
		dregion3.setFill(GradientImpl.create(
				getRGBColorDefinition(Utils.COLOR_GREEN[0]),
				getRGBColorDefinition(Utils.COLOR_GREEN[10]), 90, true));
		dregion3.setOutline(LineAttributesImpl.create(ColorDefinitionImpl
				.BLACK().darker(), LineStyle.SOLID_LITERAL, 1));
		dregion3.setStartValue(NumberDataElementImpl.create(0));
		dregion3.setEndValue(NumberDataElementImpl.create(30));
		dial.getDialRegions().add(dregion3);

		//
		sdBase.getSeriesDefinitions().add(sdCity);
		sdCity.getSeries().add(seDial1);

		return chart;
	}

	private static void adjustFont(FontDefinition font, int size) {
		font.setSize(size);
		font.setName("微软雅黑");//$NON-NLS-1$
	}

	private static ColorDefinition getRGBColorDefinition(String colorCode) {
		int[] rgb = Utils.getRGB(colorCode);// "正常完成"
		return ColorDefinitionImpl.create(rgb[0], rgb[1], rgb[2]);
	}

	public static Chart getSchedualStatusPieChart(ProjectProvider data) {
		// "正常完成"
		int value1 = data.sum.finished_normal;
		// "超期完成",
		int value2 = data.sum.finished_delay;
		// "提前完成",
		int value3 = data.sum.finished_advance;
		// "进度延迟",
		int value4 = data.sum.processing_delay;
		// "正常进行"
		int value5 = data.sum.processing_normal;
		// 进度提前
		int value6 = data.sum.processing_advance;

		String pieChartCaption = Messages.get().ProjectChartFactory_4;
		String[] texts = new String[] { Messages.get().ProjectChartFactory_5, Messages.get().ProjectChartFactory_6, Messages.get().ProjectChartFactory_7, Messages.get().ProjectChartFactory_8, Messages.get().ProjectChartFactory_9 };
		double[] values = new double[] { (value1 + value3), value2, value4,
				value5, value6 };
		return createPieChart(pieChartCaption, texts, values);
	}

	public static Chart getFinishedProjectSchedualMeter(ProjectProvider data) {
		// "正常完成"
		int value1 = data.sum.finished_normal;
		// "超期完成",
		int value2 = data.sum.finished_delay;
		// "提前完成",
		int value3 = data.sum.finished_advance;
		int sum = value1 + value2 + value3;
		double finishProjectOverTimeRate = sum == 0 ? 0 : (100d * value2 / sum);

		return createMeterChart(Messages.get().ProjectChartFactory_10, Messages.get().ProjectChartFactory_11, finishProjectOverTimeRate);
	}

	public static Chart getProcessProjectSchedualMeterChart(ProjectProvider data) {
		// "进度延迟",
		int value4 = data.sum.processing_delay;
		// "正常进行"
		int value5 = data.sum.processing_normal;
		// 进度提前
		int value6 = data.sum.processing_advance;

		int sum = value4 + value5 + value6;
		double processProjectOverTimeRate = sum == 0 ? 0
				: (100d * value4 / sum);

		return createMeterChart(Messages.get().ProjectChartFactory_12, Messages.get().ProjectChartFactory_13, processProjectOverTimeRate);
	}

	public static Chart getProjectSchedualMeter(ProjectProvider data) {
		// "正常完成"
		int value1 = data.sum.finished_normal;
		// "超期完成",
		int value2 = data.sum.finished_delay;
		// "提前完成",
		int value3 = data.sum.finished_advance;
		// "进度延迟",
		int value4 = data.sum.processing_delay;
		// "正常进行"
		int value5 = data.sum.processing_normal;
		// 进度提前
		int value6 = data.sum.processing_advance;
		int sum = value1 + value2 + value3 + value4 + value5 + value6;
		double allProjectOverTimeRate = sum == 0 ? 0
				: (100d * (value2 + value4) / sum);
		return createMeterChart(Messages.get().ProjectChartFactory_14, Messages.get().ProjectChartFactory_15, allProjectOverTimeRate);
	}

	public static Chart getDeptSchedualBar(ProjectProvider data) {
		String[] deptParameter = new String[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue1 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue2 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		for (int i = 0; i < deptParameter.length; i++) {
			ProjectProvider projectProvider = data.sum.subOrganizationProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			deptParameter[i] = projectProvider.getDesc();
			deptValue1[i] = projectProvider.sum.processing_normal
					+ projectProvider.sum.processing_advance;
			deptValue2[i] = projectProvider.sum.processing_delay;
		}

		return create2StackedBarChart(Messages.get().ProjectChartFactory_16, deptParameter, deptValue1,
				deptValue2, new String[] { Messages.get().ProjectChartFactory_17, Messages.get().ProjectChartFactory_18 });
	}

	public static Chart getDeptCombinationSchedualBar(ProjectProvider data) {
		String[] deptParameter = new String[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue1 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue2 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue3 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		for (int i = 0; i < deptParameter.length; i++) {
			ProjectProvider projectProvider = data.sum.subOrganizationProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			deptParameter[i] = projectProvider.getDesc();
			deptValue1[i] = projectProvider.sum.processing_normal
					+ projectProvider.sum.processing_advance;
			deptValue2[i] = projectProvider.sum.processing_delay;
			deptValue3[i] = projectProvider.sum.total_sales_revenue
					/ 10000
//					- (projectProvider.sum.total_sales_cost + projectProvider.sum.total_investment_amount)
					-projectProvider.sum.total_sales_cost/ 10000;
		}

		return createCombinnationStackedBarChart(Messages.get().ProjectChartFactory_19, deptParameter,
				deptValue1, deptValue2, deptValue3, new String[] { Messages.get().ProjectChartFactory_20,
						Messages.get().ProjectChartFactory_21, Messages.get().ProjectChartFactory_22 });
	}

	public static Chart getChargerSchedualBar(ProjectProvider data) {
		String[] chargerName = new String[data.sum.subChargerProjectProvider
				.size()];
		double[] userValue1 = new double[data.sum.subChargerProjectProvider
				.size()];
		double[] userValue2 = new double[data.sum.subChargerProjectProvider
				.size()];
		for (int i = 0; i < chargerName.length; i++) {
			ProjectProvider projectProvider = data.sum.subChargerProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			chargerName[i] = projectProvider.getDesc();
			userValue1[i] = projectProvider.sum.processing_normal
					+ projectProvider.sum.processing_advance;
			userValue2[i] = projectProvider.sum.processing_delay;
		}

		return create2StackedBarChart(Messages.get().ProjectChartFactory_23, chargerName, userValue1,
				userValue2, new String[] { Messages.get().ProjectChartFactory_24, Messages.get().ProjectChartFactory_25 });
	}

	public static Chart getFinishedProjectBudgetAndCostMeter(
			ProjectProvider data) {
		// "预算内完成"
		int value1 = data.sum.finished_cost_normal;
		// "超预算完成"
		int value2 = data.sum.finished_cost_over;
		int sum = value1 + value2;
		// 已经完成的项目的超支比例
		double finishedProjectOverCostRate = sum == 0 ? 0
				: (100d * value2 / sum);

		return createMeterChart(Messages.get().ProjectChartFactory_26, Messages.get().ProjectChartFactory_27,
				finishedProjectOverCostRate);
	}

	public static Chart getProjectBudgetAndCostPie(ProjectProvider data) {
		// "预算内完成"
		int value1 = data.sum.finished_cost_normal;
		// "超预算完成"
		int value2 = data.sum.finished_cost_over;
		// "超支风险"
		int value3 = data.sum.processing_cost_over;
		// "正常运行"
		int value4 = data.sum.processing_cost_normal;

		String pieChartCaption = Messages.get().ProjectChartFactory_28;
		String[] texts = new String[] { Messages.get().ProjectChartFactory_29, Messages.get().ProjectChartFactory_30, Messages.get().ProjectChartFactory_31, Messages.get().ProjectChartFactory_32 };
		double[] values = new double[] { value1, value2, value3, value4 };
		return createPieChart(pieChartCaption, texts, values);
	}

	public static Chart getProcessProjectBudgetAndCostMeter(ProjectProvider data) {
		// "超支风险"
		int value3 = data.sum.processing_cost_over;
		// "正常运行"
		int value4 = data.sum.processing_cost_normal;
		// 进行中项目的超支风险
		int sum = value4 + value3;
		double processProjectOverTimeRate = sum == 0 ? 0
				: (100d * value3 / sum);

		return createMeterChart(Messages.get().ProjectChartFactory_33, Messages.get().ProjectChartFactory_34,
				processProjectOverTimeRate);
	}

	public static Chart getDeptBudgetAndCostBar(ProjectProvider data) {
		String[] deptParameter = new String[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue1 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue2 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		for (int i = 0; i < deptParameter.length; i++) {
			ProjectProvider projectProvider = data.sum.subOrganizationProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			deptParameter[i] = projectProvider.getDesc();
			deptValue1[i] = projectProvider.sum.total_budget_amount / 10000;
			deptValue2[i] = projectProvider.sum.total_investment_amount / 10000;
		}
		return create2StackedBarChart(Messages.get().ProjectChartFactory_35, deptParameter, deptValue1,
				deptValue2, new String[] { Messages.get().ProjectChartFactory_36, Messages.get().ProjectChartFactory_37 });
	}

	public static Chart getChargerBudgetAndCostBar(ProjectProvider data) {
		String[] chargerName = new String[data.sum.subChargerProjectProvider
				.size()];
		double[] userValue1 = new double[data.sum.subChargerProjectProvider
				.size()];
		double[] userValue2 = new double[data.sum.subChargerProjectProvider
				.size()];
		for (int i = 0; i < chargerName.length; i++) {
			ProjectProvider projectProvider = data.sum.subChargerProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			chargerName[i] = projectProvider.getDesc();
			userValue1[i] = projectProvider.sum.total_budget_amount / 10000;
			userValue2[i] = projectProvider.sum.total_investment_amount / 10000;
		}
		return create2StackedBarChart(Messages.get().ProjectChartFactory_38, chargerName, userValue1,
				userValue2, new String[] { Messages.get().ProjectChartFactory_39, Messages.get().ProjectChartFactory_40 });
	}

	public static Chart getCostAndProfitBar(ProjectProvider data) {
		// "销售收入"
		long value1 = data.sum.total_sales_revenue;
		// "销售成本
		long value2 = data.sum.total_sales_cost;
//				+ data.sum.total_investment_amount;

		// "利润"
		long value3 = value1 - value2;

		String pieChartCaption = Messages.get().ProjectChartFactory_41;
		String[] texts = new String[] { Messages.get().ProjectChartFactory_42, Messages.get().ProjectChartFactory_43 };
		double[] values = new double[] { value2 / 10000, value3 / 10000 };
		return createStackedBarChart(pieChartCaption, texts, values, null);
	}

	public static Chart getProfitRateMeter(ProjectProvider data) {
		// "销售收入"
		long value1 = data.sum.total_sales_revenue;
		// "销售成本
		long value2 = data.sum.total_sales_cost;
//				+ data.sum.total_investment_amount;
		// "利润"
		long value3 = value1 - value2;
		// 利润率
		double profit = value1 == 0 ? 0 : 100d * value3 / value1;

		return createMeterChart(Messages.get().ProjectChartFactory_44, Messages.get().ProjectChartFactory_45, profit);
	}

	public static Chart getDeptRevenueBar(ProjectProvider data) {
		String[] deptParameter = new String[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue1 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		double[] deptValue2 = new double[data.sum.subOrganizationProjectProvider
				.size()];
		for (int i = 0; i < deptParameter.length; i++) {
			ProjectProvider projectProvider = data.sum.subOrganizationProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			deptParameter[i] = projectProvider.getDesc();
			deptValue1[i] = projectProvider.sum.total_budget_amount / 10000;
			deptValue2[i] = projectProvider.sum.total_investment_amount / 10000;
		}
		return create2StackedBarChart(Messages.get().ProjectChartFactory_46, deptParameter, deptValue1,
				deptValue2, new String[] { Messages.get().ProjectChartFactory_47, Messages.get().ProjectChartFactory_48 });
	}

	public static Chart getProjectChargerRevenueBar(ProjectProvider data) {
		String[] chargerName = new String[data.sum.subChargerProjectProvider
				.size()];
		double[] userValue1 = new double[data.sum.subChargerProjectProvider
				.size()];
		double[] userValue2 = new double[data.sum.subChargerProjectProvider
				.size()];
		for (int i = 0; i < chargerName.length; i++) {
			ProjectProvider projectProvider = data.sum.subChargerProjectProvider
					.get(i);
			projectProvider.setParameters(data.parameters);
			projectProvider.getData();
			chargerName[i] = projectProvider.getDesc();
			userValue1[i] = projectProvider.sum.total_sales_revenue / 10000;
			userValue2[i] = (projectProvider.sum.total_sales_revenue
					- projectProvider.sum.total_sales_cost) / 10000;
		}
		return create2StackedBarChart(Messages.get().ProjectChartFactory_49, chargerName, userValue1,
				userValue2, new String[] { Messages.get().ProjectChartFactory_50, Messages.get().ProjectChartFactory_51 });
	}

	public static Chart getROIMeter(ProjectProvider data) {
		// "销售收入"
		long value1 = data.sum.total_sales_revenue;
		// "销售成本
		long value2 = data.sum.total_sales_cost;
//				+ data.sum.total_investment_amount;
		// "利润"
		long profit = value1 - value2;

		// 期初资产
		long inv = data.sum.total_investment_amount;

		double roi = value1 == 0 ? 0 : 100d * profit / inv;

		return createMeterChart("ROI", "ROI", roi); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static Chart getProjectDesignatedInvestmentBar(Project project) {
		return getProjectAggrgationCost(project,
				IModelConstants.C_WORKORDER_COST);
	}

	public static Chart getProjectAllocationInvestmentBar(Project project) {
		return getProjectAggrgationCost(project,
				IModelConstants.C_RND_PEROIDCOST_ALLOCATION);
	}

	private static Chart getProjectAggrgationCost(Project project,
			String collection) {
		List<DBObject> result = project.getAggregationCost(collection);

		String seriesTitle = Messages.get().ProjectChartFactory_54;
		String title = project.getLabel() + Messages.get().ProjectChartFactory_55;

		String[] deptParameter = new String[result.size()];
		double[] deptValue1 = new double[result.size()];
		for (int i = 0; i < result.size(); i++) {
			DBObject dbo = result.get(i);
			Object value = dbo.get("_id"); //$NON-NLS-1$
			if (value instanceof DBObject) {
				deptParameter[i] = ((DBObject) value).get("year") + "/" //$NON-NLS-1$ //$NON-NLS-2$
						+ ((DBObject) value).get("month"); //$NON-NLS-1$

			} else {
				deptParameter[i] = "unknown"; //$NON-NLS-1$
			}

			value = dbo.get("summ"); //$NON-NLS-1$
			if (value instanceof Number) {
				deptValue1[i] = getDoubleValueTenThousand(((Number) value)
						.doubleValue());
			} else {
				deptValue1[i] = 0d;
			}
		}

		return createStackedBarChart(title, deptParameter, deptValue1,
				seriesTitle);
	}

	private static double getDoubleValueTenThousand(double value) {
		BigDecimal b = new BigDecimal(value / 10000);
		return b.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static Chart getProjectRevenueBar(Project project) {
		List<DBObject> result = project.getAggregationRevenue();
		List<DBObject> costResult1 = project
				.getAggregationCost(IModelConstants.C_WORKORDER_COST);
		List<DBObject> costResult2 = project
				.getAggregationCost(IModelConstants.C_RND_PEROIDCOST_ALLOCATION);

		// 构造期间
		Set<String> p = new HashSet<String>();
		for (int i = 0; i < costResult1.size(); i++) {
			Object value = costResult1.get(i).get("_id"); //$NON-NLS-1$
			if (value instanceof DBObject) {
				p.add(getCode(value));
			}
		}

		for (int i = 0; i < costResult2.size(); i++) {
			Object value = costResult2.get(i).get("_id"); //$NON-NLS-1$
			if (value instanceof DBObject) {
				p.add(getCode(value));
			}
		}

		for (int i = 0; i < result.size(); i++) {
			Object value = result.get(i).get("_id"); //$NON-NLS-1$
			if (value instanceof DBObject) {
				p.add(getCode2(value));
			}
		}

		ArrayList<String> l = new ArrayList<String>();
		l.addAll(p);
		Collections.sort(l);

		String[] seriesTitle = new String[] { Messages.get().ProjectChartFactory_65, Messages.get().ProjectChartFactory_66 };
		String title = project.getLabel() + Messages.get().ProjectChartFactory_67;

		String[] deptParameter = l.toArray(new String[0]);
		double[] deptValue1 = new double[deptParameter.length];// 收入
		double[] deptValue2 = new double[deptParameter.length];// 成本
		double[] deptValue3 = new double[deptParameter.length];// 利润

		for (int i = 0; i < deptParameter.length; i++) {
			double[] v = getRevenueData(deptParameter[i], result);
			deptValue1[i] = getDoubleValueTenThousand(v[1]);
			double v1 = getCostData(deptParameter[i], costResult1);
			double v2 = getCostData(deptParameter[i], costResult2);
			deptValue2[i] = getDoubleValueTenThousand(v[0] + v1 + v2);
			deptValue3[i] = deptValue1[i] - deptValue2[i];
		}

		return createLineChart(title, deptParameter, deptValue1, deptValue3,
				seriesTitle);
	}

	private static String getCode2(Object value) {
		return ((DBObject) value).get(SalesData.F_ACCOUNT_YEAR)
				+ "/" //$NON-NLS-1$
				+ ("" + ((DBObject) value).get(SalesData.F_ACCOUNT_MONTH)) //$NON-NLS-1$
						.substring(1);
	}

	private static String getCode(Object value) {
		return ((DBObject) value).get("year") + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ String.format("%02d", ((DBObject) value).get("month")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static double getCostData(String code, List<DBObject> result) {
		for (int i = 0; i < result.size(); i++) {
			DBObject dbObject = result.get(i);
			Object value = dbObject.get("_id"); //$NON-NLS-1$
			if (value instanceof DBObject) {
				String _code = getCode(value);
				if (_code.equals(code)) {
					Object v = dbObject.get("summ"); //$NON-NLS-1$
					if (v instanceof Number) {
						return ((Number) v).doubleValue();
					} else {
						return 0d;
					}
				}
			}
		}
		return 0d;
	}

	private static double[] getRevenueData(String code, List<DBObject> result) {
		for (int i = 0; i < result.size(); i++) {
			DBObject dbObject = result.get(i);
			Object value = dbObject.get("_id"); //$NON-NLS-1$
			if (value instanceof DBObject) {
				DBObject dbo = (DBObject) value;
				String _code = getCode2(dbo);
				if (_code.equals(code)) {
					double[] ret = new double[2];
					Object v = dbObject.get(SalesData.F_SALES_COST);
					if (v instanceof Number) {
						ret[0] = ((Number) v).doubleValue();
					} else {
						ret[0] = 0d;
					}
					v = dbObject.get(SalesData.F_SALES_INCOME);
					if (v instanceof Number) {
						ret[1] = ((Number) v).doubleValue();
					} else {
						ret[1] = 0d;
					}
					return ret;
				}
			}
		}

		return new double[] { 0d, 0d };
	}

}
