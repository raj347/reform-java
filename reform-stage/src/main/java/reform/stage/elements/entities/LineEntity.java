package reform.stage.elements.entities;

import reform.core.analyzer.Analyzer;
import reform.core.forms.Form;
import reform.core.forms.LineForm;
import reform.core.graphics.DrawingType;
import reform.core.runtime.Runtime;
import reform.identity.Identifier;
import reform.math.Vec2;
import reform.math.Vector;
import reform.stage.elements.Entity;
import reform.stage.elements.EntityPoint;
import reform.stage.elements.Handle;
import reform.stage.elements.PivotPair;
import reform.stage.elements.outline.EntityOutline;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class LineEntity implements Entity
{
	private final Identifier<? extends LineForm> _formId;

	private final EntityPoint _start;
	private final EntityPoint _end;
	private final EntityPoint _center;

	private final Handle _startHandle;
	private final Handle _endHandle;

	private final EntityOutline.Line _outline;

	private final GeneralPath.Double _shape = new GeneralPath.Double();

	private final ArrayList<EntityPoint> _points = new ArrayList<>();
	private final ArrayList<Handle> _handles = new ArrayList<>();

	private String _label = "Line";

	private boolean _isGuide = false;

	public LineEntity(final Identifier<? extends LineForm> formId)
	{
		_formId = formId;

		_start = new EntityPoint(formId, LineForm.Point.Start);
		_end = new EntityPoint(formId, LineForm.Point.End);
		_center = new EntityPoint(formId, LineForm.Point.Center);

		_points.add(_start);
		_points.add(_end);
		_points.add(_center);

		_startHandle = new Handle(formId, LineForm.Point.Start, LineForm.Anchor.Start,
		                          new PivotPair(_end, _center));
		_endHandle = new Handle(formId, LineForm.Point.End, LineForm.Anchor.End,
		                        new PivotPair(_start, _center));

		_handles.add(_startHandle);
		_handles.add(_endHandle);

		_outline = new EntityOutline.Line(formId);

	}

	@Override
	public void updateForRuntime(final Runtime runtime, final Analyzer analyzer)
	{
		_start.updateForRuntime(runtime, analyzer);
		_end.updateForRuntime(runtime, analyzer);
		_center.updateForRuntime(runtime, analyzer);

		_outline.update(_start.getX(), _start.getY(), _end.getX(), _end.getY());

		_startHandle.updateForRuntime(runtime, analyzer);
		_endHandle.updateForRuntime(runtime, analyzer);

		_shape.reset();
		final Form form = runtime.get(_formId);
		form.appendToPathForRuntime(runtime, _shape);

		_label = form.getName().getValue();

		_isGuide = runtime.get(_formId).getType() == DrawingType.Guide;

	}

	@Override
	public List<EntityPoint> getSnapPoints()
	{
		return _points;
	}

	@Override
	public List<Handle> getHandles()
	{
		return _handles;
	}

	@Override
	public boolean contains(final Vec2 position)
	{
		return Vector.isBetween(position.x, position.y, _start.getX(), _start.getY(),
		                        _end.getX(), _end.getY(), 0.5);
	}

	@Override
	public Identifier<? extends LineForm> getId()
	{
		return _formId;
	}

	@Override
	public EntityOutline getOutline()
	{
		return _outline;
	}

	@Override
	public Shape getShape()
	{
		return _shape;
	}

	@Override
	public boolean isGuide()
	{
		return _isGuide;
	}


	@Override
	public String getLabel()
	{
		return _label;
	}

}
