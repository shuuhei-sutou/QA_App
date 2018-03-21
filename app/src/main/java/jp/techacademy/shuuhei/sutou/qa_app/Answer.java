package jp.techacademy.shuuhei.sutou.qa_app;

/**
 * Created by user on 2018/03/18.
 */
import java.io.Serializable;

public class Answer implements Serializable{
    private String mBody;
    private String mName;
    private String mUid;
    private String mAnswerUid;

    public Answer(String body, String name, String uid, String answerUid){
        mBody = body;
        mName = name;
        mUid = uid;
        mAnswerUid = answerUid;
    }

    public String getBody(){
        return mBody;
    }

    public String getName(){
        return mName;
    }

    public String getUid(){
        return mUid;
    }

    public String getAnswerUid(){
        return mAnswerUid;
    }
}
