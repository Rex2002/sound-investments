package app.ui;

import java.util.function.Consumer;
import app.mapping.LineData;
import app.mapping.MappedInstr;
import app.mapping.Mapping;
import app.mapping.PointData;
import app.mapping.RangeData;
import dataRepo.Sonifiable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import util.Maths;

public class SonifiablePane extends Pane {
	protected Mapping mapping;
	protected Sonifiable sonifiable;
	protected Font titleFont = new Font("System bold", 40);
	protected Label title; // TODO: Make title it's own class
	protected Line[] verticalLines;
	protected ImageView closeIcon;
	protected LineRangeParamNode priceNode;
	protected LineRangeParamNode movingAvgNode;
	protected LineRangeParamNode relChangeNode;
	protected LineRangeParamNode flagNode;
	protected LineRangeParamNode triangleNode;
	protected LineRangeParamNode vformNode;
	protected PointParamNode trendbreakNode;
	protected PointParamNode eqMovingAvgNode;
	protected PointParamNode eqSupportNode;
	protected PointParamNode eqResistNode;
	protected Consumer<MappedInstr> onChanged;

	protected double labelFontSize;
	protected double cbFontSize;
	protected double cbPad;
	protected double cbMargin;
	protected double labelMargin;

	public SonifiablePane(Mapping mapping, Sonifiable sonifiable, Consumer<MappedInstr> onChanged) {
		this.mapping         = mapping;
		this.sonifiable      = sonifiable;
		this.onChanged       = onChanged;
		this.title           = new Label(sonifiable.getName());
		this.verticalLines   = new Line[] {new Line(), new Line()};
		this.closeIcon       = new ImageView(new Image(getClass().getResource("/close_icon.png").toExternalForm()));
		this.priceNode       = new LineRangeParamNode(mapping, sonifiable, LineData.PRICE, true, onChanged);
		this.movingAvgNode   = new LineRangeParamNode(mapping, sonifiable, LineData.MOVINGAVG, true, onChanged);
		this.relChangeNode   = new LineRangeParamNode(mapping, sonifiable, LineData.RELCHANGE, true, onChanged);
		this.flagNode        = new LineRangeParamNode(mapping, sonifiable, RangeData.FLAG, false, onChanged);
		this.triangleNode    = new LineRangeParamNode(mapping, sonifiable, RangeData.TRIANGLE, false, onChanged);
		this.vformNode       = new LineRangeParamNode(mapping, sonifiable, RangeData.VFORM, false, onChanged);
		this.trendbreakNode  = new PointParamNode(mapping, sonifiable, PointData.TRENDBREAK);
		this.eqMovingAvgNode = new PointParamNode(mapping, sonifiable, PointData.EQMOVINGAVG);
		this.eqSupportNode   = new PointParamNode(mapping, sonifiable, PointData.EQSUPPORT);
		this.eqResistNode    = new PointParamNode(mapping, sonifiable, PointData.EQRESIST);
		getChildren().addAll(title, priceNode, movingAvgNode, relChangeNode, flagNode, triangleNode, vformNode, trendbreakNode, eqMovingAvgNode, eqSupportNode, eqResistNode);
	}

	public Sonifiable getSonifiable() {
		return sonifiable;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
		priceNode      .setMapping(mapping);
		movingAvgNode  .setMapping(mapping);
		relChangeNode  .setMapping(mapping);
		flagNode       .setMapping(mapping);
		triangleNode   .setMapping(mapping);
		vformNode      .setMapping(mapping);
		trendbreakNode .setMapping(mapping);
		eqMovingAvgNode.setMapping(mapping);
		eqSupportNode  .setMapping(mapping);
		eqResistNode   .setMapping(mapping);
	}

	public void showMapping() {
		priceNode      .showMapping();
		movingAvgNode  .showMapping();
		relChangeNode  .showMapping();
		flagNode       .showMapping();
		triangleNode   .showMapping();
		vformNode      .showMapping();
		trendbreakNode .showMapping();
		eqMovingAvgNode.showMapping();
		eqSupportNode  .showMapping();
		eqResistNode   .showMapping();
	}

	public void setFontSizes(double titleFontSize, double labelFontSize, double cbFontSize) {
		titleFont = new Font(titleFont.getName(), titleFontSize);
		title.setFont(titleFont);
		priceNode      .setFontSizes(labelFontSize, cbFontSize);
		movingAvgNode  .setFontSizes(labelFontSize, cbFontSize);
		relChangeNode  .setFontSizes(labelFontSize, cbFontSize);
		flagNode       .setFontSizes(labelFontSize, cbFontSize);
		triangleNode   .setFontSizes(labelFontSize, cbFontSize);
		vformNode      .setFontSizes(labelFontSize, cbFontSize);
		trendbreakNode .setFontSizes(labelFontSize, cbFontSize);
		eqMovingAvgNode.setFontSizes(labelFontSize, cbFontSize);
		eqSupportNode  .setFontSizes(labelFontSize, cbFontSize);
		eqResistNode   .setFontSizes(labelFontSize, cbFontSize);
	}

	protected void layoutChunk(double x, double y, double width, double height, double padLR, double padTB, LabeledCBLayout... nodes) {
		double individualHeight = (height - 2*padTB) / nodes.length;
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].layout(padLR, padTB + individualHeight * i, width - 2*padLR, individualHeight, labelFontSize, cbFontSize, cbPad, cbMargin, labelMargin);
		}
	}

	public void layout(double x, double y, double width, double height, double labelFontSize, double cbFontSize, double cbPad, double cbMargin, double labelMargin) {
		relocate(x, y);
		setPrefSize(width, height);

		double chunkWidth = width/3;
		double padLR = 0; // Maths.clamp(chunkWidth/40d, 2, 50);
		double padTB = 0; // Maths.clamp(height    /60d, 2, 50);

		this.labelFontSize = labelFontSize;
		this.cbFontSize    = cbFontSize;
		this.cbPad         = cbPad;
		this.cbMargin      = cbMargin;
		this.labelMargin   = labelMargin;

		layoutChunk(0*chunkWidth, 0, chunkWidth, height, padLR, padTB, priceNode, movingAvgNode, relChangeNode);
		layoutChunk(1*chunkWidth, 0, chunkWidth, height, padLR, padTB, flagNode, triangleNode, vformNode);
		layoutChunk(1*chunkWidth, 0, chunkWidth, height, padLR, padTB, trendbreakNode, eqMovingAvgNode, eqSupportNode, eqResistNode);
	}
}
