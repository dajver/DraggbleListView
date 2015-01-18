package dajver.draggblelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DragListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    public DragListAdapter(Context context, List<String> objects) {
        super(context, 0, objects);
        this.mContext = context;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        view = LayoutInflater.from(getContext()).inflate(R.layout.custom_item, null);

        TextView textView = (TextView)view.findViewById(R.id.name);
        textView.setText(getItem(position));

        return view;
    }
}