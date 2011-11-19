package com.jameslow;

import java.util.*;
import joptsimple.*;

public class CommandLine extends OptionParser {
	private OptionSet options;
	
	private boolean debug;
	private boolean quiet;
	private boolean help;
	private int loglevel;
	
	private static final String DEBUG = "d";
	private static final String QUIET = "q";
	private static final String LOGLEVEL = "l";
	private static final String HELP = "help";
	{
		accepts(DEBUG, "debug");
        accepts(QUIET, "quiet");
        accepts(LOGLEVEL, "loglevel").withRequiredArg().describedAs( "level" ).ofType(String.class);
        accepts(HELP, "shows this help message");
	}
	public CommandLine(String[] args) {
		/*
		OptionParser parser = new OptionParser() {
            {
                accepts(DEBUG, "debug");
                accepts(QUIET, "quiet");
                accepts(LOGLEVEL, "loglevel").withRequiredArg().describedAs( "level" ).ofType(String.class);
                accepts(HELP, "shows this help message");
            }
        };
		*/
        options = parse(args);
        
        help = options.wasDetected(HELP);
        if (help) {
        	try {
        		printHelpOn(System.out);
        	} catch (Exception e) {
        		Main.Logger().warning("System.out not avaliable");
        	}
        }
        debug = options.wasDetected(DEBUG);
        quiet = options.wasDetected(QUIET);
        if (options.wasDetected(LOGLEVEL)) {
        	loglevel = Integer.parseInt(options.argumentOf(LOGLEVEL));
        }
	}
	public boolean getQuiet() {
		return quiet;
	}
	public boolean getDebug() {
		return debug;
	}
	public int getLogLevel() {
		return loglevel;
	}
	public boolean getHelp() {
		return help;
	}	
	public OptionSet getOptions() {
		return options;
	}
}