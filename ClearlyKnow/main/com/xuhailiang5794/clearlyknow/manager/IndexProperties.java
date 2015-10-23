package com.xuhailiang5794.clearlyknow.manager;

import java.lang.reflect.Field;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;

import com.xuhailiang5794.clearlyknow.test.Entity;

/**
 * 存储需要建立索引的属性字段; 该字段是否存储; 该类的${@link #field}字段必须是传入{@link IndexManager
 * IndexManager}的数据实体的属性
 * 
 * @author 徐海亮
 *
 */
public class IndexProperties {

	// 建索引时需要的实体的属性对象
	private Field field;

	private Store store;

	// 属性名，建索引时以此字段为索引字段名称
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
