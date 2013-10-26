package com.darktidegames.celeo.scripter;

public class IllegalScriptException extends Exception
{

	private static final long serialVersionUID = 5662427594174029949L;

	public IllegalScriptException()
	{
		super("Illegal script arguement found");
	}

	public IllegalScriptException(String message)
	{
		super(message);
	}

}