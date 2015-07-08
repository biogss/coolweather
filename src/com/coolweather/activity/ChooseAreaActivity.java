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
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/*
	 * ���б�
	 */
	private List<City> cityList;
	/*
	 * ���б�
	 */
	private List<County> countyList;
	/*
	 * ѡ�е�ʡ��
	 */
	private Province selectProvince;
	/*
	 * ѡ�еĳ���
	 */
	private City selectCity;
	/*
	 * ѡ�еļ���
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
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1,dataList);//����ҳ�棬�����ļ�������Ĭ�ϣ�������
		listView.setAdapter(adapter);//����������乤��
		coolWeatherDB = coolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel ==LEVEL_PROVINCE)
				{
					selectProvince = provinceList.get(position);  //���ѡ�е�ʡ��
					queryCities();
				}else if(currentLevel ==LEVEL_CITY)
				{
					selectCity =cityList.get(position);  //���ѡ�е���
					queryCounties();
				}
			}
			
		});
		queryProvinces();
	}
	/*
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û���ٵ��������ϲ�ѯ
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
			titleText.setText("�й�");
			currentLevel =LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "Province");
		}
	}
	/*
	 * ��ѯѡ�е�ʡ�����г��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
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
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
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
	 *���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������
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
					//ͨ��runOnUiThread()�����ص����̴߳����߼�,onFinish�Ѿ������ݴ浽���ݿ��ˣ������ٴλص����̴߳����߼�
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
				//ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/*
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if(progressDialog ==null){
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("���ڼ��ء�����");
				progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if(progressDialog !=null){
			progressDialog.dismiss();
		}
	}
	
	/*
	 * ����back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
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
