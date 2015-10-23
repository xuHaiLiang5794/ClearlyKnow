package com.xuhailiang5794.clearlyknow.manager.configuration;

import com.xuhailiang5794.clearlyknow.manager.Constants;

/**
 * 索引参数配置类
 * 
 * @author 徐海亮
 *
 */
public class IndexConfiguration {

	private String path = Constants.PATH;

	private int maxNumSegments = Constants.MAX_NUM_SEGMENTS;
	private double ramBufferSizeMb = Constants.RAM_BUFFER_SIZE_MB;

	private double targetMaxStaleSec = Constants.TARGET_MAX_STALE_SEC;
	private double targetMinStaleSec = Constants.TARGET_MIN_STALE_SEC;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getMaxNumSegments() {
		return maxNumSegments;
	}

	public void setMaxNumSegments(int maxNumSegments) {
		this.maxNumSegments = maxNumSegments;
	}

	public double getRamBufferSizeMb() {
		return ramBufferSizeMb;
	}

	public void setRamBufferSizeMb(double ramBufferSizeMb) {
		this.ramBufferSizeMb = ramBufferSizeMb;
	}

	public double getTargetMaxStaleSec() {
		return targetMaxStaleSec;
	}

	public void setTargetMaxStaleSec(double targetMaxStaleSec) {
		this.targetMaxStaleSec = targetMaxStaleSec;
	}

	public double getTargetMinStaleSec() {
		return targetMinStaleSec;
	}

	public void setTargetMinStaleSec(double targetMinStaleSec) {
		this.targetMinStaleSec = targetMinStaleSec;
	}

}
