package fi.tut.fast.dpws.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Node;

public class DPWSTypeManager {

	private static Map<String,DPWSTypeManager> instances = new HashMap<String,DPWSTypeManager>();
	private List<Class> classList = new ArrayList<Class>();
	private JAXBContext context;
	private final String namespace;
	
	public static DPWSTypeManager getInstance(String namespace){
		DPWSTypeManager instance = instances.get(namespace);
		if(instance == null){
			instance = new DPWSTypeManager(namespace);
			instances.put(namespace, instance);
		}
		return instance;
	}
	
	public static void destroyInstance(String namespace){
		instances.remove(namespace);
	}
	
	private DPWSTypeManager(String namespace){
		this.namespace = namespace;
	}
	
	public String getNamespace(){
		return namespace;
	}
	
	public void addClasses(Class...classes){
		classList.addAll(Arrays.asList(classes));
	}
	
	public Node generateSchema() throws JAXBException, IOException{
		if(classList.isEmpty()){
			return null;
		}
		return generateSchema(classList.toArray(new Class[]{}));
	}
	
    public Node generateSchema(Class...classes) throws JAXBException, IOException{
    	context = JAXBContext.newInstance(classes);
    	
    	final DOMResult result = new DOMResult();
    	
    	context.generateSchema(new SchemaOutputResolver(){

			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				return result;
			}
    		
    	});
    	
    	return result.getNode();
    	
    }
	
}
