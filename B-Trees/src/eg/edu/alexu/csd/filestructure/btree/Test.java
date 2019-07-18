package eg.edu.alexu.csd.filestructure.btree;

import java.util.HashMap;
import java.util.Map;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 Map< String,Integer> hm =  
                 new HashMap< String,Integer>(); 
hm.put("a", 100); 
hm.put("a", hm.get("a")+1); 

System.out.println(hm.get("b"));
	}

}
