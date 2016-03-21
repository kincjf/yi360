package com.telnetproject.pastelplus.telnetproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telnetproject.pastelplus.telnetproject.connect.GetParam_Thread;
import com.telnetproject.pastelplus.telnetproject.model.JsonParsingContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;



public class MainActivity extends Activity {
    public static int REQUEST_OPEN = 0;
    public static int REQUEST_HISTORY = 1;
    final Context context = this;
    private boolean mIsConnected = false;
    Handler mHandler = null;
    Socket mSocket = null;
    String arrParamResult[];  //처음 엑세스토큰 호출시 request 값 받아오기 위한 배열; 원래는 다받아오려 했지만 디바이스 구별 불가능으로 한개만 받음
    String mTorkenKey = ""; // 토큰값 저장할 변수
    boolean isGetTorkenkeyFirst = true; //토큰값은 처음  한번만 받아옴
    boolean isRecoding = false; //현재 동영상 촬열중인지 확인하는 함수


    Socket arrSocket[];
    public final String PORT = "7878";
    public final String ACCESS = "{\"msg_id\":257,\"token\":0}";
    public final String PHOTOCATURE = "{\"msg_id\":769,\"token\":1}";

    public String mResultJson = "";
    Context mContext;
    JsonParsingContainer mJsonContainer;
    /**
     * Called when the activity is first created.
     */

    TextView txtStatus1;
    TextView txtStatus2;
    TextView txtStatus3;
    TextView txtStatus4;
    TextView txtStatus5;
    TextView txtStatus6;


    ClientThread clientThread1;
    ClientThread clientThread2;
    ClientThread clientThread3;
    ClientThread clientThread4;
    ClientThread clientThread5;
    ClientThread clientThread6;

    ClientThread clientThread[];
    Thread thread[];

