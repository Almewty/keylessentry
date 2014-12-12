package de.w_hs.keylessentry.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.Collections;
import java.util.List;

import de.w_hs.keylessentry.BuildConfig;
import de.w_hs.keylessentry.R;
import de.w_hs.keylessentry.data.DataStorage;
import de.w_hs.keylessentry.data.Door;

import static de.w_hs.keylessentry.Constants.*;

public class ListDoorsActivity extends ListActivity {
    private ArrayAdapter<Door> arrayAdapter;

    private static final int VIEW_ID = 0;
    private static final int DELETE_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_doors);

        getListView().setEmptyView(findViewById(R.id.empty_list));

        List<Door> doors = DataStorage.getInstance(getApplicationContext()).getDoors();
        Collections.sort(doors);

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_listview_item);
        setListAdapter(arrayAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        arrayAdapter.clear();
        List<Door> doors = DataStorage.getInstance(getApplicationContext()).getDoors();
        Collections.sort(doors);
        arrayAdapter.addAll(doors);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(arrayAdapter.getItem(info.position).getName());
        if (BuildConfig.DEBUG) {
            menu.add(Menu.NONE, VIEW_ID, VIEW_ID, getString(R.string.view));
        }
        menu.add(Menu.NONE, DELETE_ID, DELETE_ID, getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Door door = arrayAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case DELETE_ID:
                DataStorage.getInstance(getApplicationContext()).deleteDoor(door);
                arrayAdapter.remove(door);
                break;
            case VIEW_ID:
                Intent intent = new Intent(this, ShowDoorActivity.class);
                intent.putExtra(INTENT_EXTRA_DOOR, door);
                startActivity(intent);
                break;
        }
        return true;
    }
}
