package jp.techacademy.shuuhei.sutou.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;
import android.support.design.widget.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;
    int count = 0;
    private int mGenre;
    private Button button;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()){
                if(answerUid.equals(answer.getAnswerUid())){
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();;
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        button = (Button)findViewById(R.id.likeButton);


 /*       else if((user != null) && ((count%2) == 0)) {
            button.setText("登録済み");
            count += 1;

        }else if((user != null) && ((count%2) != 0)){ */




            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 渡ってきたジャンルの番号を保持する
                    Bundle extras = getIntent().getExtras();
                    int mGenre = extras.getInt("genre");
                    TextView mBodyText = (TextView) findViewById(R.id.bodyTextView);
                    ImageView mImageView = (ImageView) findViewById(R.id.imageView);
                    mImageView.setOnClickListener(this);

                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference genreRef = dataBaseReference.child(Const.FavoritesPATH).child(String.valueOf(mGenre));

                    Map<String, String> data = new HashMap<String, String>();

                    // UID
                    data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // 本文を取得する
                    String body = mBodyText.getText().toString();

   //                 String like = button.getText().toString();

                    // Preferenceから名前を取る
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String name = sp.getString(Const.NameKEY, "");

                    data.put("body", body);
                    data.put("name", name);
           //         data.put("like", like);

                    // 添付画像を取得する
                    BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    if (drawable != null) {
                        Bitmap bitmap = drawable.getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                        data.put("image", bitmapString);
                    }

                    genreRef.push().setValue(data, new DatabaseReference.CompletionListener(){
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference){
                            if (databaseError == null) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user == null) {
                                    button.setVisibility(View.GONE);

                                }
                                else {
                                    button.setVisibility(View.VISIBLE);
                                    button.setText("登録済み");
                                    finish();
                                }
                            } else {
                                Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
                            }

                        }
                    });

                }
            });


        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user == null){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}
