package es.um.sisdist.videofaces.backend.Service;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.mysql.cj.xdevapi.JsonArray;

public class Prueba {

	public static void main(String[] args) {
		String s = "{\"id\":{\"chars\":\"5\",\"string\":\"5\",\"valueType\":\"STRING\"},\"name\":{\"chars\":\"juan\",\"string\":\"juan\",\"valueType\":\"STRING\"},\"email\":{\"chars\":\"juan6@um.es\",\"string\":\"juan6@um.es\",\"valueType\":\"STRING\"},\"password\":{\"chars\":\"81dc9bdb52d04dc20036dbd8313ed055\",\"string\":\"81dc9bdb52d04dc20036dbd8313ed055\",\"valueType\":\"STRING\"}}";
		System.out.println(s);
		
//		System.out.println(trozos[1]);
//		System.out.println(s.indexOf(trozos[1]));
//		System.out.println(trozos[1].indexOf("\""));
		
//		Map<String, String> properties = Splitter.on("{").withKeyValueSeparator(":").split(s);
//		System.out.println(properties);
	}

}
