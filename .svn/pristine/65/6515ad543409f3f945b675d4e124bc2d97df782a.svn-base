/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.config;

import java.util.*;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

final class SQLiteConfig extends ConfigInterface.Stub
{
	private final Service			myService;

	private final SQLiteDatabase	myDatabase;
	/**
	 * 查询特定Group和Name的SQL语句
	 */
	private final SQLiteStatement	myGetValueStatement;
	/**
	 * 插入或替换特定Group和Name的SQL语句
	 */
	private final SQLiteStatement	mySetValueStatement;
	/**
	 * 删除特定Group和Name的SQL语句
	 */
	private final SQLiteStatement	myUnsetValueStatement;
	/**
	 * 删除指定Group组的SQL语句
	 */
	private final SQLiteStatement	myDeleteGroupStatement;

	public SQLiteConfig(Service service)
	{
		myService = service;
		myDatabase = service.openOrCreateDatabase("config.db", Context.MODE_PRIVATE, null);
		switch (myDatabase.getVersion()) {
		case 0:
			// 首次创建数据库文件时，创建config表，以groupName和name组合为主键
			myDatabase.execSQL("CREATE TABLE config (groupName VARCHAR, name VARCHAR, value VARCHAR, PRIMARY KEY(groupName, name) )");
			break;
		case 1:
			// 删除一些无用的记录
			myDatabase.beginTransaction();
			SQLiteStatement removeStatement = myDatabase.compileStatement(
					"DELETE FROM config WHERE name = ? AND groupName LIKE ?"
					);
			removeStatement.bindString(2, "/%");
			removeStatement.bindString(1, "Size");
			removeStatement.execute();
			removeStatement.bindString(1, "Title");
			removeStatement.execute();
			removeStatement.bindString(1, "Language");
			removeStatement.execute();
			removeStatement.bindString(1, "Encoding");
			removeStatement.execute();
			removeStatement.bindString(1, "AuthorSortKey");
			removeStatement.execute();
			removeStatement.bindString(1, "AuthorDisplayName");
			removeStatement.execute();
			removeStatement.bindString(1, "EntriesNumber");
			removeStatement.execute();
			removeStatement.bindString(1, "TagList");
			removeStatement.execute();
			removeStatement.bindString(1, "Sequence");
			removeStatement.execute();
			removeStatement.bindString(1, "Number in seq");
			removeStatement.execute();
			myDatabase.execSQL(
					"DELETE FROM config WHERE name LIKE 'Entry%' AND groupName LIKE '/%'"
					);
			myDatabase.setTransactionSuccessful();
			myDatabase.endTransaction();
			// VACUUM操作的意义在于清除表内已经被删除了的字段所占用的空间
			// 更多详细信息查看：http://database.9sssd.com/sqlite/art/1213
			myDatabase.execSQL("VACUUM");
			break;
		}
		// 当前版本为2
		myDatabase.setVersion(2);
		myGetValueStatement = myDatabase.compileStatement("SELECT value FROM config WHERE groupName = ? AND name = ?");
		mySetValueStatement = myDatabase.compileStatement("INSERT OR REPLACE INTO config (groupName, name, value) VALUES (?, ?, ?)");
		myUnsetValueStatement = myDatabase.compileStatement("DELETE FROM config WHERE groupName = ? AND name = ?");
		myDeleteGroupStatement = myDatabase.compileStatement("DELETE FROM config WHERE groupName = ?");
	}

	@Override
	synchronized public List<String> listGroups()
	{
		final LinkedList<String> list = new LinkedList<String>();
		final Cursor cursor = myDatabase.rawQuery("SELECT DISTINCT groupName FROM config", null);
		while (cursor.moveToNext()) {
			list.add(cursor.getString(0));
		}
		cursor.close();
		return list;
	}

	@Override
	synchronized public List<String> listNames(String group)
	{
		final LinkedList<String> list = new LinkedList<String>();
		final Cursor cursor = myDatabase.rawQuery("SELECT name FROM config WHERE groupName = ?", new String[] { group });
		while (cursor.moveToNext()) {
			list.add(cursor.getString(0));
		}
		cursor.close();
		return list;
	}

	@Override
	synchronized public void removeGroup(String name)
	{
		myDeleteGroupStatement.bindString(1, name);
		try {
			myDeleteGroupStatement.execute();
		} catch (SQLException e) {
		}
	}

	@Override
	synchronized public List<String> requestAllValuesForGroup(String group)
	{
		try {
			final List<String> pairs = new LinkedList<String>();
			// 查出所有groupName为group的name和value字段值
			final Cursor cursor = myDatabase.rawQuery(
					"SELECT name,value FROM config WHERE groupName = ?",
					new String[] { group }
					);
			while (cursor.moveToNext()) {
				pairs.add(cursor.getString(0) + "\000" + cursor.getString(1));
			}
			cursor.close();
			return pairs;
		} catch (SQLException e) {
			return Collections.emptyList();
		}
	}

	@Override
	synchronized public String getValue(String group, String name)
	{
		// 获取
		myGetValueStatement.bindString(1, group);
		myGetValueStatement.bindString(2, name);
		try {
			return myGetValueStatement.simpleQueryForString();
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	synchronized public void setValue(String group, String name, String value)
	{
		// 插入
		mySetValueStatement.bindString(1, group);
		mySetValueStatement.bindString(2, name);
		mySetValueStatement.bindString(3, value);
		try {
			mySetValueStatement.execute();
			sendChangeEvent(group, name, value);
		} catch (SQLException e) {
		}
	}

	@Override
	synchronized public void unsetValue(String group, String name)
	{
		// 删除
		myUnsetValueStatement.bindString(1, group);
		myUnsetValueStatement.bindString(2, name);
		try {
			myUnsetValueStatement.execute();
			sendChangeEvent(group, name, null);
		} catch (SQLException e) {
		}
	}

	private void sendChangeEvent(String group, String name, String value)
	{
		// 值变化通知广播
		myService.sendBroadcast(
				new Intent(ConfigShadow.OPTION_CHANGE_EVENT_ACTION)
						.putExtra("group", group)
						.putExtra("name", name)
						.putExtra("value", value)
				);
	}
}
