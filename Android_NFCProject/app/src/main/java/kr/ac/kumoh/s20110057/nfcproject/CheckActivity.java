package kr.ac.kumoh.s20110057.nfcproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CheckActivity extends AppCompatActivity {

    public static final String LESSONTAG = "LessonTag";

    protected JSONObject mResult = null;

    protected ArrayList<LessonInfo> mArray = new ArrayList<LessonInfo>();
    protected ListView mList;
    protected LessonAdapter mAdapter;
    protected RequestQueue mQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAdapter = new LessonAdapter(this, R.layout.lesson_item);
        mList = (ListView)findViewById(R.id.lesson_list);
        mList.setAdapter(mAdapter);

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
        Network network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();


        requestLesson();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(LESSONTAG);
        }
    }

    //---------------------------------------------------------------------
    public class LessonInfo {
        String name;
        String date;
        String classNo;
        String teacher;

        public LessonInfo(String name, String date, String classNo,
                          String teacher) {
            super();
            this.name = name;
            this.date = date;
            this.classNo = classNo;
            this.teacher = teacher;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getClassNo() {
            return classNo;
        }

        public String getTeacher() {
            return teacher;
        }
    }
    //---------------------------------------------------------------------
    static class LessonViewHolder {
        TextView txName;
        TextView txDate;
        TextView txClassNo;
        TextView txTeacher;
    }
    //---------------------------------------------------------------------
    public class LessonAdapter extends ArrayAdapter<LessonInfo> {

        private LayoutInflater mInflater = null;

        public LessonAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mArray.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            LessonViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.lesson_item, parent, false);

                viewHolder = new LessonViewHolder();
                viewHolder.txName = (TextView) v.findViewById(R.id.name);
                viewHolder.txDate = (TextView) v.findViewById(R.id.date);
                viewHolder.txClassNo = (TextView) v.findViewById(R.id.class_number);
                viewHolder.txTeacher = (TextView) v.findViewById(R.id.teacher);

                v.setTag(viewHolder);
            } else {
                viewHolder = (LessonViewHolder) v.getTag();
            }

            LessonInfo info = mArray.get(position);
            if (info != null) {
                viewHolder.txName.setText(info.getName());
                viewHolder.txDate.setText(info.getDate());
                viewHolder.txClassNo.setText(info.getClassNo());
                viewHolder.txTeacher.setText(info.getTeacher());
            }
            return v;
        }
    }
    //---------------------------------------------------------------------


    //--------------------------------------------------------------
    protected void requestLesson()
    {
        String url ="http:";       // json 받아올 url

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mResult = response;
                        drawList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CheckActivity.this, "서버 에러", Toast.LENGTH_LONG).show();
                    }
                }
        );
        jsObjRequest.setTag(LESSONTAG);
        mQueue.add(jsObjRequest);
    }
    //--------------------------------------------------------------
    public void drawList() {
        mArray.clear();
        try {
            JSONArray jsonMainNode = mResult.getJSONArray("list");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                JSONObject jsonLessonNode = jsonChildNode.getJSONArray("lesson").getJSONObject(0);
                String name = jsonLessonNode.getString("LessonName");
                String classNo = jsonLessonNode.getString("ClassNo");
                String teacher = jsonLessonNode.getString("Teacher");

                String gmt = jsonChildNode.getString("LessonDate");
                Date local = new Date(Long.parseLong(gmt) * 1000);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(local);
                Log.i("Lesson", " " + name + "(" + classNo + ")");

                mArray.add(new LessonInfo(name, date, classNo, teacher));
            }
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
            mResult = null;
        }

        mAdapter.notifyDataSetChanged();
    }
}
