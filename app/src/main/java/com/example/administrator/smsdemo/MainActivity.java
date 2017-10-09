package com.example.administrator.smsdemo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
public class MainActivity extends Activity implements View.OnClickListener {
   private EditText etphone; // 手机号输入框
    private EditText etcode; // 验证码输入框
    private Button btnrequest; // 获取验证码按钮
   private Button btnsub; // 注册按钮
    int i = 30; // 设置短信验证提示时间为30s
   @Override
  protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               requestWindowFeature(Window.FEATURE_NO_TITLE);// 除去标题栏
               setContentView(R.layout.activity_main);
                etphone = (EditText) findViewById(R.id.etPhoneNumber);
                etcode = (EditText) findViewById(R.id.etCode);
                btnrequest = (Button) findViewById(R.id.btnSendMsg);
                btnsub = (Button) findViewById(R.id.btnSubmitCode);
                btnrequest.setOnClickListener(this);
                btnsub.setOnClickListener(this);
                SMSSDK.initSDK(this, "2178595f78570 ",
                         "9c3aca996a0a4a52a89b58d8a0d78ac7");
                 EventHandler eventHandler = new EventHandler() {
           @Override
           public void afterEvent(int event, int result, Object data) {
                        Message msg = new Message();
                             msg.arg1 = event;
                              msg.arg2 = result;
                              msg.obj = data;
                            handler.sendMessage(msg);
                          }
      };
           SMSSDK.registerEventHandler(eventHandler); // 注册回调监听接口
         }

       @Override
     public void onClick(View v) {
           String phoneNums = etphone.getText().toString();
           switch (v.getId()) {
                case R.id.btnSendMsg:
                       if (!judgePhoneNums(phoneNums)) {// 判断输入号码是否正确
                              return;
                           }
                       SMSSDK.getVerificationCode("86", phoneNums); // 调用sdk发送短信验证
                         btnrequest.setClickable(false);// 设置按钮不可点击 显示倒计时
                         btnrequest.setText("重新发送(" + i + ")");
                        new Thread(new Runnable() {
             @Override
               public void run() {
                                        for (i = 30; i > 0; i--) {
                                                  handler.sendEmptyMessage(-9);
                                                if (i <= 0) {
                                                     break;
                                                   }
                                               try {
                                                         Thread.sleep(1000);// 线程休眠实现读秒功能
                                                   } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                          }
                                       handler.sendEmptyMessage(-8);// 在30秒后重新显示为获取验证码
                                    }
                                  }).start();

               case R.id.btnSubmitCode:
                      SMSSDK.submitVerificationCode("86", phoneNums, etcode.getText()
                                .toString());
                        createProgressBar();
                      break;
                }
          }
         Handler handler = new Handler() {
     public void handleMessage(Message msg) {
                    if (msg.what == -9) {
                           btnrequest.setText("重新发送(" + i + ")");
                         } else if (msg.what == -8) {
                              btnrequest.setText("获取验证码");
                            btnrequest.setClickable(true); // 设置可点击
                             i = 30;
                      } else {
                           int event = msg.arg1;
                             int result = msg.arg2;
                             Object data = msg.obj;
                              if (result == SMSSDK.RESULT_COMPLETE) {
                                     // 短信注册成功后，返回MainActivity,然后提示
                                        if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                                                Toast.makeText(getApplicationContext(), "提交验证码成功",
                                                                Toast.LENGTH_SHORT).show();
                                              // 验证成功后跳转主界面
                                              //此处可注释掉
                                               Intent intent = new Intent(MainActivity.this, reg.class);
                                              intent.putExtra("flag", "newuser");
                                              startActivity(intent);
                                             Log.e("注册", "成功！");
                                            finish();// 成功跳转之后销毁当前页面

                                        } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                                              Toast.makeText(getApplicationContext(), "验证码已经发送",
                                                            Toast.LENGTH_SHORT).show();
                                       } else {
                                            ((Throwable) data).printStackTrace();
                                        }
                               }
                         }
                    }
   };

            private boolean judgePhoneNums(String phoneNums) {
              if (isMatchLength(phoneNums, 11) && isMobileNO(phoneNums)) {
                      return true;
                  }
             Toast.makeText(this, "手机号码输入有误！", Toast.LENGTH_SHORT).show();
              return false;
         }

          public static boolean isMatchLength(String str, int length) {
              if (str.isEmpty()) {
                      return false;
                   } else {
                    return str.length() == length ? true : false;
                 }
           }
   public static boolean isMobileNO(String mobileNums) {
             String telRegex = "[1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
              if (TextUtils.isEmpty(mobileNums))
                    return false;
               else
               return mobileNums.matches(telRegex);
         }

           private void createProgressBar() {
              FrameLayout layout = (FrameLayout) findViewById(android.R.id.content);
              FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                      FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
               layoutParams.gravity = Gravity.CENTER;
                ProgressBar mProBar = new ProgressBar(this);
              mProBar.setLayoutParams(layoutParams);
             mProBar.setVisibility(View.VISIBLE);
                layout.addView(mProBar);
          }

          @Override
  protected void onDestroy() {
            SMSSDK.unregisterAllEventHandler();
            super.onDestroy();
     }
}

