package thymeleaf.session.reporter.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDto {
	public Map<String,String> map = new HashMap<>();
	{
		map.put("keyA","valueA");		
		map.put("keyB","valueB");		
		map.put("keyC","valueC");		
	};
	
	public List<String> list = new ArrayList<>();
	{{
		list.add("item1");
		list.add("item2");
		list.add("item3");
	}};

}
