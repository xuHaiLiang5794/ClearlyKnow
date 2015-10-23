package com.xuhailiang5794.clearlyknow.test;

import org.junit.Before;
import org.junit.Test;

public class NearRealTimeSearchTest {
	private static NearRealTimeSearch ns = null;

	@Before
	public void init() {
		ns = new NearRealTimeSearch();
	}

	@Test
	public void testIndex() {
		ns.index(true);
	}

	@Test
	public void testSearch() {
		ns.query();
		for (int i = 0; i < 10; i++) {
			ns.search();
			System.out.println(i + "--------------------");
			ns.delete();
			if (i == 3) {
				ns.update();
				ns.query();
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ns.commit();
		ns.query();
	}

	@Test
	public void close() {
		ns.close();
	}

	@Test
	public void testQuery() {
		ns.query();
	}
}
