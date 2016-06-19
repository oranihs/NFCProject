package kr.ac.kumoh.s20110057.nfcproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LessonActivity extends AppCompatActivity {

    public static final String CHECKTAG = "CheckTag";

    protected JSONArray mResult = null;

    protected ArrayList<LessonInfo> mArray = new ArrayList<LessonInfo>();
    protected ListView mList;
    protected LessonAdapter mAdapter;
    protected RequestQueue mQueue = null;

    String id,lessonName,lessonCode,classNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        lessonName = intent.getStringExtra("name");
        lessonCode = intent.getStringExtra("lessonCode");
        classNo = intent.getStringExtra("classNo");

        mAdapter = new LessonAdapter(this, R.layout.check_item);
        mList = (ListView)findViewById(R.id.check_list);
        mList.setAdapter(mAdapter);

        Cache cache = new DiskBasedCache(getCacheDir(),1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();


        requestCheck();
    }
    protected void requestCheck() {
        String url = "http://172.20.10.3:8080/project/getCheckList?id=" + id + "&lessonCode=" + lessonCode + "&classNo=" + classNo;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mResult = response;
                drawList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LessonActivity.this, "서버 에러", Toast.LENGTH_LONG);
            }
        });
        jsonArrayRequest.setTag(CHECKTAG);
        mQueue.add(jsonArrayRequest);
    }
    public void drawList() {
        mArray.clear();
        try {
            for (int i = 0; i < mResult.length(); i++) {
                JSONObject jsonChildNode = mResult.getJSONObject(i);
                String name = null;
                String classNo = jsonChildNode.getString("classNo");
                int tmp = jsonChildNode.getInt("status");
                String status = null;
                switch (tmp){
                    case 0:
                        status = "결석";
                        break;
                    case 1:
                        status = "출석";
                        break;
                    case 2:
                        status = "지각";
                        break;
                }

                String gmt = jsonChildNode.getString("LessonDate");
                Date local = new Date(Long.parseLong(gmt) * 1000);
                String date = new SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.KOREA).format(local);
                Log.i("Lesson", " " + name + "(" + classNo + ")");

                mArray.add(new LessonInfo(name, date, status));
            }
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Error" + e.toString(), Toast.LENGTH_LONG).show();
            mResult = null;
        }

        mAdapter.notifyDataSetChanged();
    }
    public class LessonInfo {
        String code;
        String date;
        String status;

        public LessonInfo(String code, String date, String status) {
            super();
            this.code = code;
            this.date = date;
            this.status = status;
        }


        public String getCode() {
            return code;
        }

        public String getDate() {
            return date;
        }
        public String getStatus() {
            return status;
        }

    }

    //---------------------------------------------------------------------
    static class LessonViewHolder {
        TextView txName;
        TextView txDate;
        TextView txStatus;
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
            final LessonViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.lesson_item, parent, false);

                viewHolder = new LessonViewHolder();
                viewHolder.txName = (TextView) v.findViewById(R.id.lesson_name);
                viewHolder.txDate = (TextView) v.findViewById(R.id.date);
                viewHolder.txStatus = (TextView) v.findViewById(R.id.lesson_check);

                v.setTag(viewHolder);
            } else {
                viewHolder = (LessonViewHolder) v.getTag();
            }

            LessonInfo info = mArray.get(position);
            if (info != null) {
                viewHolder.txName.setText(info.getCode());
                viewHolder.txDate.setText(info.getDate());
                viewHolder.txStatus.setText(info.getStatus());
            }
            return v;
        }
    }
    //---------------------------------------------------------------------

}
