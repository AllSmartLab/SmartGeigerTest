package kr.ftlab.samples.smartgeigertest;

import kr.ftlab.lib.SmartSensor;
import kr.ftlab.lib.SmartSensorEventListener;
import kr.ftlab.lib.SmartSensorResultGE;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SmartSensorEventListener {
    private SmartSensor mMI ;
    private SmartSensorResultGE mResultGE;

    private Button btnStart;
    private TextView txtResultCPM;
    private TextView txtResultSV;

    int mProcess_Status = 0;
    int Process_Stop = 0;
    int Process_Start = 1;

    BroadcastReceiver mHeadSetConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG)) { // 이어폰 단자에 센서 결함 유무 확인
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {//센서 분리 시
                        stopSensing();
                        btnStart.setEnabled(false);//센서가 분리되면 START/STOP 버튼 비활성화, 클릭 불가
                        Toast.makeText(MainActivity.this,"Sensor not found", Toast.LENGTH_SHORT).show();
                    } else if (intent.getIntExtra("state", 0) == 1) {//센서 결합 시
                        Toast.makeText(MainActivity.this,"Sensor find", Toast.LENGTH_SHORT).show();
                        btnStart.setEnabled(true);//센서가 연결되면 START/STOP 버튼 활성화
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 앱이 전면에서 실행되는 동안 화면이 꺼지지 않도록 해줍니다 .

        btnStart = (Button) findViewById(R.id.button_on); // 레이아웃에 정의했던 버튼을 객체로서 참조할 수 있도록 참조변수에 저장합니다.
        txtResultCPM = (TextView) findViewById(R.id.textresultcpm); // 레이아웃에 정의했던 텍스트뷰(CPM 측정값을 위한 텍스트뷰)를 객체로서 참조할 수 있도록 참조변수에 저장합니다.
        txtResultSV = (TextView) findViewById(R.id.textresultsv); // 레이아웃에 정의했던 텍스트뷰(µSv/h 측정값을 위한 텍스트뷰)를 객체로서 참조할 수 있도록 참조변수에 저장합니다.

        mMI = new SmartSensor(MainActivity.this, this);
        mMI.selectDevice(SmartSensor.GE);
    }

    public void mOnClick(View v) {
        if (mProcess_Status == Process_Start) {//버튼 클릭 시의 상태가 Start 이면 stop process 수행
            stopSensing();
        }
        else {//버튼 클릭 시의 상태가 Stop 이면 start process 수행
            startSensing();
        }
    }

    public void startSensing()
    {
        btnStart.setText("STOP");
        mProcess_Status = Process_Start; //현재 상태를 start로 설정
        mMI.start();//측정 시작
    }

    public void stopSensing()
    {
        btnStart.setText("START");
        mProcess_Status = Process_Stop; //현재 상태를 stop로 설정
        mMI.stop();//측정 종료
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//상단 메뉴 버튼 생성
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_calibration) {
            mMI.registerSelfConfiguration(); //보정값 초기화
            mMI.start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intflt = new IntentFilter();
        intflt.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mHeadSetConnectReceiver, intflt);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mHeadSetConnectReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {//어플 종료 시 수행
        mMI.quit();
        finish();
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public void onMeasured() {//센서로 부터 데이터를 받을 경우 호출
        String str ="";
        mResultGE = mMI.getResultGE();//측정된 값을 가져옴

        str = String.format("%1.0f", mResultGE.GE_CPM);
        txtResultCPM.setText(str);
        str = String.format("%1.0f", mResultGE.GE_uSv);
        txtResultSV.setText(str);
    }

    @Override
    public void onSelfConfigurated() { //어플을 설치하고 최초 실행시에만 보정 진행, 보정이 끝난 후 호출, 측정 시작 상태
        mProcess_Status = 0;
        btnStart.setText("START");
    }
}