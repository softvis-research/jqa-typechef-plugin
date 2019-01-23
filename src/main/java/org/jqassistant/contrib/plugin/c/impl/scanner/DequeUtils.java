package org.jqassistant.contrib.plugin.c.impl.scanner;

import java.util.ArrayDeque;
import java.util.Iterator;

public class DequeUtils {

	public static <A extends Object, B extends Object> boolean before(Class<A> a, Class<B> b, ArrayDeque<Object> deque) {
		Iterator<Object> it = deque.iterator();
		while(it.hasNext()) {
			Object currentObject = it.next();
			if(currentObject.getClass() == a) {
				return true;
			} else if(currentObject.getClass() == b) {
				return false;
			}
		}
		return false;
	}
	
	public static <A extends Object> Object getFirstOfType(Class<A> a, ArrayDeque<? extends Object> deque){
		Iterator<? extends Object> it = deque.iterator();
		while(it.hasNext()) {
			Object currentObject = it.next();
			if(a.isAssignableFrom(currentObject.getClass())) {
				return currentObject;
			}
		}
		return null;
	}
	
}
