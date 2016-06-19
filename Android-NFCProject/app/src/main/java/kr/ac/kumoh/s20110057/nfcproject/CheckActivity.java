package kr.ac.kumoh.s20110057.nfcproject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.provider.Settings;
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
import android.widget.Button;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;

public class CheckActivity extends AppCompatActivity {

    public static final String LESSONTAG = "LessonTag";
    protected String id = null;
    protected String lessonName = null;
    protected String LessonTime =null;

    protected JSONArray mResult = null;
    protected JSONObject checkResult = null;
    protected JSONObject checkObject = null;

    protected ArrayList<LessonInfo> mArray = new ArrayList<LessonInfo>();
    protected ListView mList;
    protected LessonAdapter mAdapter;
    protected RequestQueue mQueue = null;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private String dayOfWeek[] = {"" , "일","월","화","수","목","금","토"};
    private String classtime[] = {"1","2","3","4","5","6","7","8","9","A","B","C","D"};
    private int day;
    private int hour;

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_URI = 2;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        Intent nfcIntent = new Intent(this, getClass());
        nfcIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd,HH:mm").format(Calendar.getInstance().getTime());
        day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        hour = Integer.parseInt(timeStamp.split(",")[1].split(":")[0]);

        try {
            ndef.addDataType("*/*");
            mFilters = new IntentFilter[]{ndef};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[]{ndef,};
        mTechLists = new String[][]{new String[]{NfcF.class.getName()}};
        intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                proceccTag(intent);
            }
        }

        mAdapter = new LessonAdapter(this, R.layout.lesson_item);
        mList = (ListView) findViewById(R.id.lesson_list);
        mList.setAdapter(mAdapter);

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
        Network network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);

        mQueue.start();


        requestLesson();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void proceccTag(Intent passedIntent) {
        Parcelable[] rawMsgs = passedIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs == null) {
            return;
        }
        NdefMessage[] msgs;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Check Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://kr.ac.kumoh.s20110057.nfcproject/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        if (mQueue != null) {
            mQueue.cancelAll(LESSONTAG);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
            onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        String tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG).toString();
        String strMsg = action + "\n\n" + tag;
        Toast.makeText(CheckActivity.this, strMsg, Toast.LENGTH_LONG);
        Parcelable[] messages = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (messages == null) return;

        for (int i = 0; i < messages.length; i++) {
            showMsg((NdefMessage) messages[i]);
        }
    }

    // 버퍼 데이터를 디코딩해서 String 으로 변환
    public String byteDecoding(byte[] buf) {
        String strText = "";
        String textEncoding = ((buf[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int langCodeLen = buf[0] & 0077;

        try {
            strText = new String(buf, langCodeLen + 1,
                    buf.length - langCodeLen - 1, textEncoding);
        } catch (Exception e) {
            Log.d("tag1", e.toString());
        }
        return strText;
    }

    public void showMsg(NdefMessage mMessage) {
        String strRec = "";
        // NDEF 메시지에서 NDEF 레코드 배열을 구한다
        NdefRecord[] recs = mMessage.getRecords();
        for (int i = 0; i < recs.length; i++) {
            // 개별 레코드 데이터를 구한다
            NdefRecord record = recs[i];
            byte[] payload = record.getPayload();
            // 레코드 데이터 종류가 텍스트 일때
            if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                // 버퍼 데이터를 인코딩 변환
                strRec = byteDecoding(payload);
            }
            // 레코드 데이터 종류가 URI 일때
            else if (Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                strRec = new String(payload, 0, payload.length);
            }
        }
        StringTokenizer tokenizer = new StringTokenizer(strRec);
        String placeNo, seatX, seatY;
        placeNo = tokenizer.nextToken("\n");
        seatX = tokenizer.nextToken("\n");
        seatY = tokenizer.nextToken("\n");
        NFCTag(placeNo, seatX, seatY);
    }

    public void NFCTag(String placeNo, String seatX, String seatY) {


        String url = null;
        try {
            if(checkObject.getString("placeNo").equals(placeNo)) {
                url = "http://172.20.10.3:8080/project/checkLesson?id=" + id
                        + "&lessonCode=" + checkObject.getString("lessonCode") + "&classNo=" + checkObject.getString("classNo") +
                        "&placeNo=" + placeNo + "&seatX=" + seatX + "&seatY=" + seatY;
            }
            else{
                Toast.makeText(CheckActivity.this,"해당 수업의 강의실이 아닙니다.", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                checkResult = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckActivity.this, "서버 에러", Toast.LENGTH_LONG);
            }
        });
        try {
            if(checkResult.getString("checkResult").equals("true")){
                Toast.makeText(CheckActivity.this,"출석 체크 성공", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(CheckActivity.this,"NFC tag", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Check Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://kr.ac.kumoh.s20110057.nfcproject/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
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
            final LessonViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.lesson_item, parent, false);

                viewHolder = new LessonViewHolder();
                viewHolder.txName = (TextView) v.findViewById(R.id.name);
                viewHolder.txCode = (TextView) v.findViewById(R.id.lesson_code);
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
            Button button = (Button)v.findViewById(R.id.check_btn);
            button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent lessonIntent = new Intent(CheckActivity.this,LessonActivity.class);
                    lessonIntent.putExtra("id",id);
                    lessonIntent.putExtra("name",lessonName);
                    lessonIntent.putExtra("lessonCode",viewHolder.txCode.getText().toString());
                    lessonIntent.putExtra("classNo",viewHolder.txClassNo.getText().toString());
                    startActivity(lessonIntent);
                }
            });
            return v;
        }
    }
    //---------------------------------------------------------------------

    //--------------------------------------------------------------
    protected void requestLesson() {
        String url = "http://172.20.10.3:8080/project/getLessonList?id=" + id;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mResult = response;
                drawList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckActivity.this, "서버 에러", Toast.LENGTH_LONG);
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
                LessonTime = jsonChildNode.getString("lessonTime");
                lessonName=name;

                if(LessonTime.contains(dayOfWeek[day] + classtime[hour - 9].charAt(0))){
                    checkObject = jsonChildNode;
                }
                if(LessonTime.contains(dayOfWeek[day] + classtime[hour - 8].charAt(0))){
                    checkObject = jsonChildNode;
                }
                /*String gmt = jsonChildNode.getString("LessonDate");
                Date local = new Date(Long.parseLong(gmt) * 1000);
                String date = new SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.KOREA).format(local);
                Log.i("Lesson", " " + name + "(" + classNo + ")");*/

                mArray.add(new LessonInfo(name, code, classNo, teacher));
            }
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Error" + e.toString(), Toast.LENGTH_LONG).show();
            mResult = null;
        }


        mAdapter.notifyDataSetChanged();
    }

}