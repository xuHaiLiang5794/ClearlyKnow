package com.xuhailiang5794.clearlyknow.test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queries.TermsQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.junit.Before;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.xuhailiang5794.clearlyknow.manager.IndexManager;
import com.xuhailiang5794.clearlyknow.manager.IndexProperties;

public class IndexManagerTest {

	private IndexManager indexManager;

	@Before
	public void init() throws IOException {
		indexManager = new IndexManager("D:\\test\\index11\\");
	}

	@Test
	public void testCreateIndex() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException,
			IllegalAccessException, IOException {

		indexManager.deleteAll();
		List<Entity> list = new ArrayList<Entity>();
		list.add(new Entity("小明叮叮当当该f", 15, "男"));
		Class<?> clazz = Entity.class;
		List<IndexProperties> propertys = new ArrayList<IndexProperties>();
		propertys.add(new IndexProperties(clazz.getDeclaredField("name"),
				Store.YES));
		propertys.add(new IndexProperties(clazz.getDeclaredField("age"),
				Store.YES));
		propertys.add(new IndexProperties(clazz.getDeclaredField("sex"),
				Store.YES));
		propertys.add(new IndexProperties(clazz.getDeclaredField("desc"),
				Store.YES));
		indexManager.index(list, clazz, propertys);
		indexManager.commit();
		indexManager.close();
	}

	@Test
	public void testSearchAll() throws IOException, Exception {
		QueryParser parser = new QueryParser("name", new IKAnalyzer());
		Query query = parser.parse("当当");
		// query = new TermQuery(new Term("name", "小明叮叮"));
		IndexSearcher indexSearcher = new IndexSearcher(
				DirectoryReader.open(FSDirectory.open(Paths
						.get("D:\\test\\index11\\"))));

		List<Entity> list = new ArrayList<Entity>();
		list.add(new Entity("小明叮叮当当该f", 15, "男"));
		Class<?> clazz = Entity.class;
		List<IndexProperties> propertys = new ArrayList<IndexProperties>();
		propertys.add(new IndexProperties(clazz.getDeclaredField("name"),
				Store.YES));
		propertys.add(new IndexProperties(clazz.getDeclaredField("age"),
				Store.YES));
		propertys.add(new IndexProperties(clazz.getDeclaredField("sex"),
				Store.YES));
		propertys.add(new IndexProperties(clazz.getDeclaredField("desc"),
				Store.YES));
		indexManager.index(list, clazz, propertys);
		indexManager.commit();
		indexManager.close();

		if (!((DirectoryReader) indexSearcher.getIndexReader()).isCurrent()) {
			System.out
					.println("(DirectoryReader) indexSearcher.getIndexReader())");
			indexSearcher = new IndexSearcher(
					DirectoryReader
							.openIfChanged((DirectoryReader) indexSearcher
									.getIndexReader()));
		}
		System.out.println((indexSearcher.search(query, 10).totalHits));
		System.out
				.println(indexSearcher.doc(indexSearcher.search(
						new WildcardQuery(new Term("name", "*小明*")), 10).scoreDocs[0].doc));
	}

	@Test
	public void testAnalyzer() throws IOException {
		analyzer(new StandardAnalyzer());
		System.out.println();
		analyzer(new CJKAnalyzer());
		System.out.println();
		analyzer(new WhitespaceAnalyzer());
		System.out.println();
		analyzer(new KeywordAnalyzer());
		System.out.println();
		analyzer(new IKAnalyzer());
		System.out.println();
	}

	private void analyzer(Analyzer analyzer) throws IOException {
		TokenStream ts = null;
		try {
			String str = "我们是是中国人";
			StringReader reader = new StringReader(str);
			ts = analyzer.tokenStream("field", reader);
			OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
			CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
			TypeAttribute type = ts.addAttribute(TypeAttribute.class);

			ts.reset();

			while (ts.incrementToken()) {
				System.out.println(offset.startOffset() + "-"
						+ offset.endOffset() + " : " + term.toString() + "|"
						+ type.type());
			}
			ts.end();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ts.close();
		}
	}

	public static void main(String[] args) throws Exception {
		FSDirectory fsDirectory = FSDirectory.open(Paths
				.get("D:\\test\\index11\\"));
		RAMDirectory ramDirectory = new RAMDirectory();
		IndexWriterConfig conf = new IndexWriterConfig(new IKAnalyzer());
		IndexWriter fsWriter = new IndexWriter(fsDirectory, conf);
		IndexWriter ramWriter = new IndexWriter(ramDirectory, conf);
		DirectoryReader indexReader = DirectoryReader.open(fsDirectory);
		// indexReader.openIfChanged(oldReader)
		ramWriter.addDocument(new Document());
		// ramWriter.

	}

}
