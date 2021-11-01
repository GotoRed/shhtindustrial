package com.shht.apps.yqj.openapi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.api.OpenApiClient;
import com.actionsoft.sdk.service.response.ListMapResponse;

public class UserUnitBOQuery {

	public static void main(String[] ar) {
		// TODO Auto-generated method stub

		String url = "https://www.sht808.com/portal/openapi";
		String apiMethod = "bo.query";
		String accessKey = "opapiuser4326";
		String secret = "Abcd123456@#";

		Map<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("boName","BO_EU_SHHT_SASTUNIT" );//用户单位信息
		OpenApiClient client = new OpenApiClient(url, accessKey, secret);
		ListMapResponse r = client.exec(apiMethod, tmp, ListMapResponse.class);
		List<Map<String, Object>> lists = r.getData();
		System.out.println(""+lists);
	}

}
