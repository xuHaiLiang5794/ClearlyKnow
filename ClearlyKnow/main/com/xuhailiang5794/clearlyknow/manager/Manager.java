package com.xuhailiang5794.clearlyknow.manager;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import com.xuhailiang5794.clearlyknow.manager.configuration.IndexConfiguration;

public class Manager {

	// basic
	protected IndexConfiguration configuration;
	protected Directory directory;
	protected Analyzer analyzer;
	protected IndexWriterConfig writerConfig;
	protected IndexWriter writer;
	protected TrackingIndexWriter trackingIndexWriter;
	protected ReferenceManager<IndexSearcher> referenceManager;
	protected ControlledRealTimeReopenThread<IndexSearcher> controlledRealTimeReopenThread;
	
	// component
	protected IndexWriterManager2 writerManager;
	protected IndexSearcherManager searcherManager;

	public Manager(IndexConfiguration configuration, Analyzer analyzer, OpenMode openMode) throws IOException {
		if (configuration == null)
			configuration = new IndexConfiguration();
		this.configuration = configuration;
		this.analyzer = analyzer;
		init(openMode);
	}

	private void init(OpenMode openMode) throws IOException {
		this.directory = new NIOFSDirectory(Paths.get(configuration.getPath()));
		this.writerConfig = new IndexWriterConfig(analyzer);
		this.writerConfig.setOpenMode(openMode);
		this.writerConfig.setRAMBufferSizeMB(configuration
				.getRamBufferSizeMb());
		this.writer = new IndexWriter(directory, writerConfig);
		this.writer.forceMerge(configuration.getMaxNumSegments());
		this.trackingIndexWriter = new TrackingIndexWriter(writer);
		this.referenceManager = new SearcherManager(writer, true, null);
		this.controlledRealTimeReopenThread = new ControlledRealTimeReopenThread<IndexSearcher>(
				trackingIndexWriter, referenceManager,
				configuration.getTargetMaxStaleSec(),
				configuration.getTargetMinStaleSec());
	}

	public IndexWriterManager2 buildWriter() {
		this.writerManager = new IndexWriterManager2(writerConfig, writer,
				trackingIndexWriter);
		return writerManager;
	}
	
	
	
	
	
	

//	/**
//	 * 指定{@link Manager}以{@link OpenMode}模式操作索引库
//	 */
//	public static enum OpenMode {
//		/**
//		 * 只读索引
//		 */
//		READ_ONLY,
//		/**
//		 * 索引读写
//		 */
//		READ_WRITE,
//		/**
//		 * 只写索引
//		 */
//		JUST_WRITE;
//	}

}
