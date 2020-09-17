package ch.admin.bag.dp3t.util;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import java.util.concurrent.atomic.AtomicLong;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.debug.DebugFragment;
import ch.admin.bag.dp3t.html.HtmlFragment;

public class ToolbarUtil {

	public static void setupToolbar(Context context, @NonNull Toolbar toolbar, FragmentManager fragmentManager) {
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.homescreen_menu_impressum) {
				HtmlFragment htmlFragment =
						HtmlFragment.newInstance(R.string.menu_impressum, AssetUtil.getImpressumBaseUrl(context),
								AssetUtil.getImpressumHtml(context));
				fragmentManager.beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.root_fragment_container, htmlFragment)
						.addToBackStack(HtmlFragment.class.getCanonicalName())
						.commit();
				return true;
			}
			return false;
		});

		View schwiizerchruez = toolbar.findViewById(R.id.schwiizerchruez);
		setupDebugButton(schwiizerchruez, fragmentManager);
	}

	private static void setupDebugButton(View schwiizerchruez, FragmentManager fragmentManager) {
		if (!DebugFragment.EXISTS) {
			return;
		}

		AtomicLong lastClick = new AtomicLong(0);
		schwiizerchruez.setOnClickListener(v -> {
			if (lastClick.get() > System.currentTimeMillis() - 1000L) {
				lastClick.set(0);
				DebugFragment.startDebugFragment(fragmentManager);
			} else {
				lastClick.set(System.currentTimeMillis());
			}
		});
	}

}
