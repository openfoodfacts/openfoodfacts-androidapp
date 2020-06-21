package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.jobs.DownloadOfflineProductService;
import openfoodfacts.github.scrachx.openfood.models.OfflineListItem;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.models.OfflineListItem.TYPE_LARGE;
import static openfoodfacts.github.scrachx.openfood.models.OfflineListItem.TYPE_SMALL;

/**
 * Created by Prashant on 25/02/19.
 */

public class OfflineListAdapter extends RecyclerView.Adapter<OfflineListAdapter.ViewHolder> {

    private Activity activity;
    private List<OfflineListItem> list;
    private SharedPreferences settings;

    public OfflineListAdapter(List<OfflineListItem> list, Activity activity) {
        this.list = list;
        this.activity = activity;
        settings = Objects.requireNonNull(activity).getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.offline_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (list.get(position).getType() == TYPE_SMALL) {
            holder.name.setText(list.get(position).getName() + " (Small)");
        } else if (list.get(position).getType() == TYPE_LARGE) {
            holder.name.setText(list.get(position).getName() + " (Large)");
        }
        holder.size.setText(list.get(position).getSize() + "mb");
        if (settings.getBoolean(list.get(position).getUrl() + "extract", false)) {
            holder.download.setImageResource(R.drawable.baseline_offline_pin_24);
        } else {
            holder.download.setImageResource(R.drawable.round_cloud_download_24);
        }
        holder.download.setOnClickListener(view -> {
            download(list.get(position).getUrl(), holder.progressbar, position);
        });
        int progress = list.get(position).getProgress();
        if (progress <= 0 || progress >= 100) {
            if (progress == 200) {
                //intermediate step
                holder.progressbar.setIndeterminate(true);
            } else {
                holder.progressbar.setVisibility(View.GONE);
            }
        } else {
            holder.progressbar.setIndeterminate(false);
            holder.progressbar.setVisibility(View.VISIBLE);
            holder.progressbar.setProgress(progress);
        }

        holder.options.setOnClickListener(view -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(activity, holder.options);
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_offline_item);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        //handle menu1 click
//                        OfflineProductDao mOfflineProductDao = Utils.getAppDaoSession(activity).getOfflineProductDao();
//                        DeleteQuery<OfflineProduct> tableDeleteQuery = mOfflineProductDao.queryBuilder()
//                                .where(OfflineProductDao.Properties.Url.eq(list.get(position).getUrl()))
//                                .buildDelete();
//                        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
//                        holder.download.setImageResource(R.drawable.round_cloud_download_24);
//                        settings.edit().putBoolean(list.get(position).getUrl() + "download", false)
//                                .putBoolean(list.get(position).getUrl() + "download", false).apply();
                        return true;
                    case R.id.action_update:
                        //handle menu2 click
                        settings.edit().putBoolean(list.get(position).getUrl() + "download", false)
                                .putBoolean(list.get(position).getUrl() + "download", false).apply();
                        download(list.get(position).getUrl(), holder.progressbar, position);
                        return true;
                    default:
                        return false;
                }
            });
            //displaying the popup
            popup.show();
        });

    }

    public void download(String url, ProgressBar progressBar, int position) {
        if (Utils.isStoragePermissionGranted(activity)) {
            if (DownloadOfflineProductService.isDownloadOfflineProductServiceRunning) {
                Toast.makeText(activity, activity.getString(R.string.toast_already_running), Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                Toast.makeText(activity, activity.getString(R.string.toast_starting_download), Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(activity, DownloadOfflineProductService.class);
                serviceIntent.putExtra("url", url);
                serviceIntent.putExtra("index", position);
                Objects.requireNonNull(activity).startService(serviceIntent);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView size;
        ImageButton download;
        TextView options;
        ProgressBar progressbar;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            size = itemView.findViewById(R.id.size);
            download = itemView.findViewById(R.id.download);
            options = itemView.findViewById(R.id.options);
            progressbar = itemView.findViewById(R.id.progressbar);
        }
    }
}
