package com.xuhailiang5794.clearlyknow.manager.constants;

import java.io.File;

public interface Constants {

	String PATH = System.getProperty("user.dir") + File.separator + "indexDir";

	int MAX_NUM_SEGMENTS = 5;
	double RAM_BUFFER_SIZE_MB = 32;

	double TARGET_MAX_STALE_SEC = 5;
	double TARGET_MIN_STALE_SEC = 0.025;

}
