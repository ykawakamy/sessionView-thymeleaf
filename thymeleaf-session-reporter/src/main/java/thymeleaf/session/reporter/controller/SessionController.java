package thymeleaf.session.reporter.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/session")
public class SessionController {
	
	Method GraphLayout_parseInstance;
	Method GraphLayout_totalSize;
	
	@Autowired
	ObjectMapper mapper;
	
	public SessionController() {
		try {
			Class<?> classToLoad = Class.forName("org.openjdk.jol.info.GraphLayout");
			GraphLayout_parseInstance = classToLoad.getDeclaredMethod("parseInstance", Object[].class);
			GraphLayout_totalSize = classToLoad.getDeclaredMethod("totalSize");
		} catch (Exception e) {
		}
		
	}
	
	public class SessionVO {
		public String key;
		public Object value;
		public long size;
		public String json;
		
		public SessionVO(String name, Object value) {
			this.key = name;
			this.value = value;
			this.size  = getTotalSize(value);

			try {
				this.json = mapper.writeValueAsString(value);
			} catch (JsonProcessingException e) {
				this.json = "(failed to deserialized)";
			}
		}
	}

	private long getTotalSize(Object value) {
		if( GraphLayout_totalSize == null ) {
			return -1;
		}

		try {
			// GraphLayout.parseInstance(obj).totalSize();
			Object graphLayout = GraphLayout_parseInstance.invoke(null, new Object[] { new Object[]{value} } );
			return (long) GraphLayout_totalSize.invoke(graphLayout);
		} catch (Exception e) {
			return -1;
		}
	}

	@RequestMapping("/list")
	public String list(Model model, HttpSession session) {

		IdentityHashMap<Object, SessionVO> map = new IdentityHashMap<>();
		
		Enumeration<String> names = session.getAttributeNames();
		while( names.hasMoreElements() ) {
			String name = names.nextElement();
			Object value = session.getAttribute(name);
			
			SessionVO vo = new SessionVO(name, value);
			map.put(value, vo );
		}
		model.addAttribute("sessionVo", map);
		
		return "session";
	}
	
	@RequestMapping("/add")
	public String add(
			@RequestParam("key") Optional<String> pkey,
			@RequestParam("value") Optional<String> pvalue,
			Model model, HttpSession session) {
		
		
		String name = pkey.orElseGet( ()->{ return "key" + System.currentTimeMillis(); } );
		String value = pvalue.orElseGet( ()->{ return "" + System.currentTimeMillis(); } );
		
		Object object = value;
		switch( value ) {
		case "dto":
			object = new TestDto();
			break;
		case "map":
			Map<String,String> map = new HashMap<>();
			map.put("keyA","valueA");		
			map.put("keyB","valueB");		
			map.put("keyC","valueC");
			object = map;
			break;
		case "list":
			List<String> list = new ArrayList<>();
			list.add("item1");
			list.add("item2");
			list.add("item3");
			object = list;
			break;
			
		}
		session.setAttribute(name, object);
		
		return list(model, session);
	}

	@RequestMapping("/remove")
	public String remove(@RequestParam("key") Optional<String> key, Model model, HttpSession session) {
		key.ifPresent( (it)->{
			session.removeAttribute(it);
		});
		
		return list(model, session);
	}

	@RequestMapping("/removeAll")
	public String removeAll(@RequestParam("key") Optional<String> key, Model model, HttpSession session) {
		Enumeration<String> names = session.getAttributeNames();
		while( names.hasMoreElements() ) {
			String name = names.nextElement();
			Object value = session.getAttribute(name);
			
			if( key.isPresent() && !name.matches(key.get()) ) {
				continue;
			}
			
			session.removeAttribute(name);
		}
		
		return list(model, session);
	}
}
