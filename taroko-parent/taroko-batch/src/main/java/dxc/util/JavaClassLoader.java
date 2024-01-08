/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  111-01-19 V1.00.02   Justin       fix Missing Check against Null          *                                                                           *
*****************************************************************************/
package dxc.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class JavaClassLoader extends ClassLoader {
  public Class<?> getClass(String classBinName) {
    Class<?> loadedMyClass = null;
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      if (classLoader != null) {
    	  loadedMyClass = classLoader.loadClass(classBinName);
	  }  
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      loadedMyClass = null;
    } catch (Exception e) {
      e.printStackTrace();
      loadedMyClass = null;
    }
    return loadedMyClass;
  }

  public Class<?> getClassNew(String classBinName) {
    Class<?> loadedMyClass = null;
    try {
      String[] tmp = classBinName.split("\\.");
      if (tmp.length == 2) {
        String pack = tmp[0];
        String clsName = tmp[1];

        clsName = String.format("%s/%s.class", new Object[] {pack, clsName});
      //2020_0615 resolve Unreleased Resource: Streams by yanghan
        ClassLoader classLoader = JavaClassLoader.class.getClassLoader();
        if (classLoader != null) {
        	try( InputStream input = classLoader.getResourceAsStream(clsName)){
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int data = input.read();
                while (data != -1) {
                  buffer.write(data);
                  data = input.read();
                }
                byte[] classData = buffer.toByteArray();
                loadedMyClass = defineClass(classBinName, classData, 0, classData.length);
              } 
		} 
      }
    } catch (Exception e) {
      e.printStackTrace();
      loadedMyClass = null;
    }
    return loadedMyClass;
  }
}
