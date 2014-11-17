package chat.sample.buddy.com.buddychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.buddy.sdk.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shawn on 8/27/14.
 */
abstract class SimpleAdapter<T> extends ArrayAdapter<T> {

    private List<T> itemList;
    private Context context;

    public SimpleAdapter(List<T> itemList, Context ctx) {
        super(ctx, android.R.layout.simple_list_item_2, itemList);
        this.itemList = itemList;
        this.context = ctx;
    }

    public int getCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public T getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public long getItemId(int position) {
        if (itemList != null)
            return itemList.get(position).hashCode();
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        T c = itemList.get(position);

        populateView(v, c);
        return v;

    }

    protected  abstract <T> void populateView(View v, T item);

    public List<T> getItemList() {
        return itemList;
    }

    public void setItemList(List<T> itemList) {

        this.itemList = itemList;
    }

    public void appendItemList(List<T> itemList) {

        if (this.itemList == null) {
            this.itemList = new ArrayList<T>();
        }
        for (T item : itemList) {
            this.itemList.add(item);
        }
    }

}
