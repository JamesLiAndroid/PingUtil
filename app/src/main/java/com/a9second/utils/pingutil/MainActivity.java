package com.a9second.utils.pingutil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et_address)
    EditText etAddress;
    @BindView(R.id.et_count)
    EditText etCount;
    @BindView(R.id.et_timeout)
    EditText etTimeout;
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.lv_network_test)
    ListView lvNetworkTest;

    List<String> pingResults = new ArrayList<>();
    static ArrayAdapter<String> adapter;

    boolean isRunning = false;
    static Thread thread;

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                ArrayList<String> content = msg.getData().getStringArrayList("result");
                adapter.addAll(content);
                adapter.notifyDataSetChanged();
            }
            if (msg.what == 0x124) {
                adapter.add("测试完成！！！");
                adapter.notifyDataSetChanged();
                // Toast.makeText(getApplicationContext(), "测试完成！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initListView();
    }

    private void initListView() {
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, pingResults);
        lvNetworkTest.setAdapter(adapter);
    }

    @OnClick(R.id.btn_test)
    public void onViewClicked() {
        adapter.clear();
        adapter.notifyDataSetChanged();

        final String address = etAddress.getText().toString().trim();
        final String count = etCount.getText().toString();
        String timeout = etTimeout.getText().toString();

        if (address == null || "".equals(address)) {
            Toast.makeText(MainActivity.this, "未输入地址", Toast.LENGTH_SHORT).show();
        }
        if (count == null || "".equals(count)) {
            Toast.makeText(MainActivity.this, "未输入执行次数", Toast.LENGTH_SHORT).show();
        }
        if (timeout == null || "".equals(timeout)) {
            Toast.makeText(MainActivity.this, "未输入超时时间", Toast.LENGTH_SHORT).show();
        }
        final String commandPing = "/system/bin/ping -c 1 -w " + timeout + " " + address;

        isRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    int countM = Integer.valueOf(count);
                    for (int i = 1; i <= countM; i++) {
                        List<String> list = CommandUtils.execute(commandPing);
//                        StringBuilder builder = new StringBuilder();
//                        for (int j = 0; j < list.size(); j++) {
//                            String content = list.get(i);
//                            if (!"".equals(content)) {
//                                builder.append(content);
//                                builder.append("\n");
//                            }
//                        }
//                        String finalContent = builder.toString();
//                        ArrayList<String> contents = new ArrayList<>();
//                        contents.add(finalContent);
                        list.add("=========================第" + i + "次Ping测试==============================");
                        Log.d("TAG", "list:" + list.toString());
                        Message msg = new Message();
                        msg.what = 0x123;
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("result", (ArrayList<String>) list);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        // pingResults.addAll(list);
                    }
                    Message msg = new Message();
                    msg.what = 0x124;
                    handler.sendMessage(msg);
                    isRunning = false;
                }
            }
        });
        thread.start();


//        String commandPing = "/system/bin/ping " + " -w " + timeout + " " + address;

//        Log.d("commandPing..", "当前命令执行：" + commandPing);
//        pingResults.addAll(CommandUtils.execute(commandPing));
//        adapter.notifyDataSetChanged();
    }
}
