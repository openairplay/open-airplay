package com.jameslow;

import java.awt.Color;
import java.io.File;
import java.util.logging.Logger;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * This class assumes you know what you're doing
 * It doesn't let you fully access the XML dom structure
 * but assumes that if you're only trying to get one node or set one node, that only one exists
 * @author James
 */
public class XMLHelper {
	public static final String DELIM = ".";
	private static final String REGEX = "\\" + DELIM;
	private static final String NAME = "Name";
	private Element element;
	private boolean isnewnode = false;
	private Logger logger;
	
	public XMLHelper() {
		try {
			logger = Main.Logger();
		} catch (Exception e) {
		}
	}
	public XMLHelper(String root) {
		this();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			setElement((Element) document.appendChild(document.createElement(root)));
		} catch (ParserConfigurationException e) {
			//Not quite sure when this would happen
			severe("Could not extantiate XML builder: " + e.getMessage());
		}
	}
	public XMLHelper(Element element) {
		this();
		setElement(element);
	}
	public XMLHelper(Element element, boolean isnewnode) {
		this();
		setElement(element);
		setIsNewNode(isnewnode);
	}
	public XMLHelper(String filename, String root) {
		this();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				Document document = builder.parse(new File(filename));
				setElement((Element) document.getElementsByTagName(root).item(0));
			} catch (Exception e) {
				Document document = builder.newDocument();
				setElement((Element) document.appendChild(document.createElement(root)));
				warning("Could not load XML file: " + e.getMessage());
			}
		} catch (ParserConfigurationException e) {
			//Not quite sure when this would happen
			severe("Could not extantiate XML builder: " + e.getMessage());
		}
	}
	public void save(String filename) {
		try {
			Document document = element.getOwnerDocument();
			document.getDocumentElement().normalize();
			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(4));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(document);
			File file = new File(filename);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
		} catch (Exception e) {
			severe("Could not save XML file: " + e.getMessage());
		}
	}
	private void severe(String msg) {
		if (logger != null) {
			logger.severe(msg);
		} else {
			System.out.println("Severe: "+msg);
		}
	}
	private void warning(String msg) {
		if (logger != null) {
			logger.warning(msg);
		} else {
			System.out.println("Warning: "+msg);
		}
	}
	private Element getElement() {
		return element;
	}
	private void setElement(Element element) {
		this.element = element;
	}
	public boolean getIsNewNode() {
		if (isnewnode) {
			isnewnode = false;
			return true;
		} else {
			return false;
		}
	}
	private void setIsNewNode(boolean isnewnode) {
		this.isnewnode = isnewnode;
	}
	private Node getTextNode() {
		NodeList list = element.getChildNodes();
		int length = list.getLength();
		for (int i=0; i<length; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				return node;
			}
		}
		return element.appendChild(element.getOwnerDocument().createTextNode(""));
	}
	public void setAttribute(String attribute, String value) {
		element.setAttribute(attribute, value);
	}
	public void setAttribute(String subnode, String attribute, String value) {
		getSubNode(subnode).setAttribute(attribute, value);
	}
	public String getAttribute(String attribute) {
		return element.getAttribute(attribute);
	}
	public String getAttribute(String subnode, String attribute) {
		return getSubNode(subnode).getAttribute(attribute);
	}
	public NodeList getImediateElementsByTagName(String tagname) {
		return getImediateElementsByTagName(element, tagname);
	}
	public NodeList getImediateElementsByTagName(Element el, String tagname) {
		NodeList list = el.getElementsByTagName(tagname);
		SimpleNodeList result = new SimpleNodeList();
		int length = list.getLength();
		for(int i=0; i < length; i++) {
			Node node = list.item(i);
			if ((Element) node.getParentNode() == el) {
				result.add(node);
			}
		}
		return result;
	}
	public void deleteSubNode(String subnode) {
		int pos = subnode.lastIndexOf(DELIM);
		if (pos >= 0) {
			XMLHelper helper = getSubNode(subnode.substring(0, pos));
			helper.deleteSubNode(subnode.substring(pos+1));
		} else {
			NodeList list = getImediateElementsByTagName(subnode);
			try {
				element.removeChild(list.item(0));
			} catch (Exception e) {}
		}	
	}
	public void deleteSubNodes(String subnode) {
		int pos = subnode.lastIndexOf(DELIM);
		if (pos >= 0) {
			XMLHelper helper = getSubNode(subnode.substring(0, pos));
			helper.deleteSubNodes(subnode.substring(pos+1));
		} else {
			NodeList list = getImediateElementsByTagName(subnode);
			int length = list.getLength();
			for(int i=0; i < length; i++) {
				try {
					element.removeChild(list.item(i));
				} catch (Exception e) {}
			}
		}	
	}
	public XMLHelper getSubNode(String subnode) {
		String nodes[] = subnode.split(REGEX);
		Element sub = element;
		boolean isnew = false;
		for (int i=0; i<nodes.length; i++) {
			NodeList list = getImediateElementsByTagName(sub,nodes[i]);
			if (list.getLength() > 0) {
				sub = (Element) list.item(0);				
			} else {
				sub = (Element) sub.appendChild(element.getOwnerDocument().createElement(nodes[i]));
				isnew=true;
			}
		}
		return new XMLHelper(sub,isnew);
	}
	public XMLHelper getSubNodeByName(String subnode, String name) {
		return getSubNodeByName(subnode,name,NAME);
	}
	public XMLHelper getSubNodeByName(String subnode, String name, String namenode) {
		XMLHelper[] list = getSubNodeList(subnode);
		if (list.length != 0) {
			for (int i=0; i<list.length; i++) {
				if (list[i].getSubNode(namenode).getValue().compareTo(name) == 0) {
					return list[i];
				}
			}
		}
		XMLHelper node = createSubNode(subnode);
		node.createSubNode(namenode).setValue(name);
		return node;  
	}
	public XMLHelper getNameNode() {
		return getSubNode(NAME);
	}
	public String getNameValue() {
		return getNameNode().getValue();
	}
	public XMLHelper createSubNode(String subnode) {
		int pos = subnode.lastIndexOf(DELIM);
		if (pos >= 0) {
			XMLHelper helper = getSubNode(subnode.substring(0, pos));
			return helper.createSubNode(subnode.substring(pos+1));
		} else {
			return new XMLHelper((Element) element.appendChild(element.getOwnerDocument().createElement(subnode)),true);
		}		
	}
	public XMLHelper[] getSubNodeList(String subnode) {
		int pos = subnode.lastIndexOf(DELIM);
		if (pos >= 0) {
			XMLHelper helper = getSubNode(subnode.substring(0, pos));
			return helper.getSubNodeList(subnode.substring(pos+1));
		} else {
			NodeList list = getImediateElementsByTagName(subnode);
			XMLHelper[] results = new XMLHelper[list.getLength()]; 
			int length = list.getLength();
			for (int i=0; i<length; i++) {
				results[i] = new XMLHelper((Element) list.item(i));
			}
			return results;
		}
	}
	public static int parseValue(int def, XMLHelper node) {
		try {
			return Integer.parseInt(node.getValue());
		} catch (Exception e) {
			node.setValue(def);
			return def;
		}
	}
	public static float parseValue(float def, XMLHelper node) {
		try {
			return Float.parseFloat(node.getValue());
		} catch (Exception e) {
			node.setValue(def);
			return def;
		}
	}
	public static boolean parseValue(boolean def, XMLHelper node) {
		String value = node.getValue().toUpperCase();
		if ("TRUE".compareTo(value) == 0) {
			return true;
		} else if ("FALSE".compareTo(value) == 0) {
			return false;
		} else {
			node.setValue(def);
			return def;
		}
	}
	public static Color parseValue(Color def, XMLHelper node) {
		try {
			return Color.decode(node.getValue());
		} catch (Exception e) {
			node.setValue(def);
			return def;
		}
	}
	public int getValue(int def) {
		return parseValue(def,this);
	}
	public float getValue(float def) {
		return parseValue(def,this);
	}
	public boolean getValue(boolean def) {
		return parseValue(def,this);
	}
	public Color getValue(Color def) {
		return parseValue(def,this);
	}
	public String getValue() {
		return getTextNode().getNodeValue();
	}
	public String getValue(String subnode) {
		return getSubNode(subnode).getValue();
	}
	public String getValue(String subnode, String def) {
		XMLHelper helper = getSubNode(subnode);
		//To account for the case where the default is not a blank string, but we may want to eventually set the setting to one
		//We don't do this if we are doing getValue() on this node, because we should know if its new or not
		if (helper.getIsNewNode()) {
			helper.setValue(def);
			return def;
		} else {
			return helper.getValue();
		}
	}
	public int getValue(String subnode, int def) {
		return parseValue(def,getSubNode(subnode));
	}
	public float getValue(String subnode, float def) {
		return parseValue(def,getSubNode(subnode));
	}
	public boolean getValue(String subnode, boolean def) {
		return parseValue(def,getSubNode(subnode));
	}
	public Color getValue(String subnode, Color def) {
		return parseValue(def,getSubNode(subnode));
	}
	public void setValue(String value) {
		getTextNode().setNodeValue(value);
	}
	public void setValue(String subnode, String value) {
		getSubNode(subnode).setValue(value);
	}
	public void setValue(int value) {
		getTextNode().setNodeValue(""+value);
	}
	public void setValue(String subnode, int value) {
		getSubNode(subnode).setValue(value);
	}
	public void setValue(float value) {
		getTextNode().setNodeValue(""+value);
	}
	public void setValue(String subnode, float value) {
		getSubNode(subnode).setValue(value);
	}
	public void setValue(boolean value) {
		getTextNode().setNodeValue(""+value);
	}
	public void setValue(String subnode, boolean value) {
		getSubNode(subnode).setValue(value);
	}
	public void setValue(Color value) {
		getTextNode().setNodeValue(Integer.toHexString(value.getRGB()));
	}
	public void setValue(String subnode, Color value) {
		getSubNode(subnode).setValue(value);
	}
	//TODO: write a function that allows you to save a list of XMLHelper classes to the tree
	public void addValue(String subnode, String value) {
		createSubNode(subnode).setValue(value);
	}
	public void addValues(String subnode, String values[]) {
		for(int i=0; i<values.length; i++) {
			addValue(subnode,values[i]);
		}
	}
	public void replaceValues(String subnode, String values[]) {
		deleteSubNodes(subnode);
		addValues(subnode, values);
	}
}