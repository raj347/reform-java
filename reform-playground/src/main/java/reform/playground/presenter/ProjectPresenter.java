package reform.playground.presenter;

import reform.core.procedure.Procedure;
import reform.core.project.Picture;
import reform.data.sheet.Sheet;
import reform.evented.core.EventedProject;
import reform.identity.FastIterable;
import reform.identity.Identifier;
import reform.identity.IdentifierEmitter;
import reform.math.Vec2i;
import reform.naming.Name;
import reform.playground.actions.NewPictureAction;
import reform.playground.presenter.PicturePresenter.Listener;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ProjectPresenter implements Listener
{
	private final EventedProject _project;
	private final Map<Identifier<? extends Picture>, PicturePresenter> _pictures = new
			HashMap<>();

	private final ThumbnailView _picturesView = new ThumbnailView(
			new ThumbnailAdapter(this));
	private final JPanel _bottom = new JPanel(new BorderLayout());
	private final JScrollPane _scroller = new JScrollPane(_picturesView,
	                                                      ScrollPaneConstants
			                                                      .VERTICAL_SCROLLBAR_NEVER,

	                                                      ScrollPaneConstants
			                                                      .HORIZONTAL_SCROLLBAR_ALWAYS);
	private final JSplitPane _split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
	                                                 _scroller, _bottom);
	private final IdentifierEmitter _idEmitter;
	private Identifier<? extends Picture> _selected;

	public ProjectPresenter(final EventedProject project, final IdentifierEmitter
			idEmitter)
	{
		_idEmitter = idEmitter;
		_project = project;

		_bottom.setPreferredSize(new Dimension(1100, 500));

		_scroller.setMinimumSize(_picturesView.getPreferredSize());
		_scroller.setBorder(BorderFactory.createEmptyBorder());


		final FastIterable<Identifier<? extends Picture>> pictures = _project
				.getPictures();
		for (int i = 0, j = pictures.size(); i < j; i++)
		{
			initializePicture(pictures.get(i));
		}

		if (_project.getPictureCount() > 0)
		{
			selectPicture(_project.getPictureAtIndex(0));
		}

		_project.addListener(new ProjectListener(this));

		final InputMap inputMap = _picturesView.getInputMap(
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap actionMap = _picturesView.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("alt meta N"), "newPicture");
		actionMap.put("newPicture", new NewPictureAction(project, idEmitter));
	}

	private void initializePicture(final Identifier<? extends Picture> id)
	{
		final PicturePresenter presenter = new PicturePresenter(
				_project.getEventedPicture(id), _idEmitter);
		_pictures.put(id, presenter);
		presenter.addListener(this);
		presenter.update();
	}

	private void destructPicture(final Identifier<? extends Picture> pictureId)
	{
		final PicturePresenter presenter = _pictures.remove(pictureId);
		presenter.removeListener(this);
		if (_selected != null && _selected.equals(pictureId))
		{
			_selected = null;
			_bottom.removeAll();
			_bottom.revalidate();
			_bottom.repaint();
		}
		_picturesView.update();
	}

	private void updatePicture(final Identifier<? extends Picture> pictureId)
	{
		_pictures.get(pictureId).update();
		for(int i=0,j=_project.getPictureCount();i<j;i++) {
			_pictures.get(_project.getPictureAtIndex(i)).notifyPictureChange(pictureId);
		}
	}

	private void selectPicture(final Identifier<? extends Picture> pictureId)
	{
		_selected = pictureId;

		_pictures.get(pictureId).appendTo(_bottom);
	}

	private boolean isSelected(final Identifier<? extends Picture> pictureId)
	{
		return _selected != null && _selected.equals(pictureId);
	}

	public JComponent getView()
	{
		return _split;
	}

	@Override
	public void onPreviewChange(final PicturePresenter presenter)
	{
		_picturesView.update();
	}

	private static class ProjectListener implements EventedProject.Listener
	{

		private final ProjectPresenter _presenter;

		public ProjectListener(final ProjectPresenter presenter)
		{
			_presenter = presenter;
		}

		@Override
		public void onPictureAdded(final EventedProject project, final Identifier<?
				extends Picture> pictureId)
		{
			_presenter.initializePicture(pictureId);
			_presenter.selectPicture(pictureId);
		}

		@Override
		public void onPictureRemoved(final EventedProject project, final Identifier<?
				extends Picture> pictureId)
		{
			_presenter.destructPicture(pictureId);
		}

		@Override
		public void onPictureChanged(final EventedProject project, final Identifier<?
				extends Picture> pictureId)
		{
			_presenter.updatePicture(pictureId);
		}

	}

	private static class ThumbnailAdapter implements ThumbnailView.Adapter
	{
		private final ProjectPresenter _presenter;

		public ThumbnailAdapter(final ProjectPresenter presenter)
		{
			_presenter = presenter;
		}

		@Override
		public int getCount()
		{
			return _presenter._pictures.size();
		}

		@Override
		public int getWidth(final int i)
		{
			final Identifier<? extends Picture> id = _presenter._project
					.getPictureAtIndex(i);
			return _presenter._pictures.get(id).getPreview().getWidth();
		}

		@Override
		public int getHeight(final int i)
		{

			final Identifier<? extends Picture> id = _presenter._project
					.getPictureAtIndex(i);
			return _presenter._pictures.get(id).getPreview().getHeight();
		}

		@Override
		public boolean isSelected(final int i)
		{
			return _presenter.isSelected(_presenter._project.getPictureAtIndex(i));
		}

		@Override
		public void draw(final Graphics2D g2, final int i)
		{
			final Identifier<? extends Picture> id = _presenter._project
					.getPictureAtIndex(i);
			_presenter._pictures.get(id).getPreview().draw(g2);
		}

		@Override
		public void onClickAtIndex(final int clicked)
		{
			_presenter.selectPicture(_presenter._project.getPictureAtIndex(clicked));

		}

		@Override
		public void onDoubleClick()
		{
			final Picture newPicture = new Picture(_presenter._idEmitter.emit(),
			                                       new Name("New Picture"),
			                                       new Vec2i(400, 400), new Sheet(),
			                                       new Procedure(), new Sheet());
			_presenter._project.addPicture(newPicture);
			_presenter.selectPicture(newPicture.getId());
		}

		@Override
		public void onDoubleClickAtIndex(final int clicked)
		{
			_presenter._project.removePicture(
					_presenter._project.getPictureAtIndex(clicked));

		}

	}
}
