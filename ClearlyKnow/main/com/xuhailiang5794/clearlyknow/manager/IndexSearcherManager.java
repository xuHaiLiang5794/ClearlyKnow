package com.xuhailiang5794.clearlyknow.manager;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
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
 * 搜索
 * 
 * @author 徐海亮
 *
 */
public class IndexSearcherManager {

	protected IndexConfiguration configuration;
	protected Directory directory;
	protected Analyzer analyzer;
	protected IndexWriterConfig indexWriterConfig;
	protected IndexWriter indexWriter;
	protected TrackingIndexWriter trackingIndexWriter;
	protected ReferenceManager<IndexSearcher> referenceManager;
	protected ControlledRealTimeReopenThread<IndexSearcher> controlledRealTimeReopenThread;

	public IndexSearcherManager() throws IOException {
		init();
	}

	private void init() throws IOException {
		this.configuration = new IndexConfiguration();
		this.directory = new NIOFSDirectory(Paths.get(configuration.getPath()));
		this.analyzer = new IKAnalyzer();
		this.indexWriterConfig = new IndexWriterConfig(analyzer);
		try {
			this.indexWriter = new IndexWriter(directory, indexWriterConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.trackingIndexWriter = new TrackingIndexWriter(indexWriter);
		this.referenceManager = new SearcherManager(indexWriter, true, null);
		this.controlledRealTimeReopenThread = new ControlledRealTimeReopenThread<IndexSearcher>(
				trackingIndexWriter, referenceManager,
				configuration.getTargetMaxStaleSec(),
				configuration.getTargetMinStaleSec());
	}

	public void searcher() throws IOException {
		IndexSearcher indexSearcher = getIndexSearcher();
		System.out.println(indexSearcher.getIndexReader().numDocs());
	}

	public IndexSearcher getIndexSearcher() throws IOException {
		referenceManager.maybeRefresh();
		return referenceManager.acquire();
	}

	protected void release(IndexSearcher indexSearcher) throws IOException {
		referenceManager.release(indexSearcher);
	}

	public void close() throws IOException {
		controlledRealTimeReopenThread.interrupt();
		controlledRealTimeReopenThread.close();
		indexWriter.commit();
		indexWriter.close();
	}
}
