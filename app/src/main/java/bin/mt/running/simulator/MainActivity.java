package bin.mt.running.simulator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, View.OnClickListener {
    private AMap aMap;
    private MapView mapView;
    private UiSettings mUiSettings;

    public void tipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("wyy给您的温馨提示");
        builder.setMessage("1.请在设置中打开模拟定位功能(如果你能看到这条消息证明你已经打开了) \n\n2.魏萍在用app的时候不许偷懒(重点！) \n\n3.点击取消程序直接退出\n\n4.药萍儿花生大的脑仁不太好使");
        //builder.setMessage("1.请在设置中打开模拟定位功能(如果你能看到这条消息证明你已经打开了) \n\n3.点击取消程序直接退出");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setCancelable(true);            //点击对话框以外的区域是否让对话框消失

        //设置正面按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "你点击了确定", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        //设置反面按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "你点击了取消", Toast.LENGTH_SHORT).show();
                getApplication().onTerminate();
                System.exit(0);
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();      //创建AlertDialog对象
        //对话框显示的监听事件

        dialog.show();                              //显示对话框
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tipDialog();
        if (!LocationUtil.initLocationManager(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        mUiSettings = aMap.getUiSettings();
        init();
    }

    private void init() {
        // 定位到当前位置
        mUiSettings.setMyLocationButtonEnabled(true);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);

        aMap.moveCamera(CameraUpdateFactory.zoomTo(15.5f));
        mUiSettings.setTiltGesturesEnabled(false);

        aMap.setOnMapClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null)
            mapView.onDestroy();
    }

    Marker startMarker = null;
    List<Marker> passMarkers = new ArrayList<>(4);
    Marker endMarker = null;

    private void reset() {
        for (Marker marker : aMap.getMapScreenMarkers()) {
            if (marker.getObject() == this)
                marker.remove();
        }
        passMarkers.clear();
        startMarker = null;
        endMarker = null;
    }

    int evenType = -1;
    Toast toast;

    public void pick(int evenType) {
        this.evenType = evenType;
        toast = Toast.makeText(this, "请在地图上选取坐标", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        switch (evenType) {
            case 0: {
                if (startMarker != null) {
                    startMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .position(latLng)
                        .draggable(true).title("起点");
                startMarker = aMap.addMarker(markerOptions);
                startMarker.setObject(this);
                break;
            }
            case 1: {
                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .position(latLng)
                        .draggable(true).title("途径点");
                Marker marker = aMap.addMarker(markerOptions);
                marker.setObject(this);
                passMarkers.add(marker);
                break;
            }
            case 2: {
                if (endMarker != null) {
                    endMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(latLng)
                        .draggable(true).title("终点");
                endMarker = aMap.addMarker(markerOptions);
                endMarker.setObject(this);
                break;
            }
        }
        evenType = -1;
//        startMarker.showInfoWindow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                pick(0);
                break;
            case R.id.button2:
                if (passMarkers.size() >= 4) {
                    Toast.makeText(this, "最多选择4个途径点", Toast.LENGTH_SHORT).show();
                    return;
                }
                pick(1);
                break;
            case R.id.button3:
                pick(2);
                break;
            case R.id.button4:
                reset();
                break;
            case R.id.button5:
                if (startMarker == null) {
                    Toast.makeText(this, "请选择起点", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (endMarker == null) {
                    Toast.makeText(this, "请选择终点", Toast.LENGTH_SHORT).show();
                    return;
                }
                NaviActivity.sList.clear();
                NaviActivity.pList.clear();
                NaviActivity.eList.clear();

                NaviActivity.sList.add(newNaviLatLng(startMarker.getPosition()));
                for (Marker passMarker : passMarkers)
                    NaviActivity.pList.add(newNaviLatLng(passMarker.getPosition()));
                NaviActivity.eList.add(newNaviLatLng(endMarker.getPosition()));
                Intent intent = new Intent(this, NaviActivity.class);
                startActivity(intent);
                break;
        }
    }

    private static NaviLatLng newNaviLatLng(LatLng latLng) {
        return new NaviLatLng(latLng.latitude, latLng.longitude);
    }
}
