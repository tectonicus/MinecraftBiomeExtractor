package com.google.code.minecraftbiomeextractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil
{

	// Given a class name, this function creates a string which
	// characterizes the class's structure without including
	// any names.
	public static String generateClassSignature(final String className)
	{
		String signature = new String("");
		try
		{
			Class<?> clazz = Class.forName(className);
			
			Constructor<?> ctorList[] = clazz.getDeclaredConstructors();
			for (int i = 0; i < ctorList.length; i++)
			{
				Constructor<?> ct = ctorList[i];
				Class<?> pvec[] = ct.getParameterTypes();
				for (int j = 0; j < pvec.length; j++)
				{
					signature = signature + "carg:" + pvec[j].toString() + " ";
				}
				signature = signature + "+";
			}
			
			Method methlist[] = clazz.getDeclaredMethods();
			for (int i = 0; i < methlist.length; i++)
			{
				Method m = methlist[i];
				Class<?> pvec[] = m.getParameterTypes();
				for (int j = 0; j < pvec.length; j++)
				{
					if (pvec[j].toString().startsWith("class"))
						signature = signature + "arg:class" + " ";
					else
						signature = signature + "arg:" + pvec[j].toString() + " ";
				}
				if (m.getReturnType().toString().startsWith("class"))
					signature = signature + "ret:class" + "+";
				else
					signature = signature + "ret:" + m.getReturnType().toString() + "+";
			}
			
			Field fieldList[] = clazz.getDeclaredFields();
			for (int i = 0; i < fieldList.length; i++)
			{
				Field field = fieldList[i];
				if (field.getType().toString().startsWith("class"))
					signature = signature + "fld:class" + "+";
				else
					signature = signature + "fld:" + field.getType().toString() + "+";
			}
		}
		catch (Throwable e)
		{
		//	System.err.println(e);
		}
		return signature;
	}
	
	// This function attempts to make matching plain fields in a class easier.
	// Basically, you give it a type and a number fcount and if will find the
	// fcount-th field with that type.
	public static String getFieldWithType(final String className, final String tpe, final int fcount)
	{
		int count = 0;
		try
		{
			Class<?> clazz = Class.forName(className);
			Field fieldlist[] = clazz.getDeclaredFields();
			for (int i = 0; i < fieldlist.length; i++)
			{
				Field fld = fieldlist[i];
				if (fld.getType().toString().equals(tpe))
				{
					count++;
					if (count == fcount)
						return fld.getName();
				}
			}
		}
		catch (Throwable e) {}
		
		return "";
	}
	
}
