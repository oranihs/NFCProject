package kr.ac.kumoh.s20110057.nfcproject;

import android.content.Context;
import android.content.Intent;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class CheckActivity extends AppCompatActivity {

    public static final String LESSONTAG = "LessonTag";
    protected String id=null;

    protected JSONArray mResult = null;

    protected ArrayList<LessonInfo> mArray = new ArrayList<LessonInfo>();
    protected ListView mList;
    protected LessonAdapter mAdapter;
    protected RequestQueue mQueue = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

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

    public void NFCTag(){

    }
    //---------------------------------------------------------------------
    public class LessonInfo {
        String name;
        String code;
        String classNo;
        String teacher;

        public LessonInfo(String name, String code, String classNo,
                          String teacher) {
            super();
            this.name = name;
            this.code = code;
            this.classNo = classNo;
            this.teacher = teacher;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
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
        TextView txCode;
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
                viewHolder.txCode = (TextView) v.findViewById(R.id.date);
                viewHolder.txClassNo = (TextView) v.findViewById(R.id.class_number);
                viewHolder.txTeacher = (TextView) v.findViewById(R.id.teacher);

                v.setTag(viewHolder);
            } else {
                viewHolder = (LessonViewHolder) v.getTag();
            }

            LessonInfo info = mArray.get(position);
            if (info != null) {
                viewHolder.txName.setText(info.getName());
                viewHolder.txCode.setText(info.getCode());
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
        String url="http://172.20.10.3:8080/project/getLessonList?id=" + id;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
                 @Override
                 public void onResponse(JSONArray response) {
                        mResult=response;
                        drawList();
                     }
             }, new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError error) {
                     Toast.makeText(CheckActivity.this,"서버 에러", Toast.LENGTH_LONG);
                     }
             });
        jsonArrayRequest.setTag(LESSONTAG);
        mQueue.add(jsonArrayRequest);
    }
    //--------------------------------------------------------------
    public void drawList() {
        mArray.clear();
        try {
            for (int i = 0; i < mResult.length(); i++) {
                JSONObject jsonChildNode = mResult.getJSONObject(i);
                String name = jsonChildNode.getString("lessonName");
                String code = jsonChildNode.getString("lessonCode");
                String classNo = jsonChildNode.getString("classNo");
                String teacher = jsonChildNode.getString("teacher");

                /*String gmt = jsonChildNode.getString("LessonDate");
                Date local = new Date(Long.parseLong(gmt) * 1000);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(local);
                Log.i("Lesson", " " + name + "(" + classNo + ")");*/

                mArray.add(new LessonInfo(name, code, classNo, teacher));
            }
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Error" + e.toString(),Toast.LENGTH_LONG).show();
            mResult = null;
        }


        mAdapter.notifyDataSetChanged();
    }
}