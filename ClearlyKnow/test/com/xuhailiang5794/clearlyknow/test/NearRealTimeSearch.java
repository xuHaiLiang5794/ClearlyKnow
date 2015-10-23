package com.xuhailiang5794.clearlyknow.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * NRT��near real time����ʵʱ��������
 * 
 * @author Johnny
 *
 */
public class NearRealTimeSearch {
	private static String path = "D:\\test\\index\\";
	private String[] ids = { "1", "2", "3", "4", "5", "6" };
	private String[] emails = { "aa@itat.org", "bb@itat.org", "cc@cc.org",
			"dd@sina.org", "ee@zttc.edu", "ff@itat.org" };
	private String[] contents = { "welcome to visited the space,I like book",
			"hello boy, I like pingpeng ball", "my name is cc I like game",
			"I like football", "I like football and I like basketball too",
			"I like movie and swim" };
	private Date[] dates = null;
	private int[] attachs = { 2, 3, 1, 4, 5, 5 };
	private String[] names = { "zhangsan", "lisi", "john", "jetty", "mike",
			"jake" };
	private Directory directory = null;
	IndexWriter writer = null;

	/** nrt init **/
	private TrackingIndexWriter trackingIndexWriter = null;
	private ReferenceManager<IndexSearcher> reMgr = null;// ������Lucene3.x�е�NrtManager
	private ControlledRealTimeReopenThread<IndexSearcher> crt = null;

	private void setDates() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates = new Date[ids.length];
			dates[0] = sdf.parse("2010-02-19");
			dates[1] = sdf.parse("2012-01-11");
			dates[2] = sdf.parse("2011-09-19");
			dates[3] = sdf.parse("2010-12-22");
			dates[4] = sdf.parse("2012-01-01");
			dates[5] = sdf.parse("2011-05-19");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	// ���ݳ�ʼ��
	public NearRealTimeSearch() {
		setDates();

		try {
			// directory = FSDirectory.open(new
			// File("/Users/ChinaMWorld/Desktop/index/"));
			directory = FSDirectory.open(Paths
					.get(path));
			// writer = new IndexWriter(directory, new
			// IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
			writer = new IndexWriter(directory, new IndexWriterConfig(
					new StandardAnalyzer()));

			trackingIndexWriter = new TrackingIndexWriter(writer);
			reMgr = new SearcherManager(writer, true, new SearcherFactory());
			/** ��0.025s~5.0s֮������һ���̣߳������ʱ������ʵ�� **/
			crt = new ControlledRealTimeReopenThread<>(trackingIndexWriter,
					reMgr, 5.0, 0.025);
			crt.setDaemon(true);// ����Ϊ��̨����
			crt.setName("Index update to disk");// �߳�����
			crt.start();// �߳�����

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * �����ύ�ڴ��е�������Ӳ���ϣ���ֹ��ʧ
	 */
	public void commit() {
		try {
			writer.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �������� Ҫʵ��search nrt,��Ҫʹ��TrackIndexWriter����document��ͬʱWriterҲ����Ҫ�رա�
	 * 
	 * **/
	public void index(boolean isNew) {
		if (isNew) {
			try {
				Document doc = null;
				for (int i = 0; i < ids.length; i++) {
					doc = new Document();
					doc.add(new StringField("id", ids[i], Store.YES));
					doc.add(new StringField("email", emails[i], Store.YES));
					doc.add(new TextField("content", contents[i], Store.NO));
					doc.add(new TextField("name", names[i], Store.YES));
					// �洢����
					doc.add(new IntField("attach", attachs[i], Store.YES));
					// �洢����
					doc.add(new LongField("date", dates[i].getTime(), Store.YES));
					// ʹ��trackingIndexWriter����document
					trackingIndexWriter.addDocument(doc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				commit();// �״δ������ύ����,ֻ���ύ�󣬲Ż�������Ƭ����Ҳ����Ϣ�ı�
			}
		}
	}

	/*** ��ѯ **/
	public void query() {
		IndexSearcher is = getSearcher();

		try {
			// ͨ��reader������Ч�Ļ�ȡ���ĵ�������
			System.out.println("numDocs:" + is.getIndexReader().numDocs());
			System.out.println("maxDocs:" + is.getIndexReader().maxDoc());
			System.out.println("deleteDocs:"
					+ is.getIndexReader().numDeletedDocs());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reMgr.release(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ɾ�� ʹ��trackIndexWriter��������ɾ����Ҳ����Ҫ�ر�Writer
	 * **/
	public void delete() {
		try {
			trackingIndexWriter.deleteDocuments(new Term("id", "2"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �޸� ʹ��trackIndexWriter�����޸ģ�����Ҫ�ر�writer
	 * **/
	public void update() {
		try {
			Document doc = new Document();
			/*
			 * Lucene��û���ṩ���£�����ĸ��²�����ʵ���������������ĺϼ� ��ɾ��֮�������
			 */
			doc.add(new StringField("id", "21", Store.YES));
			doc.add(new TextField("email", "aa.bb@s", Store.YES));
			doc.add(new TextField("content", "update content like", Store.NO));
			doc.add(new StringField("name", "jackson", Store.YES));
			trackingIndexWriter.updateDocument(new Term("id", "1"), doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** ʹ�õ�����ȡIndexSearch **/
	public IndexSearcher getSearcher() {
		IndexSearcher is = null;
		try {
			if (is == null) {
				reMgr.maybeRefresh();// ˢ��reMgr,��ȡ���µ�IndexSearcher
				is = reMgr.acquire();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (is == null) {
			throw new RuntimeException("indexSearcher is null!!!!");
		}
		return is;

	}

	/**
	 * ��ѯ
	 * 
	 * ��ѯʱsearch���ʹ����ɣ���Ҫ��search�ͷŻ�searchFactory�У�ʹ��reMgr��release(indexSearcher)
	 * �����ͷ�
	 * **/
	public void search() {
		IndexSearcher is = getSearcher();
		try {
//			TermQuery query = new TermQuery(new Term("content", "like"));
			TermQuery query = new TermQuery(new Term("attach", "2"));
			TopDocs tds = is.search(query, 10);
			for (ScoreDoc sd : tds.scoreDocs) {
				Document doc = is.doc(sd.doc);
				System.out.println(doc.get("id") + "---->" + doc.get("name")
						+ "[" + doc.get("email") + "]-->" + doc.get("id") + ","
						+ doc.get("attach") + "," + doc.get("date") + ","
						+ doc.getValues("email")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reMgr.release(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * �رճ�ʼ���̵߳Ĵ���
	 */
	public void close() {
		// stop the re-open thread
		crt.interrupt();
		crt.close();
		// close the indexWriter,commit �����й��޸ĵ�����
		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
