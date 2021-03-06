package reform.rendering.canvas;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Canvas extends JComponent
{

	private static final long serialVersionUID = 1L;

	private final HashMap<RenderingHints.Key, Object> _renderingHits = new HashMap<>();
	private final ArrayList<CanvasRenderer> _renderers = new ArrayList<>();

	public Canvas()
	{
		_renderingHits.put(RenderingHints.KEY_ANTIALIASING,
		                   RenderingHints.VALUE_ANTIALIAS_ON);
	}

	@Override
	protected void paintComponent(final Graphics g)
	{
		super.paintComponent(g);

		final Graphics2D g2 = (Graphics2D) g.create();
		final int width = getWidth();
		final int height = getHeight();

		g2.setRenderingHints(_renderingHits);

		for (int i = 0, j = _renderers.size(); i < j; i++)
		{
			_renderers.get(i).render(g2, width, height);
		}

		g2.dispose();
	}

	public void addRenderer(final CanvasRenderer renderer)
	{
		_renderers.add(renderer);
	}
}
