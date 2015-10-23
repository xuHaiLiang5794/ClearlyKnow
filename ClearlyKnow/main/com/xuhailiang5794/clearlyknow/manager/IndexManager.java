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
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.xuhailiang5794.clearlyknow.manager.configuration.IndexConfiguration;

/**
 * 索引创建等
 * 
 * @author 徐海亮
 *
 */
public class IndexManager {

	private String path = "D:\\test\\index11\\";

	private IndexConfiguration configuration;
	private Directory directory;
	private Analyzer analyzer;
	private IndexWriterConfig indexWriterConfig;
	private IndexWriter indexWriter;

	private TrackingIndexWriter trackingIndexWriter;
	private ReferenceManager<IndexSearcher> referenceManager;
	private ControlledRealTimeReopenThread<IndexSearcher> controlledRealTimeReopenThread;

	public IndexManager(String path) throws IOException {
		init();
		this.path = path;
	}

	private void init() throws IOException {
		configuration = new IndexConfiguration();
		this.path = configuration.getPath();
		// this.directory = FSDirectory.open(Paths.get(path));
		this.directory = NIOFSDirectory.open(Paths.get(path));
		// this.analyzer = new StandardAnalyzer();
		// this.analyzer = new PaodingAnalyzer();
		// this.analyzer = new WhitespaceAnalyzer();
		// this.analyzer = new CJKAnalyzer();
		this.analyzer = new IKAnalyzer();
		this.indexWriterConfig = new IndexWriterConfig(analyzer);
		this.indexWriter = new IndexWriter(directory, indexWriterConfig);
		this.trackingIndexWriter = new TrackingIndexWriter(indexWriter);
		indexWriterConfig
				.setRAMBufferSizeMB(configuration.getRamBufferSizeMb());
		this.referenceManager = new SearcherManager(indexWriter, true, null);
		this.controlledRealTimeReopenThread = new ControlledRealTimeReopenThread<IndexSearcher>(
				trackingIndexWriter, referenceManager,
				configuration.getTargetMaxStaleSec(),
				configuration.getTargetMinStaleSec());
		this.controlledRealTimeReopenThread.setDaemon(true);
		this.controlledRealTimeReopenThread.setName("Index update to Disk");
		this.controlledRealTimeReopenThread.start();

		this.indexWriter.forceMerge(configuration.getMaxNumSegments());
	}

	public void deleteAll() throws IOException {
		indexWriter.deleteAll();
	}

	public void commit() throws IOException {
		indexWriter.commit();
	}

	public void index(List<?> list, Class<?> clazz,
			List<IndexProperties> propertys) throws IOException,
			IllegalArgumentException, IllegalAccessException {
		Document doc = null;
		if (list != null && !list.isEmpty()) {
			if (list instanceof RandomAccess) {
				for (int i = 0, size = list.size(); i < size; i++) {
					Object obj = list.get(i);
					doc = createDocument(obj, propertys);
					trackingIndexWriter.addDocument(doc);
				}
			}
		}
	}

	public void close() throws IOException {
		indexWriter.close();
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
				if (IndexUtils.isNumericType(clazz)) {
					field = createNumericTypeField(clazz, name, value, stored);
				} else {
					field = createNotNumericTypeField(clazz, name, value,
							stored);
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

}
