package dajver.draggblelist;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<String> list;
    private DragListAdapter dragListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //просто заполняем массив что бы список не был пуст
        list = new ArrayList<String>();
        for(int i=1; i<10; i++){
            list.add("Item " + i);
        }

        //инициализируем лист вью
        DragListView dragListView = (DragListView) findViewById(R.id.listView);
        //заполняем адаптер
        dragListAdapter = new DragListAdapter(this, list);
        //выводим в листвью
        dragListView.setAdapter(dragListAdapter);
    }
}
