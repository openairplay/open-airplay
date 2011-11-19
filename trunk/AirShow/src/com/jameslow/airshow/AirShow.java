package com.jameslow.airshow;

import com.jameslow.*;

public class AirShow extends Main {
	public AirShow(String args[]) {
		super(args,null,null,null,AirShowWindow.class.getName(),null,null,null);
		//super(args,null,null,TemplateSettings.class.getName(),TemplateWindow.class.getName(),null,null,TemplatePref.class.getName());
	}
	public static void main(String args[]) {
		instance = new AirShow(args);
	}
}