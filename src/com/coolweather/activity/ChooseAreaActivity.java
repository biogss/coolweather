package com.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.model.City;
import com.coolweather.model.CoolWeatherDB;
import com.coolweather.model.County;
import com.coolweather.model.Province;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

import android.R.anim;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>(); 
	/*
	 * 省列表
	 */
	private List<Province> provinceList;
	/*
	 * 市列表
	 */
	private List<City> cityList;
	/*
	 * 县列表
	 */
	private List<County> countyList;
	/*
	 * 选中的省份
	 */
	private Province selectProvince;
	/*
	 * 选中的城市
	 */
	private City selectCity;
	/*
	 * 选中的级别
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView =(ListView)findViewById(R.id.list_view);
		titleText =(TextView)findViewById(R.id.titlie_text);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1,dataList);//调用页面，布局文件（这是默认），数据
		listView.setAdapter(adapter);//完成最后的适配工作
		coolWeatherDB = coolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel ==LEVEL_PROVINCE)
				{
					selectProvince = provinceList.get(position);  //获得选中的省份
					queryCities();
				}else if(currentLevel ==LEVEL_CITY)
				{
					selectCity =cityList.get(position);  //获得选中的市
					queryCounties();
				}
			}
			
		});
		queryProvinces();
	}
	/*
	 * 查询全国所有的省，优先从数据库查询，如果没有再到服务器上查询
	 */
	private void queryProvinces()
	{
		provinceList =coolWeatherDB.loadProvinces();
		if(provinceList.size() > 0)
		{
			dataList.clear();
			for(Province province :provinceList)
			{
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel =LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "Province");
		}
	}
	/*
	 * 查询选中的省内所有城市，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryCities() {
		cityList =coolWeatherDB.loadCities(selectProvince.getId());
		if(cityList.size() > 0)
		{
			dataList.clear();
			for(City city :cityList)
			{
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLevel =LEVEL_CITY;
		}else {
			queryFromServer(selectProvince.getProvinceCode(),"city");
		}
	}
	
	/*
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	
	private void queryCounties() {
		countyList =coolWeatherDB.loCounties(selectCity.getId());
		if(countyList.size() >0)
		{
			dataList.clear();
			for(County county : countyList)
			{
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectCity.getCityName());
			currentLevel =LEVEL_COUNTY;
		}else {
			queryFromServer(selectCity.getCityCode(),"county");
		}
	}
	
	/*
	 *根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code))
		{
			address = "http://www.weather.com.cn/data/list3/city.xml"+code+".xml";
		}else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				  boolean result =false;
				  if("province".equals(type)){
					  result =Utility.handleProvincesResponse(coolWeatherDB, response);
				  }else if("city".equals(type)){
					  result = Utility.handleCitiesResponse(coolWeatherDB, response, selectProvince.getId());
				  }else if("county".equals(type)){
					  result = Utility.handleCountiesResponse(coolWeatherDB, response, selectCity.getId());
				  }
				  if ((result)) {
					//通过runOnUiThread()方法回到主线程处理逻辑,onFinish已经把数据存到数据库了，可以再次回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/*
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if(progressDialog ==null){
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载。。。");
				progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if(progressDialog !=null){
			progressDialog.dismiss();
		}
	}
	
	/*
	 * 捕获back按键，根据当前的级别来判断，此时应该返回市列表，省列表，还是直接退出
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(currentLevel ==LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel ==LEVEL_CITY){
			queryProvinces();
		}else {
			finish();
		}
	}
}
