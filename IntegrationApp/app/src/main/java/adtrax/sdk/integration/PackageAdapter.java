package adtrax.sdk.integration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PackageAdapter extends ArrayAdapter<ApplicationInfo> {

    Context mContext;
    List<ApplicationInfo> packageList = new ArrayList<>();
    List<ApplicationInfo> tempItems, suggestions;

    public PackageAdapter(@NonNull Context context, @NonNull List<ApplicationInfo> objects) {
        super(context, 0, objects);
        mContext = context;
        packageList = objects;
        tempItems = new ArrayList<ApplicationInfo>(objects);
        suggestions = new ArrayList<ApplicationInfo>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.package_layout,parent,false);
        }

        ApplicationInfo info = suggestions.get(position);
        ImageView packageIcon = listItem.findViewById(R.id.packageIcon);
        TextView packageName = listItem.findViewById(R.id.packageName);

        packageIcon.setBackground(mContext.getPackageManager().getApplicationIcon(info));
        packageName.setText(info.packageName);

        return listItem;
    }

    @Override
    public int getCount() {
        return (null != suggestions ? suggestions.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int index) {
        return suggestions.get(index);
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((ApplicationInfo) resultValue).packageName;
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (ApplicationInfo names : tempItems) {
                    if (names.packageName.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(names);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            }
        }
    };
}
