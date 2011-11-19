package com.jameslow;

import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimpleNodeList implements NodeList {
	private List list = new ArrayList();
	
	public void add(Node item) {
		list.add(item);
	}
	public void remove(Node item) {
		list.remove(item);
	}
	public int getLength() {
		return list.size();
	}
	public Node item(int index) {
		try {
			return (Node) list.get(index);
		} catch (Exception e) {
			return null;
		}
	}
}
