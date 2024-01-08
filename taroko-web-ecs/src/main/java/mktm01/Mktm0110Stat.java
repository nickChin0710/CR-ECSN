package mktm01;

import taroko.com.TarokoCommon;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({"unchecked","deprecation"})
public class Mktm0110Stat {
	
public static int loadControl = 5;
public static HashMap manualHash = new HashMap();


public static void setManual() {

  String uuid1 = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
  manualHash.put("1234",uuid1);
  loadControl = 9;
	return;
}

public static String getManual() {

	return (String)manualHash.get("1234");
 }

}
