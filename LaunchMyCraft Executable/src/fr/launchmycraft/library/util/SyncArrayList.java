package fr.launchmycraft.library.util;

import java.util.ArrayList;

public class SyncArrayList<T> {
	
	ArrayList<T> list = new ArrayList<T>();
	
	public synchronized void add(T o)
	{
		list.add(o);
	}
	
	public ArrayList<T> getArrayList()
	{
		return list;
	}
}
