package org.jqassistant.contrib.plugin.c.impl.scanner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to query the {@code ArrayDeque<Object>} that is used in the scanner plugin
 * @author Christina Sixtus
 */
public class DequeUtils {

	/**
	 * Iterates over a deque and checks if an object of type A appears before an object 
	 * of type B
	 * @param a Class of type A
	 * @param b Class of type B
	 * @param deque an ArrayDeque that holds a number of objects
	 * 
	 * @return boolean if type A appears before type B
	 */
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
	
	/**
	 * Iterates over a deque and gets the first instance of type A, if not contained
	 * returns null
	 * @param a Class of type A
	 * @param deque an ArrayDeque that contains a number of objects
	 * 
	 * @return Object first object of this type or null
	 */
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
	
	/**
	 * Iterates over a deque and returns a object at the specified index beginning from the
	 * top of the deque.
	 * If the specified index doesn't exist returns null.
	 * @param index int value of the searched position
	 * @param deque an ArrayDeque that is searched through
	 * 
	 * @return Object element at the specified position or null
	 */
	public static Object getElementAt(int index, ArrayDeque<? extends Object> deque) {
		int counter = 0;
		for(Iterator<? extends Object> it = deque.iterator(); it.hasNext();) {
			Object currentObject = it.next();
			if(counter == index) {
				return currentObject;
			}
			counter += 1;
		}
		return null;
	}
	
	/**
	 * Function retrieves first occurrence of type A if objects exists in deque
	 * and removes it.
	 * @param a Class of type A
	 * @param deque ArrayDeque that contains instances of Object
	 */
	public static <A extends Object> void removeFirstOccurrenceOfType(Class<A> a, ArrayDeque<? extends Object> deque) {
		A dequeElement = (A) getFirstOfType(a, deque);
		if(dequeElement != null) {
			deque.remove(dequeElement);
		}
	}
	
	/**
	 * Retrieves all elements of type B after the first occurrence of an element of type A in the ArrayDeque
	 * @param typeOfBaseElement Class of type A
	 * @param typeOfSearchedElements Class of type B
	 * @param deque ArrayDeque
	 * @return List with objects of type B
	 */
	public static <A extends Object, B extends Object> List<B> getElementsUnder(Class<A> typeOfBaseElement, Class<B> typeOfSearchedElements, ArrayDeque<? extends Object> deque){
		boolean baseElementFound = false;
		List<B> resultList = new ArrayList<>();
		for(Iterator<? extends Object> it = deque.iterator(); it.hasNext();) {
			Object currentObject = it.next();
			if(typeOfBaseElement.isAssignableFrom(currentObject.getClass())) {
				baseElementFound = true;
			}
			if(typeOfSearchedElements.isAssignableFrom(currentObject.getClass()) && baseElementFound) {
				resultList.add((B) currentObject);
			}
		}
		return resultList;	
	}
	
}
