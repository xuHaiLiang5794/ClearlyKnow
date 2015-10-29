package com.xuhailiang5794.clearlyknow.manager;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.RandomAccess;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.xuhailiang5794.clearlyknow.manager.configuration.IndexConfiguration;
import com.xuhailiang5794.clearlyknow.manager.entity.IndexProperties;
import com.xuhailiang5794.clearlyknow.manager.utils.IndexUtils;

/**
 * 索引创建、数据追加、元素删除、元素修改
 * 
 * @author 徐海亮
 *
 */
public class IndexWriterManager2 {

	private IndexConfiguration configuration;
	private Directory directory;
	private Analyzer analyzer;
	private OpenMode openMode = OpenMode.CREATE_OR_APPEND;
	private IndexWriterConfig indexWriterConfig;
	private IndexWriter indexWriter;

	private TrackingIndexWriter trackingIndexWriter;

	public IndexWriterManager2() throws IOException {
		init();
	}

	public IndexWriterManager2(IndexWriterConfig indexWriterConfig,
			IndexWriter indexWriter, TrackingIndexWriter trackingIndexWriter) {
		super();
		this.indexWriterConfig = indexWriterConfig;
		this.indexWriter = indexWriter;
		this.trackingIndexWriter = trackingIndexWriter;
	}

	private void init() throws IOException {
		configuration = new IndexConfiguration();
		this.directory = NIOFSDirectory
				.open(Paths.get(configuration.getPath()));
		this.analyzer = new IKAnalyzer();
		this.indexWriterConfig = new IndexWriterConfig(analyzer);
		this.indexWriterConfig.setOpenMode(openMode);
		this.indexWriterConfig.setRAMBufferSizeMB(configuration
				.getRamBufferSizeMb());
		this.indexWriter = new IndexWriter(directory, indexWriterConfig);
		this.indexWriter.forceMerge(configuration.getMaxNumSegments());
		this.trackingIndexWriter = new TrackingIndexWriter(indexWriter);
	}

	public long delete(String name, String value) throws IOException {
		return trackingIndexWriter.deleteDocuments(new Term(name, value));
	}

	public void deleteAll() throws IOException {
		indexWriter.deleteAll();
	}

	public long update(Term term, Document doc) throws IOException {
		return trackingIndexWriter.updateDocument(term, doc);
	}

	public void createIndex(List<?> data, Class<?> clazz,
			List<IndexProperties> propertys) throws IOException,
			IllegalArgumentException, IllegalAccessException {
		Document doc = null;
		if (data != null && !data.isEmpty()) {
			if (data instanceof RandomAccess) {
				for (int i = 0, size = data.size(); i < size; i++) {
					Object obj = data.get(i);
					doc = createDocument(obj, propertys);
					trackingIndexWriter.addDocument(doc);
				}
			}
		}
	}

	private Document createDocument(Object obj, List<IndexProperties> propertys)
			throws IllegalArgumentException, IllegalAccessException {
		Document doc = new Document();
		if (propertys instanceof RandomAccess) {
			org.apache.lucene.document.Field field;
			for (IndexProperties properties : propertys) {
				field = null;
				Object value = properties.getField().get(obj);
				String name = properties.getName();
				Store stored = properties.getStore();
				Class<?> clazz = properties.getClazz();
				if (!IndexUtils.isNumericType(clazz)) {
					field = createNotNumericTypeField(clazz, name, value,
							stored);
				} else {
					field = createNumericTypeField(clazz, name, value, stored);
				}
				doc.add(field);
			}
		}
		return doc;
	}

	private Field createNotNumericTypeField(Class<?> clazz, String name,
			Object value, Store stored) {
		org.apache.lucene.document.Field field = null;
		field = new TextField(name, String.valueOf(value), stored);
		return field;
	}

	private Field createNumericTypeField(Class<?> clazz, String name,
			Object value, Store stored) {
		org.apache.lucene.document.Field field = null;
		if (clazz == Double.class) {
			field = new DoubleField(name, (Double) value, stored);
		} else if (clazz == Float.class) {
			field = new FloatField(name, (Float) value, stored);
		} else if (clazz == Integer.class) {
			field = new IntField(name, (Integer) value, stored);
		} else if (clazz == Long.class) {
			field = new LongField(name, (Long) value, stored);
		}
		return field;
	}

	public void commit() throws IOException {
		indexWriter.commit();
	}

	public void close() throws IOException {
		commit();
		indexWriter.close();
	}

}
