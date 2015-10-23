package com.xuhailiang5794.clearlyknow.manager;

import java.lang.reflect.Field;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;

import com.xuhailiang5794.clearlyknow.test.Entity;

/**
 * �洢��Ҫ���������������ֶ�; ���ֶ��Ƿ�洢; �����${@link #field}�ֶα����Ǵ���{@link IndexManager
 * IndexManager}������ʵ�������
 * 
 * @author �캣��
 *
 */
public class IndexProperties {

	// ������ʱ��Ҫ��ʵ������Զ���
	private Field field;

	private Store store;

	// ��������������ʱ�Դ��ֶ�Ϊ�����ֶ�����
	private String name;

	private Class<?> clazz;

	public IndexProperties(Field field, Store store) {
		super();
		this.field = field;
		this.store = store;
		this.clazz = field.getType();
		this.name = field.getName();
		if (!field.isAccessible())
			field.setAccessible(true);
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public String getName() {
		return name;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return "IndexProperties [field=" + field + ", store=" + store
				+ ", name=" + name + "]";
	}

	public static void main(String[] args) {
		Field[] fs = Entity.class.getDeclaredFields();
		FieldType.NumericType.valueOf("Double");
		for (Field f : fs) {
			System.out.println(new IndexProperties(f, Store.YES));
		}
	}

}
