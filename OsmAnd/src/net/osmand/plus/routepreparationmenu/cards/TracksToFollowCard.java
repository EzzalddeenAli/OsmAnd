package net.osmand.plus.routepreparationmenu.cards;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.osmand.IndexConstants;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.GpxTrackAdapter;
import net.osmand.plus.helpers.GpxUiHelper.GPXInfo;
import net.osmand.plus.mapcontextmenu.other.HorizontalSelectionAdapter;
import net.osmand.util.Algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TracksToFollowCard extends BaseCard {

	private Map<String, List<GPXInfo>> gpxInfoCategories;

	private List<GPXInfo> gpxInfoList;
	private String selectedCategory;
	private String defaultCategory;
	private String visibleCategory;

	private GpxTrackAdapter tracksAdapter;

	public TracksToFollowCard(MapActivity mapActivity, List<GPXInfo> gpxInfoList, String selectedCategory) {
		super(mapActivity);
		this.gpxInfoList = gpxInfoList;
		this.selectedCategory = selectedCategory;
		defaultCategory = app.getString(R.string.shared_string_all);
		visibleCategory = app.getString(R.string.shared_string_visible);
		gpxInfoCategories = getGpxInfoCategories();
	}

	public void setGpxInfoList(List<GPXInfo> gpxInfoList) {
		this.gpxInfoList = gpxInfoList;
		gpxInfoCategories = getGpxInfoCategories();
	}

	public List<GPXInfo> getGpxInfoList() {
		return gpxInfoList;
	}

	public String getSelectedCategory() {
		return selectedCategory;
	}

	@Override
	public int getCardLayoutId() {
		return R.layout.follow_track_card;
	}

	@Override
	protected void updateContent() {
		setupCategoriesRow();
		setupTracksItems();
	}

	private void setupTracksItems() {
		RecyclerView filesRecyclerView = view.findViewById(R.id.track_list);
		filesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		filesRecyclerView.setNestedScrollingEnabled(false);

		tracksAdapter = new GpxTrackAdapter(view.getContext(), gpxInfoList, false, showFoldersName());
		tracksAdapter.setAdapterListener(new GpxTrackAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				if (position != RecyclerView.NO_POSITION) {
					GPXInfo gpxInfo = tracksAdapter.getGpxInfoList().get(position);
					CardListener listener = getListener();
					if (listener != null) {
						int index = gpxInfoList.indexOf(gpxInfo);
						listener.onCardButtonPressed(TracksToFollowCard.this, index);
					}
				}
			}
		});
		filesRecyclerView.setAdapter(tracksAdapter);
	}

	private void setupCategoriesRow() {
		final HorizontalSelectionAdapter selectionAdapter = new HorizontalSelectionAdapter(app, nightMode);
		selectionAdapter.setTitledItems(new ArrayList<>(gpxInfoCategories.keySet()));
		selectionAdapter.setSelectedItemByTitle(selectedCategory);
		selectionAdapter.setListener(new HorizontalSelectionAdapter.HorizontalSelectionAdapterListener() {
			@Override
			public void onItemSelected(HorizontalSelectionAdapter.HorizontalSelectionItem item) {
				selectedCategory = item.getTitle();
				List<GPXInfo> items = gpxInfoCategories.get(selectedCategory);
				tracksAdapter.setShowFolderName(showFoldersName());
				tracksAdapter.setGpxInfoList(items != null ? items : new ArrayList<GPXInfo>());
				tracksAdapter.notifyDataSetChanged();

				selectionAdapter.notifyDataSetChanged();
			}
		});

		RecyclerView iconCategoriesRecyclerView = view.findViewById(R.id.track_categories);
		iconCategoriesRecyclerView.setAdapter(selectionAdapter);
		iconCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(app, RecyclerView.HORIZONTAL, false));
		selectionAdapter.notifyDataSetChanged();
	}

	private boolean showFoldersName() {
		return defaultCategory.equals(selectedCategory) || visibleCategory.equals(selectedCategory);
	}

	private Map<String, List<GPXInfo>> getGpxInfoCategories() {
		Map<String, List<GPXInfo>> gpxInfoCategories = new LinkedHashMap<>();

		gpxInfoCategories.put(visibleCategory, new ArrayList<GPXInfo>());
		gpxInfoCategories.put(defaultCategory, new ArrayList<GPXInfo>());

		for (GPXInfo info : gpxInfoList) {
			if (info.isSelected()) {
				addGpxInfoCategory(gpxInfoCategories, info, visibleCategory);
			}
			if (!Algorithms.isEmpty(info.getFileName())) {
				File file = new File(info.getFileName());
				String dirName = file.getParent();
				if (dirName != null && !IndexConstants.GPX_INDEX_DIR.equals(dirName)) {
					addGpxInfoCategory(gpxInfoCategories, info, dirName);
				}
			}
			addGpxInfoCategory(gpxInfoCategories, info, defaultCategory);
		}

		return gpxInfoCategories;
	}

	private void addGpxInfoCategory(Map<String, List<GPXInfo>> data, GPXInfo info, String category) {
		List<GPXInfo> items = data.get(category);
		if (items == null) {
			items = new ArrayList<>();
			data.put(category, items);
		}
		items.add(info);
	}
}