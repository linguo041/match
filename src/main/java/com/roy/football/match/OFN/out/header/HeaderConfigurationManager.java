package com.roy.football.match.OFN.out.header;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HeaderConfigurationManager {  
	public final static ConcurrentHashMap<Class<?>, List<HeaderConfiguration>> cache = new ConcurrentHashMap<Class<?>, List<HeaderConfiguration>>();

    public synchronized static List<HeaderConfiguration> getHeaderConfigurations (Class<?> clazz) {
    	List<HeaderConfiguration> configurations = cache.get(clazz);
    	
    	if (configurations == null) {
    		configurations = getClassHeaderConfiguration(clazz);
    		cache.put(clazz, configurations);
    	}
    	
    	return configurations;
    }
    
    private static List<HeaderConfiguration> getClassHeaderConfiguration(Class<?> clazz){
    	List <HeaderConfiguration> headerConfigs = new ArrayList<HeaderConfiguration>();
    
    	Field [] fields = clazz.getDeclaredFields();
		
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				Header h = field.getAnnotation(Header.class);
				
				if (h != null) {
					HeaderConfiguration headerConfig = new HeaderConfiguration();
					headerConfig.setPropertyName(field.getName());
					headerConfig.setTitle(h.title());
					headerConfig.setOrder(h.order());
					headerConfig.setWritale(h.writable());
					headerConfig.setDataType(field.getType());

					headerConfigs.add(headerConfig);
				}
			}
		}

    	return headerConfigs;
    }
    
    public static void reorderHeaders (List<HeaderConfiguration> headers) {
	    Collections.sort(headers, new Comparator<HeaderConfiguration>(){

            @Override
            public int compare(HeaderConfiguration o1, HeaderConfiguration o2){
                if(null != o1 && null != o2){
                    if(o1.getOrder() == o2.getOrder()){
                        throw new DuplicateException("Duplicate order: " + o1.getOrder());
                    }
                    return (int) (o1.getOrder() - o2.getOrder());
                }
                return 0;
            }
        });
	}
}
