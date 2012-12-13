package fi.tut.fast.dpws.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.xmlsoap.schemas.devprof.LocalizedStringType;

public class LocalizedTextMap extends HashMap<String,String>{
	
	public static final String DEFAULT_LOCALE = Locale.getDefault().getLanguage();
	
	public void put(String str){
		put(DEFAULT_LOCALE,str);
	}
	public String get(){
		return get(DEFAULT_LOCALE);
	}
	
	@Override
	public String put(String locale, String value){
		if(!Arrays.asList(Locale.getAvailableLocales()).contains(new Locale(locale))){
			System.err.println("Invalid Locale.  Not adding...");
		}
		return super.put(locale, value);
	}
	
	public List<LocalizedStringType> toList(){
		List<LocalizedStringType> list = new ArrayList<LocalizedStringType>();
		for(String key : keySet()){
			list.add(new LocalizedStringType(key,get(key)));
		}
		return list;
	}
	
	public void putAll(List<LocalizedStringType> list){
		for(LocalizedStringType el : list){
			put(el.getLocale(), el.getValue());
		}
	}
	
}