    ////디바이스 연결시 밑의 값만 바꿔주면 연결됨
    String arrHostIp[] = {"192.168.43.98", "192.168.43.97", "192.168.43.96", "192.168.43.95", "192.168.43.94", "192.168.43.93"}; //디바이스 아이피
    public static final int DEVICENUMBER = 6; //디바이스 연결갯수

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiconnect_activity);
        mContext = this;
        this.mHandler = new Handler();

        mJsonContainer = new JsonParsingContainer();
        arrParamResult = new String[6];
        txtStatus1 = (TextView) findViewById(R.id.txtStatus1);
        txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
        txtStatus3 = (TextView) findViewById(R.id.txtStatus3);
        txtStatus4 = (TextView) findViewById(R.id.txtStatus4);
        txtStatus5 = (TextView) findViewById(R.id.txtStatus5);
        txtStatus6 = (TextView) findViewById(R.id.txtStatus6);

    }

    public void mOnClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnConnection:
                initThread(DEVICENUMBER);
                break;

            case R.id.btnAccessTorken:
                PostCommand("1", DEVICENUMBER);
                break;

            case R.id.btnCapture:
                PostCommand("5", DEVICENUMBER);
                break;

            case R.id.btnRecodeStart:
                isRecoding = true;
                PostCommand("3", DEVICENUMBER);
                break;

            case R.id.btnRecodeStop:
                PostCommand("4", DEVICENUMBER);
                break;

        }
    }

    void initThread(int n) //카메라 연결 갯수만큼 쓰레드 생성
    {
        Log.e("mylog", "initThread");
        clientThread1 = new ClientThread();
        clientThread2 = new ClientThread();
        clientThread3 = new ClientThread();
        clientThread4 = new ClientThread();
        clientThread5 = new ClientThread();
        clientThread6 = new ClientThread();

        clientThread = new ClientThread[6];
        clientThread[0] = clientThread1;
        clientThread[1] = clientThread2;
        clientThread[2] = clientThread3;
        clientThread[3] = clientThread4;
        clientThread[4] = clientThread5;
        clientThread[5] = clientThread6;
        arrSocket = new Socket[6];
        arrSocket[0] = new Socket();
        arrSocket[1] = new Socket();
        arrSocket[2] = new Socket();
        arrSocket[3] = new Socket();
        arrSocket[4] = new Socket();
        arrSocket[5] = new Socket();

        for (int i = 0; i < n; ++i) {
            clientThread[i].initVal(arrHostIp[i], i);
        }
        startConnect(n);
    }

    void startConnect(int n) //연결 시작
    {
        Log.e("mylog", "startConnect");
        thread = new Thread[6];
        thread[0] = new Thread(clientThread[0]);
        thread[1] = new Thread(clientThread[1]);
        thread[2] = new Thread(clientThread[2]);
        thread[3] = new Thread(clientThread[3]);
        thread[4] = new Thread(clientThread[4]);
        thread[5] = new Thread(clientThread[5]);
        for (int i = 0; i < n; ++i)
        {
            Log.e("mylog", "thread[" + i + "].start()");
            thread[i].setDaemon(true);
            thread[i].start();
        }
    }

    private void PostCommand(String strcommand, int n)
    {

        if (mSocket != null && mIsConnected)
        {
            // EditText edCmdText = (EditText) findViewById(R.id.editCmdText);
            //String strCommand = edCmdText.getText().toString();
            String strCommand = strcommand;
            if (strCommand.equals("0"))
            {

            } else if (strCommand.equals("1"))
            {
                strCommand = ACCESS;
            } else if (strCommand.equals("2"))
            {
                strCommand = PHOTOCATURE;
            } else if (strCommand.equals("3"))
            {
                strCommand = "{\"msg_id\":513,\"token\":" + mTorkenKey + "}";
            } else if (strCommand.equals("4"))
            {
                strCommand = "{\"msg_id\":514,\"token\":" + mTorkenKey + "}";
            } else if (strCommand.equals("5"))
            {
                strCommand = "{\"msg_id\":769,\"token\":" + mTorkenKey + "}";
            }


            for (int i = 0; i < n; i++)
            {
                try
                {
                    OutputStream streamOutput = arrSocket[i].getOutputStream();

                    strCommand += "\n";
                    try
                    {
                        byte[] arrayOutput = strCommand.getBytes("ASCII");
                        int nLen = arrayOutput.length;
                        streamOutput.write(arrayOutput, 0, nLen);
                        String byteToString = new String(arrayOutput, 0, arrayOutput.length);
                        Log.e("mylog", "strCommand : " + byteToString);
                    } catch (Exception e0)
                    {
                        Handler handlerException = MainActivity.this.mHandler;
                        final String strMessage = "Error while sending to server:\r\n" + e0.getMessage();
                        Runnable rExceptionThread = new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
                            }
                        };
                        handlerException.post(rExceptionThread);
                    }
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }

        } else
        {
            Toast.makeText(context, "Please connect to the server first", Toast.LENGTH_SHORT).show();
        }
    }


    protected void onDestroy()
    {
        super.onDestroy();

        if (mIsConnected)
        {
            mIsConnected = false;

            try {
                for (int i = 0; i < 6; ++i)
                {
                    if (thread[i] != null)
                    {
                        Thread threadHelper = thread[i];
                        thread[i] = null;
                        threadHelper.interrupt();
                    }
                }

            } catch (Exception e1)
            {
            }
        }
    }

    public class ClientThread implements Runnable
    {
        String strHost = "";
        int threadNum = 0;

        public void initVal(String host, int threadnum)
        {
            strHost = host;
            threadNum = threadnum;
        }

        public void run() {
            Log.e("mylog", "Thread run strHost: " + strHost);
            Log.e("mylog", "Thread run : " + threadNum);
            int nPort = 23;
            try {
                nPort = Integer.parseInt(PORT);
            } catch (NumberFormatException nfe) {
                nPort = 23;
            }

            try {
                arrSocket[threadNum] = new Socket(strHost, nPort);
                //Socket socket = new Socket(strHost, nPort);
                mSocket = arrSocket[threadNum];
                InputStream streamInput = arrSocket[threadNum].getInputStream();
                mIsConnected = true;

                byte[] arrayOfByte = new byte[10000];
                while (mIsConnected)
                {
                    Log.e("mylog", " while (mIsConnected) " + mIsConnected);
                    int j = 0;
                    try
                    {
                        int i = arrayOfByte.length;
                        j = streamInput.read(arrayOfByte, 0, i);
                        if (j == -1)
                        {
                            throw new Exception("Error while reading socket.");
                        }
                    } catch (Exception e0)
                    {
                        Handler handlerException = MainActivity.this.mHandler;
                        String strException = e0.getMessage();
                        final String strMessage = "Error while receiving from server:\r\n" + strException;
                        Runnable rExceptionThread = new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
                            }
                        };

                        handlerException.post(rExceptionThread);

                        if (strException.indexOf("reset") != -1 || strException.indexOf("rejected") != -1)
                        {
                            mIsConnected = false;
                            try {
                                Log.e("mylog", "connected false : " + threadNum);
                                arrSocket[threadNum].close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            arrSocket[threadNum] = null;
                            break;
                        }
                    }

                    if (j == 0)
                        continue;

                    final String strData = new String(arrayOfByte, 0, j).replace("\r", "");  ///리퀘스트 값
                    setmResultJson(strData);
                    setarrParamResult(strData, threadNum);
                    //Json값 받아오기
                    GetParam_Thread getParam_Thread = new GetParam_Thread(mCheckGcmId, mJsonContainer, MainActivity.this, MainActivity.this, threadNum);
                    getParam_Thread.setDaemon(true);
                    getParam_Thread.start();

                    Log.e("mylog", "" + strData);



                    Handler localHandler2 = MainActivity.this.mHandler;

                    Runnable local2 = new Runnable() {
                        public void run() {
                            Log.e("mylog", "connected : " + threadNum);
                            if (threadNum == 0) {
                                txtStatus1.setText("Connected");
                            } else if (threadNum == 1) {
                                txtStatus2.setText("Connected");
                            } else if (threadNum == 2) {
                                txtStatus3.setText("Connected");
                            } else if (threadNum == 3) {
                                txtStatus4.setText("Connected");
                            } else if (threadNum == 4) {
                                txtStatus5.setText("Connected");
                            } else if (threadNum == 5) {
                                txtStatus6.setText("Connected");
                            } else {
                            }

                            //추후 커멘트 창 보여지고 싶으면 주석 풀고 다시 사용하면 됨

//                            StringBuilder localStringBuilder1 = new StringBuilder();
//                            CharSequence localCharSequence = MainActivity.this.mTextViewContent.getText();
//                            localStringBuilder1.append(localCharSequence);
//                            localStringBuilder1.append(strData);
//                            MainActivity.this.mTextViewContent.setText(localStringBuilder1.toString());
//                            MainActivity.this.mScrollViewContent.requestLayout();
//
//                            Handler localHandler = MainActivity.this.mHandler;
//                            Runnable local1 = new Runnable()
//                            {
//                                public void run()
//                                {
//                                    ScrollView localScrollView = MainActivity.this.mScrollViewContent;
//                                    int i = MainActivity.this.mTextViewContent.getHeight();
//                                    localScrollView.smoothScrollTo(0, i);
//                                }
//                            };
//
//                            localHandler.post(local1);

                        }
                    };

                    localHandler2.post(local2);
                }

                arrSocket[threadNum].close();
                //mSocket = null;
            } catch (Exception e0) {
                mIsConnected = false;

                Handler handlerException = MainActivity.this.mHandler;
                String strException = e0.getMessage();
                if (strException == null)
                    strException = "Connection closed";
                else
                    strException = "Cannot connect to the server:\r\n" + strException;

                final String strMessage = strException;
                Runnable rExceptionThread = new Runnable() {
                    public void run() {
                        Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
                    }
                };

                handlerException.post(rExceptionThread);
            }
        }
    }


    public String getmResultJson() {
        return mResultJson;
    }

    public void setmResultJson(String result) {
        mResultJson = result;
    }

    public void initmResultJson() {
        mResultJson = "";
    }

    public void initarrParamResult() {
        for (int i = 0; i < arrParamResult.length; i++) {
            arrParamResult[i] = "";
        }
    }

    public void setarrParamResult(String str, int i) {
        arrParamResult[i] = str;
    }

    public String getarrParamResult(int i) {
        return arrParamResult[i];
    }


    Handler mCheckGcmId = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://연결 시작
                    if (!isFinishing()) {
                    }
                    break;
                case 1://연결 실패
                    if (!isFinishing()) {
                        // Toast.makeText(mContext, "서버연결에 실패하였습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2://Json받는중
                    break;
                case 3://Json에러
                    if (!isFinishing()) {
                        Toast.makeText(mContext, "네트워크상태를 확인해 주세요", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 4:// 결과
                    if (!isFinishing()) {
                        //Log.e("mylog", "msg_id : " + mJsonContainer.mCheckConnect.mParam);
                        if (mJsonContainer.mCheckConnect.mMsg_id.equals("257")) {
                            if (isGetTorkenkeyFirst) {
                                Log.e("mylog", "(mJsonContainer.mCheckConnect.mParam = " + mJsonContainer.mCheckConnect.mParam);
                                mTorkenKey = mJsonContainer.mCheckConnect.mParam;
                                isGetTorkenkeyFirst = false;
                            }

                        }

                    }
                    break;
            }
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        switch (keyCode)
        {
            //하드웨어 뒤로가기 버튼에 따른 이벤트 설정
            case KeyEvent.KEYCODE_BACK:

                new AlertDialog.Builder(mContext)
                        .setTitle("프로그램 종료")
                        .setMessage("프로그램을 종료 하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // 프로세스 종료.
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        })
                        .setNegativeButton("아니오", null)
                        .show();

                break;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}