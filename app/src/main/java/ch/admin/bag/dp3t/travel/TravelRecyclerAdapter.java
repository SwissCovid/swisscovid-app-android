package ch.admin.bag.dp3t.travel;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import ch.admin.bag.dp3t.R;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class TravelRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	List<TravelRecyclerItem> items = new ArrayList<>();


	@Override
	public int getItemViewType(int position) {
		return items.get(position).getViewType().ordinal();
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		TravelRecyclerItem.ViewType type = TravelRecyclerItem.ViewType.values()[viewType];

		switch (type) {
			case ICON_AND_TEXT:
				return new IconAndTextViewHolder(
						LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_icon_and_text, parent, false));
			case HEADER:
				return new HeaderViewHolder(
						LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_text_header, parent, false));

			case BUTTON:
				return new ButtonViewHolder(
						LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_button, parent, false));
			case COUNTRY:
				return new CountryViewHolder(
						(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_country_and_toggle, parent,
								false)));
			case ALL_CAPS_HEADER:
				return new AllCapsHeaderViewHolder(
						(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_text_header_all_caps, parent,
								false)));
			case EDITABLE_COUNTRY:
				return new EditableCountryViewHolder(
						(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_editable_country, parent,
								false)));
			case SPACE:
				return new SpaceViewHolder(
						(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_space, parent,
								false)));
			default:
				return null;
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		TravelRecyclerItem item = items.get(position);

		switch (item.getViewType()) {
			case ICON_AND_TEXT:
				((IconAndTextViewHolder) holder).bind((ItemIconAndText) item);
				break;
			case HEADER:
				((HeaderViewHolder) holder).bind((ItemHeader) item);
				break;
			case BUTTON:
				((ButtonViewHolder) holder).bind((ItemButton) item);
				break;
			case COUNTRY:
				((CountryViewHolder) holder).bind((ItemCountry) item);
				break;
			case ALL_CAPS_HEADER:
				((AllCapsHeaderViewHolder) holder).bind((ItemAllCapsHeader) item);
				break;
			case EDITABLE_COUNTRY:
				((EditableCountryViewHolder) holder).bind((ItemEditableCountry) item);
				break;
			case SPACE:
				((SpaceViewHolder) holder).bind((ItemSpace) item);
				break;
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setData(List<TravelRecyclerItem> items) {

		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TravelDiffUtil(this.items, items), true);
		diffResult.dispatchUpdatesTo(this);

		this.items.clear();
		this.items.addAll(items);
	}

	public class SpaceViewHolder extends RecyclerView.ViewHolder {

		public SpaceViewHolder(View itemView) {super(itemView);}

		public void bind(ItemSpace item) {
			itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), item.backgroundColorResId));
			int height = itemView.getContext().getResources().getDimensionPixelSize(item.height);
			itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
		}

	}


	public class EditableCountryViewHolder extends RecyclerView.ViewHolder {
		TextView countryTextView;
		ImageView flagImageView;
		ImageView addRemoveImageView;
		ImageView dragAndDropIconView;


		public EditableCountryViewHolder(@NonNull View itemView) {
			super(itemView);
			this.countryTextView = itemView.findViewById(R.id.travel_country_item_name);
			this.addRemoveImageView = itemView.findViewById(R.id.travel_country_add_remove_button);
			this.flagImageView = itemView.findViewById(R.id.travel_country_item_flag);
			this.dragAndDropIconView = itemView.findViewById(R.id.drag_and_drop_icon);
		}

		public void bind(ItemEditableCountry item) {
			addRemoveImageView.setOnClickListener(item.onAddRemoveClickedListener);
			countryTextView.setText(item.countryName);
			flagImageView.setImageResource(item.flagResId);
			addRemoveImageView.setImageResource(item.addRemoveIconResId);
			if (item.showDragAndDropIcon) dragAndDropIconView.setVisibility(View.VISIBLE);
			else dragAndDropIconView.setVisibility(View.GONE);
		}

	}


	public class CountryViewHolder extends RecyclerView.ViewHolder {
		TextView countryTextView;
		SwitchMaterial isActiveSwitch;
		ImageView flagImageView;
		TextView statusTextView;
		View topSeparator;

		public CountryViewHolder(@NonNull View itemView) {
			super(itemView);
			this.countryTextView = itemView.findViewById(R.id.travel_country_item_name);
			this.isActiveSwitch = itemView.findViewById(R.id.travel_country_item_switch);
			this.flagImageView = itemView.findViewById(R.id.travel_country_item_flag);
			this.statusTextView = itemView.findViewById(R.id.status_textview);
			this.topSeparator = itemView.findViewById(R.id.separator_top);
		}

		public void bind(ItemCountry item) {
			isActiveSwitch.setOnCheckedChangeListener(item.checkedChangeListener);
			countryTextView.setText(item.countryName);
			isActiveSwitch.setChecked(item.isActive);
			flagImageView.setImageResource(item.flagResId);
			if (item.showTopSeparator) topSeparator.setVisibility(View.VISIBLE);
			else topSeparator.setVisibility(View.GONE);
			if (item.statusText != null && !item.statusText.equals("")) {
				statusTextView.setVisibility(View.VISIBLE);
				statusTextView.setText(item.statusText);
			} else statusTextView.setVisibility(View.GONE);
		}

	}


	public class ButtonViewHolder extends RecyclerView.ViewHolder {
		Button button;

		public ButtonViewHolder(@NonNull View itemView) {
			super(itemView);
			this.button = itemView.findViewById(R.id.travel_add_countries_button);
		}

		public void bind(ItemButton item) {
			button.setText(item.buttonTitleStringId);
			button.setOnClickListener(item.onClickListener);
		}

	}


	public class HeaderViewHolder extends RecyclerView.ViewHolder {
		TextView headerTextView;

		public HeaderViewHolder(@NonNull View itemView) {
			super(itemView);
			this.headerTextView = itemView.findViewById(R.id.travel_item_header);
		}

		public void bind(ItemHeader item) {
			headerTextView.setText(item.headerTextResId);
		}

	}


	public class AllCapsHeaderViewHolder extends RecyclerView.ViewHolder {
		TextView headerTextView;

		public AllCapsHeaderViewHolder(@NonNull View itemView) {
			super(itemView);
			this.headerTextView = itemView.findViewById(R.id.travel_item_header);
		}

		public void bind(ItemAllCapsHeader item) {
			headerTextView.setText(item.headerTextResId);
		}

	}


	public class IconAndTextViewHolder extends RecyclerView.ViewHolder {
		public TextView textView;
		public ImageView iconView;

		public IconAndTextViewHolder(@NonNull View itemView) {
			super(itemView);
			this.textView = itemView.findViewById(R.id.travel_item_text);
			this.iconView = itemView.findViewById(R.id.travel_item_icon);
		}

		public void bind(ItemIconAndText item) {
			textView.setText(item.textResourceId);
			iconView.setImageResource(item.iconResourceId);
			ImageViewCompat.setImageTintList(iconView,
					ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), item.iconColorId)));
			itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), item.backgroundColorId));
		}

	}

}
